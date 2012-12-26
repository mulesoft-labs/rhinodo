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

public class WatchFile extends BaseFunction {
    private Queue<Function> asyncCallbacksQueue;

    public WatchFile(Queue<Function> asyncCallbacksQueue) {
        this.asyncCallbacksQueue = asyncCallbacksQueue;
    }

    @Override
    public Object call(final Context cx,final Scriptable scope, final Scriptable thisObj, Object[] args) {
        if (args.length != 3) {
            throw new IllegalArgumentException();
        }

        final String item = (String) Context.jsToJava(args[0], String.class);
        Scriptable options = (Scriptable) args[1];
        final Function callback = (Function) args[2];

        if ( callback != null ) {
            asyncCallbacksQueue.add(new BaseFunction() {
                @Override
                public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {

                    Scriptable curr = cx.newObject(scope);
                    Scriptable prev = cx.newObject(scope);

                    long mtime = new File(item).lastModified();

                    Object date = cx.evaluateString(scope, "new Date(" + mtime + ");", "date", 0, null);

                    ScriptableObject.putProperty(prev, "mtime", date);
                    ScriptableObject.putProperty(curr, "mtime", date);

                    callback.call(cx,scope,thisObj, new Object[]{prev,curr});
                    return Undefined.instance;
                }
            });
        }

        return Undefined.instance;
    }
}
