/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.impl;

import org.mule.tools.rhinodo.tools.JarURIHelper;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public class JavascriptResource {
    private final File file;

    public static JavascriptResource copyFromJarAndCreate(URI resource, File destDir) {
        if ("jar".equals(resource.getScheme())) {
            try {
                new JarURIHelper(resource).copyToFolder(destDir, true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return new JavascriptResource(new File(destDir, "META-INF/env"));

        } else {
            throw new IllegalArgumentException(String.format("Error creating Javascript Resource: " +
                    "[%s] scheme not recognized.", resource.getScheme()));
        }
    }

    public static JavascriptResource createFromFile(URI resource) {
        if (!"file".equals(resource.getScheme())) {
            throw new IllegalArgumentException(String.format("Error creating Javascript Resource: " +
                    "[%s] scheme not recognized.", resource.getScheme()));
        }
        return new JavascriptResource(new File(resource.getPath()));
    }

    private JavascriptResource(File file) {
        if (!file.exists()) {
            throw new IllegalArgumentException("Javascript resource does not exist: [" + file + "]");
        }
        this.file = file;
    }

    public File getFile() {
        return file;
    }
}
