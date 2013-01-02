/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.impl;

import org.mozilla.javascript.*;
import org.mule.tools.rhinodo.api.ConsoleFactory;
import org.mule.tools.rhinodo.api.NodeModuleFactory;
import org.mule.tools.rhinodo.rhino.NodeJsGlobal;
import org.mule.tools.rhinodo.rhino.NodeRequireBuilder;

import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

public class Rhinodo {

    private final NodeModuleFactory nodeModuleFactory;
    private final Queue<Function> asyncFunctionQueue = new LinkedList<Function>();
    private final ConsoleFactory consoleFactory;
    private final Context ctx;
    private final Function callback;

    public static RhinodoBuilder create() {
        return new RhinodoBuilder();
    }

    Rhinodo(ConsoleFactory consoleFactory, NodeModuleFactory nodeModuleFactory, Context context, Function callback) {
        this.consoleFactory = consoleFactory;
        this.nodeModuleFactory = nodeModuleFactory;
        this.ctx = context;
        this.callback = callback;

        NodeJsGlobal global = new NodeJsGlobal();

        global.initStandardObjects(ctx, false);

        try {
            ExitCallbackExecutor exitCallbackExecutor = new ExitCallbackExecutor();
            global.installNodeJsRequire(ctx, nodeModuleFactory,
                    new NodeRequireBuilder(asyncFunctionQueue, exitCallbackExecutor), false);

            Scriptable console = consoleFactory.getConsoleAsScriptable(global);
            ScriptableObject.putProperty(global, "console", console);

            callback.call(ctx, global, global, new Object[]{});

            Function asyncToExecute;
            while ( (asyncToExecute = asyncFunctionQueue.poll()) != null ) {
                asyncToExecute.call(ctx,global,global,new Object[] {});
            }

            Function function = exitCallbackExecutor.get();
            if ( function != null ) {
                function.call(ctx, global, global, new Object[]{});
            }

        } finally {
            Context.exit();
        }
    }

}
