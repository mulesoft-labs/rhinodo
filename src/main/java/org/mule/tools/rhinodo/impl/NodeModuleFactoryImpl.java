package org.mule.tools.rhinodo.impl;

import org.mule.tools.rhinodo.api.NodeModule;
import org.mule.tools.rhinodo.api.NodeModuleFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class NodeModuleFactoryImpl implements NodeModuleFactory {

    private List<NodeModule> nodeModules;

    private NodeModuleFactoryImpl() {}

    public <T extends NodeModule> NodeModuleFactoryImpl(T... nodeModules) {
        this.nodeModules = new ArrayList<NodeModule>();
        Collections.addAll(this.nodeModules, nodeModules);
    }

    public NodeModuleFactoryImpl(List<? extends NodeModule> nodeModules) {
        this.nodeModules = new ArrayList<NodeModule>();
        this.nodeModules.addAll(nodeModules);
    }

    @Override
    public Collection<? extends NodeModule> getModules() {
        return this.nodeModules;
    }
}
