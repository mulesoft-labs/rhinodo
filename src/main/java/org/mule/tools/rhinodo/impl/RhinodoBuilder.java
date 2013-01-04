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
import org.mule.tools.rhinodo.api.ConsoleProvider;
import org.mule.tools.rhinodo.api.NodeModuleProvider;
import org.mule.tools.rhinodo.impl.console.SystemOutConsole;
import org.mule.tools.rhinodo.impl.console.WrappingConsoleProvider;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

public class RhinodoBuilder {
    private ConsoleProvider consoleProvider = new WrappingConsoleProvider(new SystemOutConsole());
    private NodeModuleProvider nodeModuleProvider = new NodeModuleProviderImpl();
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

    public RhinodoBuilder consoleFactory(ConsoleProvider consoleProvider) {
        this.consoleProvider = consoleProvider;
        return this;
    }

    public RhinodoBuilder moduleFactory(NodeModuleProvider nodeModuleProvider) {
        this.nodeModuleProvider = nodeModuleProvider;
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

        this.nodeModuleProvider = new PrimitiveNodeModuleProvider(
                JavascriptResource.copyFromJarAndCreate(getURIFromResources(this.getClass(),"META-INF/env"), destDir),
                nodeModuleProvider);
        return new Rhinodo(this.consoleProvider, this.nodeModuleProvider, this.contextFactory, env, callback, debug);
    }

}
