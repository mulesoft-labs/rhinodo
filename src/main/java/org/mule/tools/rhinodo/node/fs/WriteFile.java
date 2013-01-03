/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.node.fs;

import org.apache.commons.io.FileUtils;
import org.mozilla.javascript.*;

import java.io.File;
import java.io.IOException;
import java.util.Queue;

public class WriteFile extends BaseFunction {
    private Queue<Function> asyncCallbacksQueue;

    public WriteFile(Queue<Function> asyncCallbacksQueue) {
        this.asyncCallbacksQueue = asyncCallbacksQueue;
    }

    @Override
    public Object call(final Context cx,final Scriptable scope, final Scriptable thisObj, Object[] args) {
        if ( args.length == 3) {
            final String file = Context.toString(args[0]);
            String data = Context.toString(args[1]);
            final Function callback = (Function) args[2];

            return writeFile(cx, scope, thisObj, file, data, callback);
        } else {
            throw new RuntimeException("Only writeFile with 3 parameters supported");
        }
    }

    private Object writeFile(final Context cx, final Scriptable scope, final Scriptable thisObj, String file,
                             String data, final Function callback) {
        try {
            FileUtils.write(new File(file).getAbsoluteFile(), data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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
