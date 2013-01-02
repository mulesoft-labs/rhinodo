package org.mule.tools.rhinodo.node.timer;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;


public class ClearTimeout extends BaseFunction {
    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        final Timer timer = (Timer) args[0];
        timer.setShouldExecute(false);
        return Undefined.instance;
    }
}
