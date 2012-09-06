package org.mule.tools.rhinodo.node.fs;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrappedException;
import sun.org.mozilla.javascript.internal.Undefined;

import java.io.File;

public class StatSync extends BaseFunction {
    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        if ( !new File(Context.toString(args[0])).exists() ) {
            throw new WrappedException(new RuntimeException(String.format("Error: file [%s] does not exist", args[0])));
        }
        return Undefined.instance;
    }
}
