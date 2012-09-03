package org.mule.tools.rhinodo.api;

public interface Console {

    void debug(String message);

    void error(String message);

    void info(String message);

    void warn(String message);

    void log(String message);

}
