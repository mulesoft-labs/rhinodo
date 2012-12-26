/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.rhino;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.commonjs.module.Require;
import org.mozilla.javascript.commonjs.module.provider.SoftCachingModuleScriptProvider;
import org.mozilla.javascript.tools.shell.Global;
import org.mule.tools.rhinodo.api.NodeModule;
import org.mule.tools.rhinodo.api.NodeModuleFactory;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NodeJsGlobal extends Global {
    @Override
    public Require installRequire(Context cx, List<String> modulePath, boolean sandboxed) {
        return super.installRequire(cx, modulePath, sandboxed);
    }

    public Require installNodeJsRequire(Context cx, NodeModuleFactory nodeModuleFactory,
                                        NodeRequireBuilder rb, boolean sandboxed) {
        rb.setSandboxed(sandboxed);
        Map<String, URI> uris = new LinkedHashMap<String, java.net.URI>();
        if (nodeModuleFactory != null) {
            for (NodeModule nodeModule : nodeModuleFactory.getModules()) {
                try {
                    URI uri = nodeModule.getPath();
                    if (!uri.isAbsolute()) {
                        // call resolve("") to canonify the path
                        uri = new File(uri).toURI().resolve("");
                    }
                    if (!uri.toString().endsWith("/")) {
                        // make sure URI always terminates with slash to
                        // avoid loading from unintended locations
                        uri = new URI(uri + "/");
                    }
                    uris.put(nodeModule.getName(), uri);
                } catch (URISyntaxException usx) {
                    throw new RuntimeException(usx);
                }
            }
        }
        NodeJsUrlModuleSourceProvider moduleSourceProvider = new NodeJsUrlModuleSourceProvider(uris);
        rb.setModuleScriptProvider(
                new SoftCachingModuleScriptProvider(
                        moduleSourceProvider));

        Require require = rb.createRequire(cx, this, moduleSourceProvider);
        require.install(this);
        return require;
    }
}
