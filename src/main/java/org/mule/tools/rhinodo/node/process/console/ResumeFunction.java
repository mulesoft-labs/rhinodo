/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.node.process.console;

import org.mozilla.javascript.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Queue;


public class ResumeFunction extends BaseFunction {

    private final InputStream in;
    private final boolean[] shouldIPause;
    private final Queue<Function> asyncFunctionQueue;

    public ResumeFunction(InputStream in, boolean[] shouldIPause, Queue<Function> asyncFunctionQueue) {
        this.in = in;
        this.shouldIPause = shouldIPause;
        this.asyncFunctionQueue = asyncFunctionQueue;
    }

    @Override
    public Object call(Context cx, Scriptable scope, final Scriptable originalThisObj, Object[] args) {
        final Function emit = ScriptableObject.getTypedProperty(originalThisObj, "emit", Function.class);

        BaseFunction readLineCallback = new BaseFunction() {

            @Override
            public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {

                int byteRead;
                int available;

                try {
                    if ((available = in.available()) == 0 && !shouldIPause[0]) {
                        asyncFunctionQueue.add(this);
                        return Undefined.instance;
                    } else if (shouldIPause[0]) {
                        return Undefined.instance;
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                StringBuilder stringBuilder = new StringBuilder();
                try {
                    do {
                    byteRead = in.read();
                    available--;
                    stringBuilder.append((char)byteRead);
                    } while (available > 0 && byteRead != -1);

                    if (byteRead == -1) {
                        shouldIPause[0] = true;
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                if (shouldIPause[0]) {
                    emit.call(cx, scope, originalThisObj, new Object[]{"end"});
                    return Undefined.instance;
                }

                emit.call(cx, scope, originalThisObj, new Object[]{"data",stringBuilder.toString()});
                asyncFunctionQueue.add(this);

                return Undefined.instance;
            }
        };
        asyncFunctionQueue.add(readLineCallback);
        return Undefined.instance;
    }

}
