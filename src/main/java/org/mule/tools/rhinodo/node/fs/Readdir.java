/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.node.fs;

import org.mozilla.javascript.*;

import java.io.File;
import java.util.Queue;

public class Readdir extends BaseFunction {
    private Queue<Function> asyncCallbacksQueue;

    public Readdir(Queue<Function> asyncCallbacksQueue) {
        this.asyncCallbacksQueue = asyncCallbacksQueue;
    }

    @Override
    public Object call(final Context cx,final Scriptable scope, final Scriptable thisObj, Object[] args) {
        if ( args.length != 2) {
            throw new RuntimeException("Only readdir 2 parameters supported");
        }

        final File file = new File(Context.toString(args[0]));
        final Function callback = (Function) (args[1]);

        String[] lst = file.list();
        final Scriptable newLst = cx.newArray(scope, 0);

        if ( lst == null ) {
            throw new IllegalArgumentException("Error listing directory: " + file);
        }

        Function push = ScriptableObject.getTypedProperty(newLst, "push", Function.class);

        for (String aLst : lst) {
            push.call(cx,scope,newLst, new Object[] {Context.javaToJS(aLst, scope)});
        }

        if (callback != null) {
            asyncCallbacksQueue.add(new BaseFunction() {

                @Override
                public Object call(Context cx2, Scriptable scope2, Scriptable thisObj2, Object[] args2) {
                    return callback.call(cx, scope, thisObj, new Object[] {null, newLst});
                }
            });
        }

        return Undefined.instance;
    }
}
