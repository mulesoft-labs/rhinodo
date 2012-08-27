package org.mule.tools.rhinodo.node.fs;

import org.apache.commons.io.IOUtils;
import org.mozilla.javascript.*;
import org.mule.tools.rhinodo.impl.JavascriptRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Queue;

public class ReadFile extends BaseFunction {
    private Queue<Function> asyncCallbacksQueue;

    public ReadFile(Queue<Function> asyncCallbacksQueue) {
        this.asyncCallbacksQueue = asyncCallbacksQueue;
    }

    @Override
    public Object call(final Context cx,final Scriptable scope, final Scriptable thisObj, Object[] args) {
        if ( args.length != 3) {
            throw new RuntimeException("Only readFile with 3 parameters supported");
        }

        String file = (String) args[0];
        String encoding = (String) args[1];
        final Function callback = (Function) args[2];

        List<String> lines = null;
        try {
            lines = IOUtils.readLines(new FileInputStream(new File(file)), encoding);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final StringBuilder sb = new StringBuilder();
        int i = 0;

        for (i = 0; i < lines.size(); i++ ) {
            if ( i == 0 ) {
                sb.append(lines.get(i));
            } else {
                sb.append('\n').append(lines.get(i));
            }
        }

        if (callback != null) {
            asyncCallbacksQueue.add(new BaseFunction() {

                @Override
                public Object call(Context cx2, Scriptable scope2, Scriptable thisObj2, Object[] args2) {
                    return callback.call(cx, scope, thisObj, new Object[] {null, sb.toString()});
                }
            });
        }

        return Undefined.instance;
    }
}
