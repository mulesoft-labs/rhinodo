/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.node.timer;

import org.mozilla.javascript.*;

import java.util.Queue;

public class SetInterval extends BaseFunction {

    private final Queue<Function> asyncCallbacksQueue;

    public SetInterval(Queue<Function> asyncCallbacksQueue) {
        this.asyncCallbacksQueue = asyncCallbacksQueue;
    }

    @Override
    public Object call(final Context cx, final Scriptable scope, final Scriptable thisObj, final Object[] args) {
        final Function callback = (Function) args[0];
        final Long millisBetween = (Long) Context.jsToJava(args[1], Long.class);
        final long startTime = System.currentTimeMillis();
        final Timer timer = new Timer();

        if (callback != null) {
            final BaseFunction timerFunction = new BaseFunction() {

                private long times = 1;

                @Override
                public Object call(Context cx2, Scriptable scope2, Scriptable thisObj2, Object[] args2) {
                    if (System.currentTimeMillis() >= startTime + millisBetween * times ) {
                        if ( timer.shouldExecute() ) {
                            asyncCallbacksQueue.add(this);
                            times++;
                            return callback.call(cx, scope, thisObj, args);
                        } else {
                            return Undefined.instance;
                        }
                    }

                    asyncCallbacksQueue.add(this);
                    return Undefined.instance;
                }
            };
            asyncCallbacksQueue.add(timerFunction);
        }

        return timer;
    }
}
