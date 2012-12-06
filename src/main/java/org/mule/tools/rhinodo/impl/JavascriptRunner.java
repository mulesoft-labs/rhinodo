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
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mule.tools.rhinodo.api.ConsoleFactory;
import org.mule.tools.rhinodo.api.NodeModuleFactory;
import org.mule.tools.rhinodo.api.Runnable;
import org.mule.tools.rhinodo.main.Main;
import org.mule.tools.rhinodo.rhino.NodeJsGlobal;
import org.mule.tools.rhinodo.rhino.NodeRequireBuilder;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;

public class JavascriptRunner {

    private URI env;
    private NodeModuleFactory nodeModuleFactory;
    private org.mule.tools.rhinodo.api.Runnable runnable;
    private final Queue<Function> asyncFunctionQueue = new LinkedList<Function>();
    private ConsoleFactory consoleFactory;

    public Scriptable getConsole() {
        return console;
    }

    private Scriptable console;

    public static JavascriptRunner withConsoleFactory(ConsoleFactory consoleFactory,
                                                      NodeModuleFactoryImpl nodeModuleFactory,
                                                      Runnable runnable,
                                                      String destDir) {
        JavascriptRunner javascriptRunner = new JavascriptRunner(nodeModuleFactory, runnable, destDir);
        javascriptRunner.consoleFactory = consoleFactory;
        return javascriptRunner;
    }

    public JavascriptRunner(Runnable mainClass, File file) {
        this(mainClass, file.toString());
    }

    public JavascriptRunner(Runnable runnable,
                            String destDir) {
        env = getURIFromResources(this.getClass(),"META-INF/env");
        this.nodeModuleFactory = new PrimitiveNodeModuleFactory(env, new EmptyNodeModuleFactoryImpl(), destDir);
        this.runnable = runnable;
        this.consoleFactory = new WrappingConsoleFactory(new SystemOutConsole());
    }

    public JavascriptRunner(NodeModuleFactoryImpl nodeModuleFactory,
                            Runnable runnable,
                            String destDir) {
        env = getURIFromResources(this.getClass(),"META-INF/env");
        this.nodeModuleFactory = new PrimitiveNodeModuleFactory(env, nodeModuleFactory, destDir);
        this.runnable = runnable;
        this.consoleFactory = new WrappingConsoleFactory(new SystemOutConsole());
    }

    public void run()  {
        final NodeJsGlobal global = new NodeJsGlobal();

        Context ctx = Context.enter();
        ctx.setOptimizationLevel(9);
        ctx.setLanguageVersion(170);

        global.initStandardObjects(ctx,false);

        try {
            global.installNodeJsRequire(ctx, nodeModuleFactory, new NodeRequireBuilder(asyncFunctionQueue), false);

            addConsole(global);

            NativeObject process = new NativeObject();
            process.put("platform", process, Context.toString("darwin"));
            process.put("env",  process, new NativeObject());
            global.put("process", global, process);

            runnable.executeJavascript(ctx, global);

            Function asyncToExecute;
            while ( (asyncToExecute = asyncFunctionQueue.poll()) != null ) {
                asyncToExecute.call(ctx,global,global,new Object[] {});
            }

        } finally {
            Context.exit();
        }
    }

    private void addConsole(NodeJsGlobal global) {
        this.console = consoleFactory.getConsoleAsScriptable();
        global.put("console", global, this.console);
    }

    public static URI getURIFromResources(Class<?> klass, String path) {
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

}
