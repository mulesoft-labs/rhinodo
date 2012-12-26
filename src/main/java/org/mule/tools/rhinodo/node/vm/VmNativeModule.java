/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.node.vm;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mule.tools.rhinodo.node.AbstractNativeModule;
import org.mozilla.javascript.ScriptableObject;

import java.util.Queue;

public class VmNativeModule extends AbstractNativeModule {

    public VmNativeModule(Queue<Function> asyncFunctionQueue) {
        super(asyncFunctionQueue);
    }

    @Override
    public String getId() {
        return "vm";
    }

    @Override
    protected void populateModule(Scriptable module, Queue<Function> asyncFunctionQueue) {
        ScriptableObject.putProperty(module, "createContext", new CreateContext(asyncFunctionQueue));
        ScriptableObject.putProperty(module, "runInContext", new RunInContext(asyncFunctionQueue));
    }
}
