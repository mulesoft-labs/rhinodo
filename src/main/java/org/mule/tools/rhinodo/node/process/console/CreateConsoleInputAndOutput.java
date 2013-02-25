/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.node.process.console;

import org.mozilla.javascript.*;
import org.mule.tools.rhinodo.node.process.ProcessNativeModule;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Queue;


public class CreateConsoleInputAndOutput extends BaseFunction {
    private Context context;
    private final Scriptable module;
    private Scriptable scope;
    private final Queue<Function> asyncFunctionQueue;

    public CreateConsoleInputAndOutput(Context context,
                                       Scriptable module, Scriptable scope, Queue<Function> asyncFunctionQueue) {
        this.context = context;
        this.module = module;
        this.scope = scope;
        this.asyncFunctionQueue = asyncFunctionQueue;
    }

    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {

        // TODO Remove this hack, it's horrible but works
        cx.evaluateString(scope,
                "var x = require('events').EventEmitter;" +
                        "function _events () {" +
                        "x.call(this);" +
                        "}" +
                        "require('util').inherits(_events, x);", "event_emitter_ctor", -1, null);

        Scriptable stdout = cx.newObject(scope, "_events");

        BaseFunction write = new WriteFunction();

        ScriptableObject.putProperty(stdout, "write", write);
        makeEventful(stdout);
        ScriptableObject.putProperty(module, "stdout", stdout);

        Scriptable stderr = cx.newObject(scope, "_events");
        ScriptableObject.putProperty(stderr, "write", write);
        makeEventful(stderr);
        ScriptableObject.putProperty(module, "stderr", stderr);

        Scriptable stdin = cx.newObject(scope, "_events");
        final boolean [] shouldIPause = new boolean[1];
        shouldIPause[0] = false;
        BaseFunction pause = new PauseFunction(shouldIPause);

        final InputStream in;
        try {
            in = this.extract(System.in);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        BaseFunction resume = new ResumeFunction(in, shouldIPause, asyncFunctionQueue);
        ScriptableObject.putProperty(stdin, "resume", resume);
        ScriptableObject.putProperty(stdin, "pause", pause);
        makeEventful(stdin);
        ScriptableObject.putProperty(module, "stdin", stdin);

        // Remove event's trick dirt
        ScriptableObject.putProperty(scope, "x", Undefined.instance);
        ScriptableObject.putProperty(scope, "_events", Undefined.instance);

        return Undefined.instance;

    }

    private void makeEventful(Scriptable stream) {
        Function require = ScriptableObject.getTypedProperty(scope, "require", Function.class);
        Scriptable events = (Scriptable) require.call(context, scope, scope, new Object[]{"events"});
        Scriptable utils = (Scriptable) require.call(context, scope, scope, new Object[]{"util"});

        Function inherits = ScriptableObject.getTypedProperty(utils, "inherits", Function.class);

        Function eventEmitter = ScriptableObject.getTypedProperty(events, "EventEmitter", Function.class);
        inherits.call(context,scope,scope,new Object[]{stream, eventEmitter});
    }

    /*
      Unravels all layers of FilterInputStream wrappers to get to the
      core InputStream
     */
    public static InputStream extract(InputStream in)
            throws NoSuchFieldException, IllegalAccessException {

        Field f = FilterInputStream.class.getDeclaredField("in");
        f.setAccessible(true);

        while( in instanceof FilterInputStream )
            in = (InputStream)f.get(in);

        return in;
    }
}
