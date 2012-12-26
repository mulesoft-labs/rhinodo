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

public class Mkdir extends BaseFunction {
    private Queue<Function> asyncCallbacksQueue;

    public Mkdir(Queue< Function > asyncCallbacksQueue) {
        this.asyncCallbacksQueue = asyncCallbacksQueue;
    }

    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        if ( args.length == 3) {
            final String file = Context.toString(args[0]);
            //TODO Implement mode
//            String mode = Context.toString(args[1]);
            final Function callback = (Function) args[2];

            return mkdir(cx, scope, thisObj, file, callback);
        } else {
            throw new RuntimeException("Only writeFile with 3 parameters supported");
        }
    }

    private Object mkdir(final Context cx, final Scriptable scope, final Scriptable thisObj, String file,
                         final Function callback) {

        new File(file).mkdir();

        if (callback != null) {
            asyncCallbacksQueue.add(new BaseFunction() {

                @Override
                public Object call(Context cx2, Scriptable scope2, Scriptable thisObj2, Object[] args2) {
                    return callback.call(cx, scope, thisObj, new Object[] {null});
                }
            });
        }

        return Undefined.instance;
    }
}
