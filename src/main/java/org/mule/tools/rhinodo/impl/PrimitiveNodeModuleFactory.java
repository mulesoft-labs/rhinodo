package org.mule.tools.rhinodo.impl;

import org.mule.tools.rhinodo.api.NodeModule;
import org.mule.tools.rhinodo.api.NodeModuleFactory;
import org.mule.tools.rhinodo.tools.JarURIHelper;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class PrimitiveNodeModuleFactory implements NodeModuleFactory {

    private NodeModuleFactory nodeModuleFactory;
    private List<NodeModuleImpl> nodeModuleList;

    public PrimitiveNodeModuleFactory(URI env, NodeModuleFactory nodeModuleFactory) {
        this.nodeModuleFactory = nodeModuleFactory;
        this.nodeModuleList = new ArrayList<NodeModuleImpl>();

        if (env == null) {
            throw new IllegalArgumentException("env cannot be null");
        }

        if ("file".equals(env.getScheme())) {

            addFileModules(env);
        } else if ("jar".equals(env.getScheme())) {
            addJarModules(env);

        } else {
            throw new IllegalArgumentException(String.format("Error creating PrimitiveNodeModuleFactory: " +
                    "[%s] scheme not recognized.", env.getScheme()));
        }
    }

    private void addFileModules(URI env) {
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

    private void addJarModules(URI env) {
        JarURIHelper jarHelper = new JarURIHelper(env);

        URL jarURL = jarHelper.getJarURL();
        String insideJarRelativePath = jarHelper.getInsideJarRelativePath();
        JarInputStream jarInputStream;
        try {

            jarInputStream = new JarInputStream(jarURL.openStream());

            JarEntry jarEntry = null;
            while( (jarEntry = jarInputStream.getNextJarEntry() ) != null ) {
                if ( jarEntry.getName().startsWith(insideJarRelativePath) ) {
                    if ( jarEntry.getName().endsWith(".js") ) {
                        String moduleName = jarEntry.getName().substring(insideJarRelativePath.length() + 1,jarEntry.getName().lastIndexOf(".js"));
                        nodeModuleList.add(new NodeModuleImpl(moduleName, URI.create("jar:" + jarURL.toString() + "!/" + jarEntry.getName())));
                    }
                }
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public Collection<? extends NodeModule> getModules() {
        ArrayList<NodeModule> nodeModules = new ArrayList<NodeModule>(nodeModuleList);
        nodeModules.addAll(nodeModuleFactory.getModules());
        return nodeModules;
    }
}
