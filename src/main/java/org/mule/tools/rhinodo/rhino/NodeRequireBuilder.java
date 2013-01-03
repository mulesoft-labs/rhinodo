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
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

public class NodeRequireBuilder {

    private boolean sandboxed = true;
    private ModuleScriptProvider moduleScriptProvider;
    private Queue<Function> asyncCallbackQueue;
    private final ExitCallbackExecutor exitCallbackExecutor;
    private final Map<String, Scriptable> partialOldExports = new ConcurrentHashMap<String, Scriptable>();
    private final Map<String, Scriptable> partialExecutionScopes = new ConcurrentHashMap<String, Scriptable>();

    public NodeRequireBuilder(Queue<Function> asyncCallbackQueue,
                              ExitCallbackExecutor exitCallbackExecutor) {
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

        public PreExecNodeRequire(Scriptable globalScope) {
            this.globalScope = globalScope;
        }

        @Override
        public Object exec(Context cx, Scriptable scope) {
            String fullPath = getModulePath(scope);

            String id = ScriptableObject.getTypedProperty(ScriptableObject.getTypedProperty(
                    scope, "module", Scriptable.class), "id", String.class);
            // Refresh exports
                for (Map.Entry<String, Scriptable> stringScriptableEntry : partialOldExports.entrySet()) {
                Scriptable oldExport = stringScriptableEntry.getValue();
                Scriptable newExport = ScriptableObject.getTypedProperty(
                        partialExecutionScopes.get(stringScriptableEntry.getKey()), "exports", Scriptable.class);
                Scriptable newModuleExport = ScriptableObject.getTypedProperty(ScriptableObject.getTypedProperty(
                        partialExecutionScopes.get(stringScriptableEntry.getKey()), "module", Scriptable.class),
                        "exports", Scriptable.class);

                if (oldExport != newExport) {
                    copyKeys(oldExport, newExport);
                }

                if (oldExport != newModuleExport && newModuleExport != newExport) {
                    copyKeys(oldExport, newModuleExport);
                }

            }

            if ( !partialOldExports.containsKey(id) ) {
                partialOldExports.put(id, ScriptableObject.getTypedProperty(scope, "exports", Scriptable.class));
                partialExecutionScopes.put(id, scope);
            }

            if (fullPath.endsWith("META-INF/env/") ) {
                return Undefined.instance;
            }

            ScriptableObject.putConstProperty(scope, "__dirname", fullPath);
            ScriptableObject.putConstProperty(scope, "__filename", fullPath);


            return Undefined.instance;
        }

        private void copyKeys(Scriptable oldExport, Scriptable newExport) {
            for (Object o : newExport.getIds()) {
                String name = o.toString();
                ScriptableObject.putProperty(oldExport, name, ScriptableObject.getProperty(newExport, name));
            }
        }
    }

    public class PostExecNodeRequire implements Script {

        private final Scriptable globalScope;

        public PostExecNodeRequire(Scriptable globalScope) {
            this.globalScope = globalScope;
        }

        @Override
        public Object exec(Context cx, Scriptable scope) {

            String id = ScriptableObject.getTypedProperty(ScriptableObject.getTypedProperty(
                    scope, "module", Scriptable.class),
                    "id", String.class);

            if ( partialOldExports.containsKey(id) ) {
                synchronized (partialOldExports) {
                partialOldExports.remove(id);
                }
                partialExecutionScopes.remove(id);
            }

            return Undefined.instance;

        }
    }

    private String getModulePath(Scriptable scope) {
        Scriptable module = (Scriptable)scope.get("module", scope);
        String moduleUriString = Context.toString(module.get("uri", module));
        URI moduleUri;
        try {
            moduleUri = new URI(moduleUriString);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid URI " + moduleUriString);
        }

        return FilenameUtils.getFullPath(moduleUri.getPath());
    }

    public Require createRequire(Context cx, Scriptable env, Scriptable globalScope) {
        //TODO test preExec and postExec
        Script preExec = new PreExecNodeRequire(globalScope);
        Script postExec = new PostExecNodeRequire(globalScope);
        return new NodeRequire(asyncCallbackQueue, env, cx, globalScope, moduleScriptProvider,
                preExec, postExec, sandboxed, exitCallbackExecutor);
    }
}
