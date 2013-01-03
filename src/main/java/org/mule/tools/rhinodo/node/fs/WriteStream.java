/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.node.fs;

import org.mozilla.javascript.Function;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class WriteStream {
    private File file;
    private final Queue<Function> asyncFunctionQueue;
    private final Map<String, Function> events = new HashMap<String, Function>();

    public WriteStream(Queue<Function> asyncFunctionQueue, String file) {
        this.asyncFunctionQueue = asyncFunctionQueue;
        this.file = new File(file).getAbsoluteFile();
    }

    File getFile() {
        return file;
    }
}
