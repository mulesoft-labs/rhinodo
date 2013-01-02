/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.impl.console;

import org.mozilla.javascript.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Map;

public class LogFunctionWrapper extends FunctionObject {
    private Method method;
    private boolean debug;
    private Object instance;

    public LogFunctionWrapper(String name, Member methodOrConstructor, Scriptable scope) {
        super(name, methodOrConstructor, scope);
    }

    public static LogFunctionWrapper fromMethodWithDebugging(Object instance, Method method, Scriptable scope) {
        LogFunctionWrapper logFunctionWrapper = new LogFunctionWrapper(method.getName(), method, scope);
        logFunctionWrapper.method = method;
        logFunctionWrapper.instance = instance;
        logFunctionWrapper.debug = true;
        return logFunctionWrapper;
    }

    public static LogFunctionWrapper fromMethod(Object instance, Method method, Scriptable scope) {
        LogFunctionWrapper logFunctionWrapper = new LogFunctionWrapper(method.getName(), method, scope);
        logFunctionWrapper.method = method;
        logFunctionWrapper.instance = instance;
        return logFunctionWrapper;
    }

    @Override
    public Object getDefaultValue(Class<?> typeHint) {
        return "LogFunctionWrapper";
    }

    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        try {
            if ( args.length == 1 ) {

                if( debug && args[0] instanceof NativeObject) {
                    method.invoke(instance, convertObjectToString((NativeObject) args[0]));
                } else {
                    method.invoke(instance, Context.toString(args[0]));
                }
            } else if ( args.length > 0 ) {
                StringBuilder stringBuilder = new StringBuilder();
                for (Object arg : args) {
                    if ( arg instanceof NativeObject ) {
                        stringBuilder.append(convertObjectToString((NativeObject) arg));
                    } else {
                        stringBuilder.append(Context.toString(arg));
                    }
                    stringBuilder.append(" ");
                }
                method.invoke(instance, stringBuilder.toString());
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

    private String convertObjectToString(NativeObject arg) throws IllegalAccessException, InvocationTargetException {
        NativeObject args0AsNativeObject = arg;
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        for (Map.Entry<Object, Object> objectObjectEntry : args0AsNativeObject.entrySet()) {
            sb.append(" ");
            sb.append(objectObjectEntry.getKey());
            sb.append(": ");
            sb.append(Context.toString(objectObjectEntry.getValue()));
            sb.append(",");
        }
        if (sb.length() > 1) {
            sb.deleteCharAt(sb.length()-1);
        }
        sb.append("}");

        return sb.toString();
    }
}
