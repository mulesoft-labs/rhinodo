package org.mule.tools.rhinodo.rhino;

import org.mozilla.javascript.*;
import org.mozilla.javascript.commonjs.module.ModuleScriptProvider;
import org.mozilla.javascript.commonjs.module.Require;
import org.mule.tools.rhinodo.node.fs.ReadFile;
import org.mule.tools.rhinodo.node.fs.ReaddirSync;
import org.mule.tools.rhinodo.node.fs.StatSync;

import java.util.Queue;

public class NodeRequire extends Require {
    private NativeObject fs;
    private Queue<Function> asyncCallbacksQueue;

    public NodeRequire(Queue<Function> asyncCallbacksQueue, Context cx, Scriptable globalScope, ModuleScriptProvider moduleScriptProvider, Script preExec, Script postExec, boolean sandboxed) {
        super(cx,globalScope,moduleScriptProvider,preExec,postExec,sandboxed);
        this.asyncCallbacksQueue = asyncCallbacksQueue;
    }

    private Object importFs(Object[] args) {
        fs = new NativeObject();

        String id = (String)Context.jsToJava(args[0], String.class);

        if ( id.equals("fs") ) {
            fs.put("readFile", fs, new ReadFile(asyncCallbacksQueue));
            fs.put("readdirSync", fs, new ReaddirSync());
            fs.put("statSync", fs, new StatSync());
        }

        return fs;
    }

    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        if (!args[0].equals("fs")) {
            return super.call(cx, scope, thisObj, args);
        }

        return importFs(args);
    }
}
