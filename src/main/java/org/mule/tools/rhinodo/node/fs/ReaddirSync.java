package org.mule.tools.rhinodo.node.fs;

import org.mozilla.javascript.*;

import java.io.File;

public class ReaddirSync extends BaseFunction {
    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        if ( args.length != 1) {
            throw new RuntimeException("Only readdirSync with 1 parameter supported");
        }

        String dir = args[0].toString();

        File[] lst=new File(dir).listFiles();

        Scriptable newLst = cx.newArray(scope, 0);

        for (File aLst : lst) {
            Function push = (Function) newLst.getPrototype().get("push", newLst);
            push.call(cx,scope,newLst, new Object[] {aLst.getName()});
        }

        return newLst;

    }
}
