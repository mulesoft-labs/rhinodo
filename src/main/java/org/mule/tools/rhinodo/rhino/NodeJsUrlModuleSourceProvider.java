package org.mule.tools.rhinodo.rhino;

import org.mozilla.javascript.commonjs.module.provider.ModuleSource;
import org.mozilla.javascript.commonjs.module.provider.UrlModuleSourceProvider;
import org.mule.tools.rhinodo.tools.JarURIHelper;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.jar.JarEntry;

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

        String pathAsString = path.toString();
        int lastIndex = 0;
        if ( (lastIndex = pathAsString.lastIndexOf("/") ) == pathAsString.length() - 1  ) {
            lastIndex = pathAsString.lastIndexOf("/", pathAsString.lastIndexOf("/") - 1);
        }
        URI newBasePath = URI.create(pathAsString.substring(0,lastIndex + 1));

        final ModuleSource moduleSource = loadFromUri(
                path, newBasePath, validator);

        if (moduleSource != null) {
            return moduleSource;
        }
        return null;
    }

    private URI getModuleRealURIWithJarScheme(URI basePath) {
        JarURIHelper jarHelper = new JarURIHelper(basePath);
        Map<String, JarEntry> entryList = jarHelper.getEntryList();

        String insideJarRelativePath = jarHelper.getInsideJarRelativePath();
        String substring = insideJarRelativePath.substring(0, insideJarRelativePath.length() - 1);

        URI uri = resolverFile(basePath, entryList, insideJarRelativePath, substring);
        if(uri == null) {
            uri = resolverFile(basePath,entryList, insideJarRelativePath,insideJarRelativePath);
        }

        if (uri == null ) {
            throw new IllegalArgumentException(String.format("Error: invalid jar path [%s]", basePath));
        }
        return uri;
    }

    private URI sanitizeURI(URI uri, String suffix)  {
        String string = uri.toString();
        if (string.endsWith("/")) {
            try {
                return new URI(string.substring(0, string.length() - 1) + suffix);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            return new URI(string + suffix);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

    }

    private URI resolverFile(URI basePath, Map<String, JarEntry> entryList, String insideJarRelativePath, String substring) {
        if ( !entryList.containsKey(substring) ) {
            if ( entryList.containsKey(insideJarRelativePath + ".js/") ) {
                return sanitizeURI(basePath, ".js");
            } else if ( entryList.containsKey(insideJarRelativePath + ".js") ) {
                return sanitizeURI(basePath, ".js");
            } if ( entryList.containsKey(substring+ ".js/") ) {
                return sanitizeURI(basePath, ".js");
            } else if ( entryList.containsKey(substring + ".js") ) {
                return sanitizeURI(basePath, ".js");
            } else {
                return null;
            }
        } else if ( entryList.get(substring).isDirectory() ) {
            if ( entryList.containsKey(insideJarRelativePath + "/index.js") ) {
                return sanitizeURI(basePath, "/index.js");
            } else if (entryList.containsKey(substring + "index.js") ) {
                return sanitizeURI(basePath, "/index.js");
            } else {
                return null;
            }
        }

        if ( basePath.toString().endsWith("/") ) {
            try {
                return new URI(basePath.toString().substring(0,basePath.toString().length()-1));
            } catch (URISyntaxException e) {
                return null;
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
