/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.node.vm;

import org.mozilla.javascript.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Queue;

public class RunInContext extends BaseFunction {
    private final Queue<Function> asyncFunctionQueue;

    public RunInContext(Queue<Function> asyncFunctionQueue) {
        this.asyncFunctionQueue = asyncFunctionQueue;
    }

    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        if ( args.length != 3 && args.length != 2 ) {
            throw new IllegalArgumentException();
        }

        String code = (String) args[0];
        VmContext vmContext = (VmContext) args[1];
        String scriptName = (String) (args[2] == null ? "_RunInConext_NoName" : args[2]);

        Script script = cx.compileString(code, scriptName, 0, null);
        System.out.println("Run: " + scriptName);
        return script.exec(cx, vmContext.getInitialContents());
    }
}
