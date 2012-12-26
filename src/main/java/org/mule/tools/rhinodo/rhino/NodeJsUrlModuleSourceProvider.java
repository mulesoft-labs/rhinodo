/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.rhino;

import org.apache.commons.io.FilenameUtils;
import org.mozilla.javascript.commonjs.module.provider.ModuleSource;
import org.mozilla.javascript.commonjs.module.provider.UrlModuleSourceProvider;
import org.mule.tools.rhinodo.impl.NodeModuleImpl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class NodeJsUrlModuleSourceProvider extends UrlModuleSourceProvider {

    private final Map<String,URI> privilegedUris;

    public NodeJsUrlModuleSourceProvider(Map<String,URI> privilegedUris) {
        super(privilegedUris.values(),null);
        this.privilegedUris = privilegedUris;
    }

    public void addPrivilegedUri(String moduleId, URI uri) {
        privilegedUris.put(moduleId, uri);
    }

    @Override
    protected ModuleSource loadFromPrivilegedLocations(
            String moduleId, Object validator)
            throws IOException, URISyntaxException {
        return loadFromPathMap(moduleId, validator, privilegedUris);
    }

    @Override
    protected ModuleSource loadFromUri(URI uri, URI base, Object validator) throws IOException, URISyntaxException {

        if( base == null || uri == null ) {
            return null;
        }

        File newUri = null;
        if( !uri.getPath().equals(base.getPath())) {
            String basePath = FilenameUtils.getFullPath(base.getPath().substring(0, base.getPath().length() - 1));
            String originalUri = uri.getPath();

            if( originalUri.contains(base.getPath()) ) {
                originalUri = basePath + originalUri.replace(base.getPath(), "");
            }

            newUri = NodeModuleImpl.cleanUpPathToModule("", originalUri);
        } else {
            newUri = new File(uri.getPath());
        }

        return super.loadFromUri(newUri.toURI(), base, validator);
    }

    private ModuleSource loadFromPathMap(String moduleId,
                                         Object validator, Map<String, URI> paths)
            throws IOException, URISyntaxException
    {
        if(paths == null) {
            return null;
        }

        URI path = paths.get(moduleId);

        if( path == null ) {
            path = new File(moduleId).toURI();
            if ( loadFromUri(path, path, null) == null ) {
                throw new RuntimeException("Cannot find module " + moduleId);
            }
        }

        final ModuleSource moduleSource = loadFromUri(
                path, path, validator);

        if (moduleSource != null) {
            return moduleSource;
        }
        return null;
    }
}
