/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.impl;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mule.tools.rhinodo.api.NodeModule;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NodeModuleProviderImplTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void create() throws Exception {
        URL brunchJar = this.getClass().getClassLoader().getResource("brunch-maven-plugin-1.0-SNAPSHOT.jar");

        temporaryFolder.create();

        File file = temporaryFolder.newFolder();

        URI jarURI = new URI("jar:" + brunchJar.toURI().toString() + "!/META-INF/node_modules/");
        NodeModuleProviderImpl nodeModuleProvider = new NodeModuleProviderImpl(file.getAbsolutePath(),
                jarURI);

        assertNotNull(nodeModuleProvider);

        Collection<? extends NodeModule> modules = nodeModuleProvider.getModules();

        assertNotNull(modules);
        assertEquals(39, modules.size());

    }
}
