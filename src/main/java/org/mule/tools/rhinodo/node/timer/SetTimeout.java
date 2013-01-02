package org.mule.tools.rhinodo.node.timer;

import org.mozilla.javascript.*;

import java.util.Queue;

public class SetTimeout extends BaseFunction {

    private final Queue<Function> asyncCallbacksQueue;

    public SetTimeout(Queue<Function> asyncCallbacksQueue) {
        this.asyncCallbacksQueue = asyncCallbacksQueue;
    }

    @Override
    public Object call(final Context cx, final Scriptable scope, final Scriptable thisObj, final Object[] args) {
        final Function callback = (Function) args[0];
        final Long millisToWait = (Long) Context.jsToJava(args[1], Long.class);
        final long startTime = System.currentTimeMillis();
        final Timer timer = new Timer();

        if (callback != null) {
            final BaseFunction timerFunction = new BaseFunction() {
                @Override
                public Object call(Context cx2, Scriptable scope2, Scriptable thisObj2, Object[] args2) {
                    if (System.currentTimeMillis() >= startTime + millisToWait) {
                        if ( timer.shouldExecute() ) {
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
