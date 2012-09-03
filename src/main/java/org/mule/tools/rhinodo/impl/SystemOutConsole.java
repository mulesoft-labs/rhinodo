package org.mule.tools.rhinodo.impl;


import org.mule.tools.rhinodo.api.Console;

public class SystemOutConsole implements Console {

    @Override
    public void debug(String message) {
        System.out.println(message);
    }

    @Override
    public void error(String message) {
        System.err.println(message);
    }

    @Override
    public void info(String message) {
        System.out.println(message);
    }

    @Override
    public void warn(String message) {
        System.out.println(message);
    }

    @Override
    public void log(String message) {
        System.out.println(message);
    }
}
