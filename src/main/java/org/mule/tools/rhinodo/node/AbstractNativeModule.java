/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.node;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mule.tools.rhinodo.api.NativeModule;

import java.util.Queue;

public abstract class AbstractNativeModule implements NativeModule {
    private Scriptable module;
    private final Queue<Function> asyncFunctionQueue;
    private Context context;
    private Scriptable scope;

    public AbstractNativeModule(final Queue<Function> asyncFunctionQueue) {
        this.asyncFunctionQueue = asyncFunctionQueue;
    }

    @Override
    public Scriptable getModule(Context context, Scriptable scope) {
        if ( module != null ) {
            return module;
        }

        this.context = context;
        this.scope = scope;
        this.module = context.newObject(scope);
        populateModule(module, asyncFunctionQueue);
        return module;
    }

    protected Scriptable getScope() {
        return scope;
    }

    protected Context getContext() {
        return context;
    }

    protected Queue<Function> getAsyncFunctionQueue() {
        return asyncFunctionQueue;
    }

    protected abstract void populateModule(final Scriptable module, final Queue<Function> asyncFunctionQueue);
}
