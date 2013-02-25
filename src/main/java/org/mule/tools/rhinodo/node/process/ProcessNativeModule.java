/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.node.process;

import org.mozilla.javascript.*;
import org.mule.tools.rhinodo.impl.ExitCallbackExecutor;
import org.mule.tools.rhinodo.node.AbstractNativeModule;
import org.mule.tools.rhinodo.node.process.console.CreateConsoleInputAndOutput;

import java.util.Queue;

public class ProcessNativeModule extends AbstractNativeModule {
    private final ExitCallbackExecutor exitCallbackExecutor;
    private final Scriptable env;

    public ProcessNativeModule(final Scriptable env,final Queue<Function> asyncFunctionQueue,
                               ExitCallbackExecutor exitCallbackExecutor) {
        super(asyncFunctionQueue);
        this.env = env;
        this.exitCallbackExecutor = exitCallbackExecutor;
    }

    @Override
    public String getId() {
        return "process";
    }

    @Override
    protected void populateModule(final Scriptable module, final Queue<Function> asyncFunctionQueue) {

        ScriptableObject.putProperty(module, "env", env);

        //TODO Unmock
        ScriptableObject.putProperty(module, "platform", Context.toString("darwin"));

        Scriptable versions = getContext().newObject(getScope());
        ScriptableObject.putProperty(versions, "node", Context.toString("0.8.15"));
        ScriptableObject.putProperty(versions, "v8", Context.toString("3.1.8.26"));
        ScriptableObject.putProperty(versions, "ares", Context.toString("1.7.4"));
        ScriptableObject.putProperty(versions, "ev", Context.toString("4.4"));
        ScriptableObject.putProperty(versions, "openssl", Context.toString("1.0.0e-fips"));
        ScriptableObject.putProperty(module, "versions", versions);

        ScriptableObject.putProperty(module, "binding", new ProcessBinding(asyncFunctionQueue));

        NextTick nextTick = new NextTick(asyncFunctionQueue);

        nextTick.call(getContext(),getScope(),getScope(),new Object[]{
                new CreateConsoleInputAndOutput(getContext(), module, getScope(), asyncFunctionQueue)
        });

        ScriptableObject.putProperty(module, "nextTick", nextTick);
        ScriptableObject.putProperty(module, "cwd", new Cwd());
        ScriptableObject.putProperty(module, "umask", new BaseFunction() {
            @Override
            public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                //TODO Implement me right
                return 0xFF;
            }
        });

        // TODO Make it work with EventEmitter
        ScriptableObject.putProperty(module, "on", new BaseFunction() {
            @Override
            public Object call(final Context cx, final Scriptable scope,
                               final Scriptable thisObj, Object[] args) {
                if (args.length != 2) {
                    throw new IllegalArgumentException("Only 2 parameters supported");
                }

                final Function callback = (Function) args[1];

                if (args[0].equals("exit")) {
                    exitCallbackExecutor.add(new BaseFunction() {
                        @Override
                        public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                            return callback.call(cx, scope, thisObj, new Object[]{0});
                        }
                    });
                }

                return Undefined.instance;
            }
        });
        ScriptableObject.putProperty(module, "exit", new BaseFunction() {
            @Override
            public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                // TODO Implement me right
                return Undefined.instance;
            }
        });

    }

}
