/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.node.fs;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

import java.util.Queue;

public class CreateReadStream extends BaseFunction {
    private final Queue<Function> asyncFunctionQueue;

    public CreateReadStream(final Queue<Function> asyncFunctionQueue) {
        this.asyncFunctionQueue = asyncFunctionQueue;
    }

    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        if ( args.length != 1) {
            throw new RuntimeException("Only createReadStream with 1 parameter supported");
        }

        Object file = args[0];
        return Context.javaToJS(new ReadStream(asyncFunctionQueue, Context.toString(file)), scope);
    }
}
