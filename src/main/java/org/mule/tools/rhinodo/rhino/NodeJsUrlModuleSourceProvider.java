package org.mule.tools.rhinodo.rhino;

import org.mozilla.javascript.commonjs.module.provider.ModuleSource;
import org.mozilla.javascript.commonjs.module.provider.UrlModuleSourceProvider;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

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

        URI path = paths.get(moduleId);
        if(path == null) {
            return super.loadFromPrivilegedLocations(moduleId, validator);
        }

        // Assumption: The URI is a File in the filesystem
        File file =new File(path.getPath());
        if ( file.isDirectory() ) {
            file = new File(file, "index.js");
        }

        final ModuleSource moduleSource = loadFromUri(
                file.toURI(), path, validator);
        if (moduleSource != null) {
            return moduleSource;
        }
        return null;
    }
}
