/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.impl;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mule.tools.rhinodo.api.ConsoleFactory;
import org.mule.tools.rhinodo.api.NodeModuleFactory;
import org.mule.tools.rhinodo.impl.console.SystemOutConsole;
import org.mule.tools.rhinodo.impl.console.WrappingConsoleFactory;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class RhinodoBuilder {
    private ConsoleFactory consoleFactory = new WrappingConsoleFactory(new SystemOutConsole());
    private NodeModuleFactory nodeModuleFactory = new NodeModuleFactoryImpl();
    private Context context;
    private File destDir;

    private static URI getURIFromResources(Class<?> klass, String path) {
        ClassLoader classLoader = klass.getClassLoader();
        URL root = classLoader.getResource(path);

        if ( root == null ) {
            throw new IllegalStateException("root cannot be null");
        }

        try {
            return root.toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    RhinodoBuilder() {
    }

    public RhinodoBuilder consoleFactory(ConsoleFactory consoleFactory) {
        this.consoleFactory = consoleFactory;
        return this;
    }

    public RhinodoBuilder moduleFactory(NodeModuleFactory nodeModuleFactory) {
        this.nodeModuleFactory = nodeModuleFactory;
        return this;
    }

    public RhinodoBuilder context(Context context) {
        this.context = context;
        return this;
    }

    public RhinodoBuilder destDir(File destDir)  {
        this.destDir = destDir;
        return this;
    }

    public Rhinodo build(Function callback) {

        if ( this.destDir == null ) {
            String userHome = System.getProperty("user.home");
            this.destDir = new File(userHome, ".rhinodo");
        }

        context = Context.enter();
        context.setOptimizationLevel(9);
        context.setLanguageVersion(Context.VERSION_1_8);
        this.nodeModuleFactory = new PrimitiveNodeModuleFactory(
                JavascriptResource.copyFromJarAndCreate(getURIFromResources(this.getClass(),"META-INF/env"), destDir),
                nodeModuleFactory);
        return new Rhinodo(this.consoleFactory, this.nodeModuleFactory, this.context, callback);
    }

}
