/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.node.vm;

import org.mozilla.javascript.*;

import java.util.Queue;

public class RunInNewContext extends BaseFunction {
    private final Queue<Function> asyncFunctionQueue;

    public RunInNewContext(Queue<Function> asyncFunctionQueue) {
        this.asyncFunctionQueue = asyncFunctionQueue;
    }

    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        if (args.length != 2) {
            throw new IllegalArgumentException("Invalid parameter count");
        }
        ScriptableObject context = (ScriptableObject) new CreateContext(asyncFunctionQueue)
                .call(cx, scope, thisObj, new Object[]{args[1]});

        return new RunInContext(asyncFunctionQueue)
                .call(cx,scope,thisObj,new Object[]{args[0], context});
    }
}
