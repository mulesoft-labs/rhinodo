/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.impl;

import org.mule.tools.rhinodo.api.NodeModule;
import org.mule.tools.rhinodo.api.NodeModuleProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PrimitiveNodeModuleProvider implements NodeModuleProvider {

    private NodeModuleProvider nodeModuleProvider;
    private List<NodeModuleImpl> nodeModuleList;

    public PrimitiveNodeModuleProvider(JavascriptResource env, NodeModuleProvider nodeModuleProvider) {
        this.nodeModuleProvider = nodeModuleProvider;
        this.nodeModuleList = new ArrayList<NodeModuleImpl>();

        if (env == null) {
            throw new IllegalArgumentException("env cannot be null");
        }

        addFileModules(env.getFile());

    }

    private void addFileModules(File file1) {
        File[] files = file1.listFiles();
        if ( files == null) {
            throw new IllegalArgumentException();
        }
        for (File file : files) {
            String fileName = file.getName();
            if ( fileName.endsWith(".js") ) {
                String moduleName = fileName.substring(0,fileName.lastIndexOf(".js"));
                nodeModuleList.add(NodeModuleImpl.create(moduleName, file.toURI()));
            }
        }
    }

    @Override
    public Collection<? extends NodeModule> getModules() {
        ArrayList<NodeModule> nodeModules = new ArrayList<NodeModule>(nodeModuleList);
        nodeModules.addAll(nodeModuleProvider.getModules());
        return nodeModules;
    }
}
