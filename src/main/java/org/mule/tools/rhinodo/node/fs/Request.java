/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.node.fs;

import org.apache.commons.io.FileUtils;
import org.mozilla.javascript.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class Request {

    private final ReadStream readStream;
    private final WriteStream writeStream;
    private final Map<String, Function> events = new HashMap<String, Function>();

    public Request(Queue<Function> asyncFunctionQueue, ReadStream readStream, WriteStream writeStream) {
        this.readStream = readStream;
        this.writeStream = writeStream;

        asyncFunctionQueue.add(new BaseFunction() {
            @Override
            public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                try {
                    FileUtils.copyFile(Request.this.readStream.getFile(),
                            Request.this.writeStream.getFile());
                    Function close = events.get("close");
                    if ( close != null ) {
                        close.call(cx, scope, thisObj, new Object[]{});
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                return Undefined.instance;
            }
        });


    }

    public void on(String event, Function callback) {
        this.events.put(event, callback);
    }
}
