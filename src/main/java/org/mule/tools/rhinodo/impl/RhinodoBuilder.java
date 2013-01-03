/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.impl;

import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mule.tools.rhinodo.api.ConsoleFactory;
import org.mule.tools.rhinodo.api.NodeModuleFactory;
import org.mule.tools.rhinodo.impl.console.SystemOutConsole;
import org.mule.tools.rhinodo.impl.console.WrappingConsoleFactory;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

public class RhinodoBuilder {
    private ConsoleFactory consoleFactory = new WrappingConsoleFactory(new SystemOutConsole());
    private NodeModuleFactory nodeModuleFactory = new NodeModuleFactoryImpl();
    private File destDir;
    private ContextFactory contextFactory = new ContextFactory();
    private boolean debug = false;
    private Map<String,String> env = System.getenv();

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

    public RhinodoBuilder context(ContextFactory contextFactory) {
        this.contextFactory = contextFactory;
        return this;
    }

    public RhinodoBuilder debug(boolean debug) {
        this.debug = debug;
        return this;
    }

    public RhinodoBuilder destDir(File destDir)  {
        this.destDir = destDir;
        return this;
    }

    public RhinodoBuilder env(Map<String,String> env) {
        this.env = env;
        return this;
    }

    public Rhinodo build(Function callback) {

        if ( this.destDir == null ) {
            String userHome = System.getProperty("user.home");
            this.destDir = new File(userHome, ".rhinodo");
        }

        this.nodeModuleFactory = new PrimitiveNodeModuleFactory(
                JavascriptResource.copyFromJarAndCreate(getURIFromResources(this.getClass(),"META-INF/env"), destDir),
                nodeModuleFactory);
        return new Rhinodo(this.consoleFactory, this.nodeModuleFactory, this.contextFactory, env, callback, debug);
    }

}
