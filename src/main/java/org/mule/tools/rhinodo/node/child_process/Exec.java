/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.node.child_process;

import org.mozilla.javascript.*;

import java.io.IOException;
import java.util.Queue;

public class Exec extends BaseFunction {

    private Queue<Function> asyncCallbacksQueue;

    public Exec(Queue<Function> asyncCallbacksQueue) {
        this.asyncCallbacksQueue = asyncCallbacksQueue;
    }

    @Override
    public Object call(final Context cx, final Scriptable scope, final Scriptable thisObj, Object[] args) {

        final String arg = Context.toString(args[0]);
        final Function callback = (Function) args[1];
        final Process process;
        try {
            process = Runtime.getRuntime().exec(arg);
        } catch (IOException e) {
            if (callback != null) {
                asyncCallbacksQueue.add(new BaseFunction() {

                    @Override
                    public Object call(Context cx2, Scriptable scope2, Scriptable thisObj2, Object[] args2) {
                        return callback.call(cx, scope, thisObj, new Object[] {true, null, null});
                    }
                });
            }

            return Undefined.instance;
        }

        try {
            process.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


        if (callback != null) {
            asyncCallbacksQueue.add(new BaseFunction() {

                @Override
                public Object call(Context cx2, Scriptable scope2, Scriptable thisObj2, Object[] args2) {
                    return callback.call(cx, scope, thisObj, new Object[] {null, null, null});
                }
            });
        }

        return Undefined.instance;
    }
}
