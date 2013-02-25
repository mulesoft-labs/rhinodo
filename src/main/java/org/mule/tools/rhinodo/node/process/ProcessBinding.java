/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.node.process;

import org.mozilla.javascript.*;
import org.mule.tools.rhinodo.node.vm.RunInContext;
import org.mule.tools.rhinodo.node.vm.RunInNewContext;

import java.util.Queue;

class ProcessBinding extends BaseFunction {
    private final Queue<Function> asyncFunctionQueue;

    public ProcessBinding(Queue<Function> asyncFunctionQueue) {
        this.asyncFunctionQueue = asyncFunctionQueue;
    }

    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        String s = Context.toString(args[0]);
        if (s.equals("buffer")) {
            Scriptable buffer = cx.newObject(scope);
            Scriptable slowBuffer = new BaseFunction();
            ScriptableObject.putProperty(slowBuffer, "makeFastBuffer", new BaseFunction());
            slowBuffer.setPrototype(new BaseFunction());
            ScriptableObject.putProperty(slowBuffer, "prototype", slowBuffer.getPrototype());
            ScriptableObject.putProperty(buffer, "SlowBuffer", slowBuffer);
            return buffer;
        } else if (s.equals("evals")) {
            Scriptable nodeScript = cx.newObject(scope);
            ScriptableObject.putProperty(nodeScript, "runInThisContext", new RunInContext(asyncFunctionQueue));
            ScriptableObject.putProperty(nodeScript, "runInNewContext", new RunInNewContext(asyncFunctionQueue));

            Scriptable evals = cx.newObject(scope);
            ScriptableObject.putProperty(evals, "NodeScript", nodeScript);
            return evals;
        } else {
            throw new RuntimeException("Not implemented: process.binding for " + s + " does not exist.");
        }
    }
}
