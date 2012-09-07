/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.rhino;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.commonjs.module.ModuleScriptProvider;
import org.mozilla.javascript.commonjs.module.Require;
import org.mozilla.javascript.commonjs.module.RequireBuilder;

import java.util.Queue;

public class NodeRequireBuilder extends RequireBuilder {

    private boolean sandboxed = true;
    private ModuleScriptProvider moduleScriptProvider;
    private Script preExec;
    private Script postExec;
    private Queue<Function> asyncCallbackQueue;

    public NodeRequireBuilder(Queue<Function> asyncCallbackQueue) {
        this.asyncCallbackQueue = asyncCallbackQueue;
    }

    public RequireBuilder setModuleScriptProvider(
            ModuleScriptProvider moduleScriptProvider)
    {
        this.moduleScriptProvider = moduleScriptProvider;
        return this;
    }

    public RequireBuilder setPostExec(Script postExec) {
        this.postExec = postExec;
        return this;
    }

    public RequireBuilder setPreExec(Script preExec) {
        this.preExec = preExec;
        return this;
    }

    public RequireBuilder setSandboxed(boolean sandboxed) {
        this.sandboxed = sandboxed;
        return this;
    }

    public Require createRequire(Context cx, Scriptable globalScope) {
        return new NodeRequire(asyncCallbackQueue, cx, globalScope, moduleScriptProvider, preExec,
                postExec, sandboxed);
    }
}
