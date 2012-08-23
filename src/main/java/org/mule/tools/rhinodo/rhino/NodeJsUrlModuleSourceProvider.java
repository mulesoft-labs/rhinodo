package org.mule.tools.rhinodo.rhino;

import org.mozilla.javascript.commonjs.module.provider.ModuleSource;
import org.mozilla.javascript.commonjs.module.provider.UrlModuleSourceProvider;
import org.mule.tools.rhinodo.tools.JarURIHelper;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class NodeJsUrlModuleSourceProvider extends UrlModuleSourceProvider {

    private Map<String,URI> privilegedUris;

    public NodeJsUrlModuleSourceProvider(Map<String,URI> privilegedUris) {
        super(privilegedUris.values(),null);
        this.privilegedUris = privilegedUris;
    }

    @Override
    protected ModuleSource loadFromPrivilegedLocations(
            String moduleId, Object validator)
            throws IOException, URISyntaxException {
        return loadFromPathMap(moduleId, validator, privilegedUris);
    }

    private ModuleSource loadFromPathMap(String moduleId,
                                         Object validator, Map<String, URI> paths)
            throws IOException, URISyntaxException
    {
        if(paths == null) {
            return null;
        }

        URI basePath = paths.get(moduleId);
        URI path;

        if(basePath == null) {
            return super.loadFromPrivilegedLocations(moduleId, validator);
        }

        if ( "file".equals(basePath.getScheme())) {
            path = getModuleRealURIWithFileScheme(basePath);
        } else if ( "jar".equals(basePath.getScheme()) ) {
            path = getModuleRealURIWithJarScheme(basePath);
        } else {
            throw new RuntimeException(String.format("Module loading [%s] scheme not supported.",basePath.getScheme()));
        }


        final ModuleSource moduleSource = loadFromUri(
                path, basePath, validator);

        if (moduleSource != null) {
            return moduleSource;
        }
        return null;
    }

    public List<JarEntry> getListOfJarFiles(JarURIHelper jarHelper) {
        URL jarURL = jarHelper.getJarURL();
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

    private URI getModuleRealURIWithJarScheme(URI basePath) {
        JarURIHelper jarHelper = new JarURIHelper(basePath);

        List<JarEntry> listOfJarFiles = getListOfJarFiles(jarHelper);
        Map<String, JarEntry> entryList = new HashMap<String, JarEntry>();

        for (JarEntry jarEntry : listOfJarFiles) {
            entryList.put(jarEntry.getName(), jarEntry);
        }

        String insideJarRelativePath = jarHelper.getInsideJarRelativePath();
        String substring = insideJarRelativePath.substring(0, insideJarRelativePath.length() - 1);
        if ( !entryList.containsKey(substring) ) {
            if ( !entryList.containsKey(insideJarRelativePath + ".js/") ) {
                throw new IllegalArgumentException(String.format("Error: invalid jar path [%s]", basePath));
            } else {
                return URI.create(basePath.toString() + ".js");
            }
        } else if ( entryList.get(substring).isDirectory() ) {
            if ( !entryList.containsKey(insideJarRelativePath + "/index.js") ) {
                throw new IllegalArgumentException(String.format("Error: invalid jar path [%s]", basePath));
            } else {
                return URI.create(basePath.toString() + "/index.js");
            }
        }

        if ( basePath.toString().endsWith("/") ) {
            try {
                return new URI(basePath.toString().substring(0,basePath.toString().length()-1));
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        return basePath;
    }

    private URI getModuleRealURIWithFileScheme(URI basePath) {
        File file = new File(basePath.getPath());
        if ( !file.exists() ) {
            File newFile = new File(file.toString() + ".js");
            if ( !newFile.exists() ) {
                throw new IllegalArgumentException(String.format("Error: invalid path [%s]", basePath));
            } else  {
                return newFile.toURI();
            }
        } else if ( file.isDirectory() ) {
            File newFile = new File(file.toString(), "index.js");
            if ( !newFile.exists() ) {
                throw new IllegalArgumentException(String.format("Error: invalid path [%s]", basePath));
            } else {
                return newFile.toURI();
            }
        }

        return basePath;
    }
}
