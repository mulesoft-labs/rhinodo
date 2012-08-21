package org.mule.tools.rhinodo.impl;

import org.mule.tools.rhinodo.api.NodeModule;
import org.mule.tools.rhinodo.api.NodeModuleFactory;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PrimitiveNodeModuleFactory implements NodeModuleFactory {

    private NodeModuleFactory nodeModuleFactory;
    private List<NodeModuleImpl> nodeModuleList;

    public PrimitiveNodeModuleFactory(URI env, NodeModuleFactory nodeModuleFactory) {
        this.nodeModuleFactory = nodeModuleFactory;
        nodeModuleList = new ArrayList<NodeModuleImpl>();
        String path = env.getPath();
        File file1 = new File(path);
        File[] files = file1.listFiles();
        if ( files == null) {
            throw new IllegalArgumentException();
        }
        for (File file : files) {
            String fileName = file.getName();
            if ( fileName.endsWith(".js") ) {
                String moduleName = fileName.substring(0,fileName.lastIndexOf(".js"));
                nodeModuleList.add(new NodeModuleImpl(moduleName, file.toURI()));
            }
        }
    }

    @Override
    public Collection<? extends NodeModule> getModules() {
        ArrayList<NodeModule> nodeModules = new ArrayList<NodeModule>(nodeModuleList);
        nodeModules.addAll(nodeModuleFactory.getModules());
        return nodeModules;
    }
}
