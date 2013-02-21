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
import org.mule.tools.rhinodo.tools.JarURIHelper;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class NodeModuleProviderImpl implements NodeModuleProvider {

    private List<NodeModule> nodeModules = new ArrayList<NodeModule>();

    public NodeModuleProviderImpl() {}

    public static NodeModuleProviderImpl fromJar(Class<?> klass, String destDir) {
        String prefix = "META-INF/node_modules";

        URI jarURI = null;
        try {
            jarURI = klass.getClassLoader().getResource(prefix).toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        return new NodeModuleProviderImpl(destDir, jarURI);

    }

    public NodeModuleProviderImpl(String destDir, URI jarURI) {
        JarURIHelper jarURIHelper;
        File destDirFile;
        try {
            jarURIHelper = new JarURIHelper(jarURI);
            destDirFile = new File(destDir);
            jarURIHelper.copyToFolder(destDirFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        FilenameFilter filenameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return file.isDirectory();
            }
        };

        File destDirMetaInfNodeModulesFile = new File(new File(destDirFile, "META-INF"), "node_modules");

        for (File filePath : destDirMetaInfNodeModulesFile.listFiles(filenameFilter)) {
            nodeModules.add(NodeModuleImplBuilder.fromFolder(filePath.getAbsolutePath()));
        }
    }

    public NodeModuleProviderImpl(Class<?> klass, String destDir, String... nodeModulesNames) {
        String prefix = "META-INF/node_modules";

        for (String nodeModuleName : nodeModulesNames) {
            nodeModules.add(NodeModuleImplBuilder.fromJarOrFile(klass, prefix + "/" + nodeModuleName, destDir));
        }

    }

    public <T extends NodeModule> NodeModuleProviderImpl(T... nodeModules) {
        Collections.addAll(this.nodeModules, nodeModules);
    }

    public NodeModuleProviderImpl(List<? extends NodeModule> nodeModules) {
        this.nodeModules.addAll(nodeModules);
    }

    @Override
    public Collection<? extends NodeModule> getModules() {
        return this.nodeModules;
    }
}
