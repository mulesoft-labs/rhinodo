/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

public class JarURIHelper {

    private String insideJarRelativePath;

    private URL jarURL;

    public JarURIHelper(URI jarURI) {
        String fullURI = jarURI.getSchemeSpecificPart();
        String [] jarFileURIParts = fullURI.split("!");
        if ( jarFileURIParts.length != 2 ) {
            throw new IllegalArgumentException("Invalid jar URI specified");
        }

        String insideJarRelativePath = jarFileURIParts[1];

        if(insideJarRelativePath.startsWith("/")) {
            insideJarRelativePath = insideJarRelativePath.substring(1);
        }

        this.insideJarRelativePath = insideJarRelativePath;
        try {
            this.jarURL = new URL(jarFileURIParts[0]);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public String getInsideJarRelativePath() {
        return insideJarRelativePath;
    }

    public URL getJarURL() {
        return this.jarURL;
    }

    public boolean exists() {
        String insideJarRelativePath = getInsideJarRelativePath();
        return getEntryList().containsKey(insideJarRelativePath);
    }

    public List<JarEntry> getListOfJarFiles() {
        URL jarURL = this.getJarURL();
        List<JarEntry> listOfJarEntries = new ArrayList<JarEntry>();

        JarInputStream jarInputStream;
        try {
            jarInputStream = new JarInputStream(jarURL.openStream());
            JarEntry jarEntry;
            while( (jarEntry = jarInputStream.getNextJarEntry() ) != null ) {
                listOfJarEntries.add(jarEntry);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return listOfJarEntries;
    }

    public Map<String, JarEntry> getEntryList() {
        Map<String, JarEntry> entryList = new HashMap<String, JarEntry>();
        List<JarEntry> listOfJarFiles = getListOfJarFiles();

        for (JarEntry jarEntry : listOfJarFiles) {
            entryList.put(jarEntry.getName(), jarEntry);
        }

        return entryList;
    }


    public void copyToFolder( String destDir) throws IOException {
        JarFile jar = null;
        jar = new JarFile(jarURL.getFile());
        Enumeration e = jar.entries();
        while (e.hasMoreElements()) {
            JarEntry file = (JarEntry) e.nextElement();
            File f = new File(destDir + File.separator + file.getName());
            if (file.isDirectory()) { // if its a directory, create it
                f.mkdir();
                continue;
            }
            if ( f.exists() ) {
                continue;
            }
            InputStream is = null; // get the input stream
            try {
                is = jar.getInputStream(file);
            } catch (IOException e1) {
                throw new RuntimeException(e1);
            }
            FileOutputStream fos = new FileOutputStream(f);
            while (is.available() > 0) {  // write contents of 'is' to 'fos'
                fos.write(is.read());
            }
            fos.close();
            is.close();
        }

    }
}
