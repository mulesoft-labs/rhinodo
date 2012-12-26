/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.node.process;

import org.mozilla.javascript.*;

import java.util.Queue;

public class NextTick extends BaseFunction {
    private final Queue<Function> asyncCallbacksQueue;

    public NextTick(Queue<Function> asyncCallbacksQueue) {
        this.asyncCallbacksQueue = asyncCallbacksQueue;
    }

    @Override
    public Object getDefaultValue(Class<?> typeHint) {
        return "nextTick function";
    }

    @Override
    public Object call(final Context cx, final Scriptable scope, final Scriptable thisObj, Object[] args) {

        final Function callback = (Function) args[0];

        if (callback != null ) {
            asyncCallbacksQueue.add(new BaseFunction() {
                @Override
                public Object call(Context cx2, Scriptable scope2, Scriptable thisObj2, Object[] args2) {
                    return callback.call(cx, scope, thisObj, new Object[] {});
                }
            });
        }

        return Undefined.instance;
    }
}
