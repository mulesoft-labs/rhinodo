/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.rhino;

import org.apache.commons.io.FilenameUtils;
import org.mozilla.javascript.*;
import org.mozilla.javascript.commonjs.module.ModuleScriptProvider;
import org.mozilla.javascript.commonjs.module.Require;
import org.mule.tools.rhinodo.impl.ExitCallbackExecutor;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Queue;
import java.util.Stack;

public class NodeRequireBuilder {

    private boolean sandboxed = true;
    private ModuleScriptProvider moduleScriptProvider;
    private Queue<Function> asyncCallbackQueue;
    private final Stack<String> moduleLoadStack;
    private final ExitCallbackExecutor exitCallbackExecutor;

    public NodeRequireBuilder(Queue<Function> asyncCallbackQueue,
                              ExitCallbackExecutor exitCallbackExecutor) {
        this.moduleLoadStack = new Stack<String>();
        this.asyncCallbackQueue = asyncCallbackQueue;
        this.exitCallbackExecutor = exitCallbackExecutor;
    }

    public NodeRequireBuilder setModuleScriptProvider(
            ModuleScriptProvider moduleScriptProvider)
    {
        this.moduleScriptProvider = moduleScriptProvider;
        return this;
    }

    public NodeRequireBuilder setSandboxed(boolean sandboxed) {
        this.sandboxed = sandboxed;
        return this;
    }

    public class PreExecNodeRequire implements Script {
        private final Scriptable globalScope;
        private final Stack<String> moduleLoadStack;

        public PreExecNodeRequire(Scriptable globalScope, Stack<String> moduleLoadStack) {
            this.globalScope = globalScope;
            this.moduleLoadStack = moduleLoadStack;
        }

        @Override
        public Object exec(Context cx, Scriptable scope) {
            String fullPath = getModulePath(scope);

            if (fullPath.endsWith("META-INF/env/") ) {
                return Undefined.instance;
            }

//            moduleLoadStack.push(Context.toString(globalScope.get("__dirname", globalScope)));
            globalScope.put("__dirname", scope, fullPath);
            globalScope.put("__filename", scope, fullPath);

            return Undefined.instance;
        }
    }

    private String getModulePath(Scriptable scope) {
        Scriptable module = (Scriptable)scope.get("module", scope);
        String moduleUriString = Context.toString(module.get("uri", module));
        URI moduleUri = null;
        try {
            moduleUri = new URI(moduleUriString);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid URI " + moduleUriString);
        }

        return FilenameUtils.getFullPath(moduleUri.getPath());
    }

    public class PostExecNodeRequire implements Script {
        private final Scriptable globalScope;
        private final Stack<String> moduleLoadStack;

        public PostExecNodeRequire(Scriptable globalScope, Stack<String> moduleLoadStack) {
            this.globalScope = globalScope;
            this.moduleLoadStack = moduleLoadStack;
        }

        @Override
        public Object exec(Context cx, Scriptable scope) {
            String fullPath = getModulePath(scope);

            if (fullPath.endsWith("META-INF/env/") ) {
                return Undefined.instance;
            }

//            fullPath = moduleLoadStack.pop();

//            globalScope.put("__dirname", globalScope, fullPath);
//            globalScope.put("__filename", globalScope, fullPath);
            return Undefined.instance;
        }
    }

    public Require createRequire(Context cx, Scriptable globalScope,
                                 NodeJsUrlModuleSourceProvider moduleSourceProvider) {
        //TODO test preExec and postExec
        Script preExec = new PreExecNodeRequire(globalScope, moduleLoadStack);
        Script postExec = new PostExecNodeRequire(globalScope, moduleLoadStack);
        return new NodeRequire(asyncCallbackQueue, cx, globalScope, moduleScriptProvider,
                preExec, postExec, sandboxed, exitCallbackExecutor);
    }
}
