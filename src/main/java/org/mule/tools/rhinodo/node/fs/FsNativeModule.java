/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.node.fs;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mule.tools.rhinodo.node.AbstractNativeModule;

import java.util.Queue;

public class FsNativeModule extends AbstractNativeModule {

    public FsNativeModule(Queue<Function> asyncFunctionQueue) {
        super(asyncFunctionQueue);
    }

    @Override
    public String getId() {
        return "fs";
    }

    @Override
    protected void populateModule(Scriptable module, Queue<Function> asyncFunctionQueue) {
        ScriptableObject.putProperty(module, "readFile", new ReadFile(asyncFunctionQueue));
        ScriptableObject.putProperty(module, "readdirSync", new ReaddirSync());
        ScriptableObject.putProperty(module, "statSync", new StatSync());
        ScriptableObject.putProperty(module, "readFileSync", new ReadFileSync());
        ScriptableObject.putProperty(module, "existsSync", new ExistsSync());
        ScriptableObject.putProperty(module, "exists", new Exists(asyncFunctionQueue));
        ScriptableObject.putProperty(module, "realpath", new RealPath(asyncFunctionQueue));
        ScriptableObject.putProperty(module, "stat", new Stat(asyncFunctionQueue));
        ScriptableObject.putProperty(module, "readdir", new Readdir(asyncFunctionQueue));
        ScriptableObject.putProperty(module, "watchFile", new WatchFile(asyncFunctionQueue));
        ScriptableObject.putProperty(module, "unwatchFile", new UnWatchFile(asyncFunctionQueue));
        ScriptableObject.putProperty(module, "createReadStream", new CreateReadStream(asyncFunctionQueue));
        ScriptableObject.putProperty(module, "createWriteStream", new CreateWriteStream(asyncFunctionQueue));
        ScriptableObject.putProperty(module, "writeFile", new WriteFile(asyncFunctionQueue));
        ScriptableObject.putProperty(module, "mkdir", new Mkdir(asyncFunctionQueue));
    }
}
