package org.mule.tools.rhinodo.tools;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
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

}
