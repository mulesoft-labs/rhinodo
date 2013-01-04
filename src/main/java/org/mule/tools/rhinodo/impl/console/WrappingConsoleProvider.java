/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.impl.console;

import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mule.tools.rhinodo.api.Console;
import org.mule.tools.rhinodo.api.ConsoleProvider;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class WrappingConsoleProvider implements ConsoleProvider {
    private Console console;

    public WrappingConsoleProvider(Console console) {
        this.console = console;
    }

    @Override
    public Scriptable getConsoleAsScriptable(Scriptable scope) {
        NativeObject consoleNativeObject = new NativeObject();
        Map<String, LogFunctionWrapper> functionWrapperMap = new HashMap<String, LogFunctionWrapper>();

        for (Method method : Console.class.getDeclaredMethods()) {
            functionWrapperMap.put(method.getName(), LogFunctionWrapper.fromMethodWithDebugging(console, method, scope));
        }

        for (Map.Entry<String, LogFunctionWrapper> stringFunctionWrapperEntry : functionWrapperMap.entrySet()) {
            consoleNativeObject.put(stringFunctionWrapperEntry.getKey(), consoleNativeObject, stringFunctionWrapperEntry.getValue());
        }

        return consoleNativeObject;
    }


}
