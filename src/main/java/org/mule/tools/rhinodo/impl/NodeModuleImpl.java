/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.impl;

import org.mule.tools.rhinodo.api.NodeModule;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class NodeModuleImpl implements NodeModule {

    private URI path;
    private String name;

    public static NodeModuleImpl create(String moduleName, URI path) {
        return new NodeModuleImpl(moduleName, path);
    }

    public static NodeModuleImpl create(URI root, Map<String, String> map) {
        //TODO Add validations
        String main = map.get("main");
        if (main.startsWith("./")) {
            main = main.substring(2);
        }

        String pathToReorder = root.getPath() + main;

        File path = null;

        path = cleanUpPathToModule(map.get("name"), pathToReorder);

        return new NodeModuleImpl(map.get("name"), path.toURI());
    }

    public static File cleanUpPathToModule(String name, String pathToReorder) {
        File path;
        File file = new File(pathToReorder);
        if( file.exists() && file.isDirectory() ) {
                path = new File(file, "index.js");
        } else if ( file.exists() && file.isFile() ) {
            path = file;
        } else {
            path = new File(file.getAbsolutePath() + ".js");
            if( !path.exists() ) {
                throw new RuntimeException("Module " + name + " not found");
            }
        }
        return path;
    }

    private NodeModuleImpl(String moduleName, URI path) {
        this.name = moduleName;
        this.path = path;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public URI getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NodeModuleImpl that = (NodeModuleImpl) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (path != null ? !path.equals(that.path) : that.path != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = path != null ? path.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
