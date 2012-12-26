/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.mule.tools.rhinodo.tools.JarURIHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

public class NodeModuleImplBuilder {


    public static NodeModuleImpl fromJarOrFile(Class<?> klass, String rootDirectory, String destDir) {
        //TODO Add validation
        URI root = getRoot(klass, rootDirectory);

        if ( "jar".equals(root.getScheme())) {
            return fromJar(klass,rootDirectory,destDir);
        }

        return extractFromPackageJson(root);
    }

    public static NodeModuleImpl fromJar(Class<?> klass, String rootDirectory, String destDir) {
        if ( rootDirectory == null ) {
            throw new IllegalArgumentException("Error validating rootDirectory");
        }

        if ( destDir == null ) {
            throw new IllegalArgumentException("Error validating destDir");
        }

        URI root = getRoot(klass, rootDirectory);

        if ( !"jar".equals(root.getScheme())) {
            throw new IllegalArgumentException("URI must have jar scheme");
        }

        JarURIHelper jarURIHelper;

        new File(destDir).mkdirs();

        try {
            jarURIHelper = new JarURIHelper(root);
            jarURIHelper.copyToFolder(new File(destDir));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return fromFolder(destDir + File.separator + jarURIHelper.getInsideJarRelativePath());
    }

    public static NodeModuleImpl fromFolder(String destDir) {
        return extractFromPackageJson(new File(destDir + "/").toURI());
    }

    private static NodeModuleImpl extractFromPackageJson(URI root) {
        if ( root == null) {
            throw new IllegalArgumentException("Error validating rootDirectory");
        }

        URI packageJson;
        try {
            packageJson = new URI(root.toString() + "/" + "package.json");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        boolean exists;
        if ("file".equals(packageJson.getScheme())) {
            exists = new File(packageJson).exists();
        } else {
            throw new IllegalStateException(String.format("Error: scheme [%s] not supported.",packageJson.getScheme()) );
        }

        if( !exists ) {
            throw new IllegalStateException(String.format("Error: package.json not found at [%s].", packageJson));
        }

        return NodeModuleImpl.create(root, NodeModuleImplBuilder.<String,String>getPackageJSONMap(new File(packageJson.getPath())));
    }

    public static <U,V> Map<U,V> getPackageJSONMap(File packageJson) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<U,V> map;
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(packageJson);
            map = objectMapper.readValue(inputStream, Map.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error: trying to parse package.json.");
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        return map;
    }

    private static URI getRoot(Class<?> klass, String rootDirectory) {
        ClassLoader classLoader = klass.getClassLoader();
        URI root;
        try {
            URL resource = classLoader.getResource(rootDirectory);
            if (resource == null ) {
                throw new IllegalArgumentException("Invalid resource at: " + rootDirectory);
            }
            root = resource.toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        if ( root == null ) {
            throw new IllegalStateException("Error: path not found.");
        }

        try {
            root = new URI(root.toString() + "/");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        return root;
    }
}
