package org.mule.tools.rhinodo.tools;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

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
}
