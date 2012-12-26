/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.node.child_process;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mule.tools.rhinodo.node.AbstractNativeModule;

import java.util.Queue;

public class ChildProcessNativeModule extends AbstractNativeModule {

    public ChildProcessNativeModule(Queue<Function> asyncFunctionQueue) {
        super(asyncFunctionQueue);
    }

    @Override
    public String getId() {
        return "child_process";
    }

    @Override
    protected void populateModule(Scriptable module, Queue<Function> asyncFunctionQueue) {
        ScriptableObject.putProperty(module, "exec", new Exec(asyncFunctionQueue));
    }
}
