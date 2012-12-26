/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.node.fs;

import java.io.File;

public class Stats {
    private final boolean isFile;
    private final boolean isDirectory;

    public boolean isFile() {
        return isFile;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public boolean isBlockDevice() {
        return isBlockDevice;
    }

    public boolean isCharacterDevice() {
        return isCharacterDevice;
    }

    public boolean isSymbolicLink() {
        return isSymbolicLink;
    }

    public boolean isFIFO() {
        return isFIFO;
    }

    public boolean isSocket() {
        return isSocket;
    }

    private final boolean isBlockDevice;
    private final boolean isCharacterDevice;
    private final boolean isSymbolicLink;
    private final boolean isFIFO;
    private final boolean isSocket;

    public Stats(File file) {
        this.isFile = file.isFile();
        this.isDirectory = file.isDirectory();

        //TODO implement
        this.isBlockDevice = false;
        this.isCharacterDevice = true;
        this.isSymbolicLink = false;
        this.isFIFO = false;
        this.isSocket = false;

    }

}
