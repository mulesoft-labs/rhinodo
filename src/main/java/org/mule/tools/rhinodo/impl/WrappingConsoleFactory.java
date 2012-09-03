package org.mule.tools.rhinodo.impl;

import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mule.tools.rhinodo.api.Console;
import org.mule.tools.rhinodo.api.ConsoleFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class WrappingConsoleFactory implements ConsoleFactory {
    private Console console;
    Map<String, LogFunctionWrapper> functionWrapperMap = new HashMap<String, LogFunctionWrapper>();

    public WrappingConsoleFactory(Console console) {
        this.console = console;

        for (Method method : Console.class.getDeclaredMethods()) {
            functionWrapperMap.put(method.getName(), LogFunctionWrapper.fromMethodWithDebugging(console, method));
        }
    }

    @Override
    public Scriptable getConsoleAsScriptable() {
        NativeObject consoleNativeObject = new NativeObject();

        for (Map.Entry<String, LogFunctionWrapper> stringFunctionWrapperEntry : functionWrapperMap.entrySet()) {
            consoleNativeObject.put(stringFunctionWrapperEntry.getKey(), consoleNativeObject, stringFunctionWrapperEntry.getValue());
        }

        return consoleNativeObject;
    }


}
