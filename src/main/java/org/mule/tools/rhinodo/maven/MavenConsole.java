/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

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
