/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.node.vm;

import org.mozilla.javascript.*;
import org.mozilla.javascript.tools.shell.Global;
import org.mule.tools.rhinodo.rhino.NodeJsGlobal;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Queue;

public class CreateContext extends BaseFunction {
    private final Queue<Function> asyncFunctionQueue;

    public CreateContext(Queue<Function> asyncFunctionQueue) {
        this.asyncFunctionQueue = asyncFunctionQueue;
    }

    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {

        ScriptableObject initialContents = (ScriptableObject) args[0];
        NativeObject newGlobal = new NativeObject();

        for (Object o : initialContents.getAllIds()) {
            String s = (String) o;
            ScriptableObject.putProperty(newGlobal, s, ScriptableObject.getProperty(initialContents, s));
        }

        return newGlobal;
    }
}
