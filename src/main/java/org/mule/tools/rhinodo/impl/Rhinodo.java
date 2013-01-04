/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.impl;

import org.mozilla.javascript.*;
import org.mozilla.javascript.tools.debugger.Main;
import org.mule.tools.rhinodo.api.ConsoleProvider;
import org.mule.tools.rhinodo.api.NodeModuleProvider;
import org.mule.tools.rhinodo.rhino.NodeJsGlobal;
import org.mule.tools.rhinodo.rhino.NodeRequireBuilder;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class Rhinodo {

    public static final int DEBUG_WINDOW_WIDTH = 1200;
    public static final int DEBUG_WINDOW_HEIGHT = 920;
    private final Queue<Function> asyncFunctionQueue = new LinkedList<Function>();

    public static RhinodoBuilder create() {
        return new RhinodoBuilder();
    }

    Rhinodo(final ConsoleProvider consoleProvider,
            final NodeModuleProvider nodeModuleProvider,
            final ContextFactory contextFactory,
            final Map<String, String> env,
            final Function callback,
            final boolean debug) {

        final NodeJsGlobal global = new NodeJsGlobal();
        final Main main  = debug ? doDebug(contextFactory, global) : null;

        contextFactory.call(new ContextAction() {
            @Override
            public Object run(Context ctx) {
                ctx.setOptimizationLevel(debug ? -1 : 9);
                ctx.setLanguageVersion(Context.VERSION_1_8);
                global.initStandardObjects(ctx, false);

                if ( debug ) {

                    ScriptableObject.putProperty(global, "strikeThePose", new BaseFunction() {
                        @Override
                        public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                            main.doBreak();
                            return Undefined.instance;
                        }
                    });

                }

                ExitCallbackExecutor exitCallbackExecutor = new ExitCallbackExecutor();

                Scriptable envAsScriptable = mapToScriptable(ctx, global, env);

                global.installNodeJsRequire(ctx, envAsScriptable, nodeModuleProvider,
                        new NodeRequireBuilder(asyncFunctionQueue, exitCallbackExecutor), false);

                Scriptable console = consoleProvider.getConsoleAsScriptable(global);
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

                return Undefined.instance;
            }
        });

    }

    private Scriptable mapToScriptable(Context ctx, NodeJsGlobal global, Map<String, String> env) {
        Scriptable envAsScriptable = ctx.newObject(global);

        for (Map.Entry<String, String> stringStringEntry : env.entrySet()) {
            ScriptableObject.putProperty(envAsScriptable, stringStringEntry.getKey(),
                    Context.javaToJS(stringStringEntry.getValue(),global));
        }
        return envAsScriptable;
    }

    private Main doDebug(ContextFactory contextFactory, NodeJsGlobal global) {
        final Main main = new Main("Rhino JavaScript Debugger");

        main.attachTo(contextFactory);

        main.setScope(global);

        main.pack();
        main.setSize(DEBUG_WINDOW_WIDTH, DEBUG_WINDOW_HEIGHT);
        main.setVisible(true);

        return main;
    }

}
