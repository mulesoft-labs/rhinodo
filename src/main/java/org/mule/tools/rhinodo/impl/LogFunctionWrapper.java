/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.impl;

import org.mozilla.javascript.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class LogFunctionWrapper extends BaseFunction {
    private Method method;
    private boolean debug;
    private Object instance;

    public LogFunctionWrapper() {}

    public static LogFunctionWrapper fromMethodWithDebugging(Object instance, Method method) {
        LogFunctionWrapper logFunctionWrapper = new LogFunctionWrapper();
        logFunctionWrapper.method = method;
        logFunctionWrapper.instance = instance;
        logFunctionWrapper.debug = true;
        return logFunctionWrapper;
    }

    public static LogFunctionWrapper fromMethod(Object instance, Method method) {
        LogFunctionWrapper logFunctionWrapper = new LogFunctionWrapper();
        logFunctionWrapper.method = method;
        logFunctionWrapper.instance = instance;
        return logFunctionWrapper;
    }

    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        try {
            if ( args.length > 0 ) {
                if( debug && args[0] instanceof NativeObject) {
                    NativeObject args0AsNativeObject = (NativeObject) args[0];
                    for (Map.Entry<Object, Object> objectObjectEntry : args0AsNativeObject.entrySet()) {
                        method.invoke(instance, (objectObjectEntry.getKey() + " -> " + objectObjectEntry.getValue()));
                    }
                }
                method.invoke(instance, Context.toString(args[0]));
            } else {
                method.invoke(instance, Context.toString(Undefined.instance));
            }

        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return Undefined.instance;
    }
}
