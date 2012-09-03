package org.mule.tools.rhinodo.maven;

import org.apache.maven.plugin.logging.Log;
import org.mule.tools.rhinodo.api.Console;

public class MavenConsole implements Console {

    private Log log;

    private MavenConsole() {}

    public static MavenConsole fromMavenLog(Log log) {
        MavenConsole mavenConsole = new MavenConsole();
        mavenConsole.log = log;
        return mavenConsole;
    }

    @Override
    public void debug(String message) {
        log.debug(message);
    }

    @Override
    public void error(String message) {
        log.error(message);
    }

    @Override
    public void info(String message) {
        log.info(message);
    }

    @Override
    public void warn(String message) {
        log.warn(message);
    }

    @Override
    public void log(String message) {
        log.info(message);
    }
}
