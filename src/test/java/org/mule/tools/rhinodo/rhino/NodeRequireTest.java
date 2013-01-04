/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.rhino;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mozilla.javascript.*;
import org.mozilla.javascript.commonjs.module.ModuleScope;
import org.mozilla.javascript.commonjs.module.ModuleScript;
import org.mozilla.javascript.commonjs.module.ModuleScriptProvider;
import org.mule.tools.rhinodo.api.NodeModule;
import org.mule.tools.rhinodo.impl.ExitCallbackExecutor;
import org.mule.tools.rhinodo.impl.NodeModuleProviderImpl;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NodeRequireTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /**
     * Require of a file 'tree.js' when there is a directory 'tree/'
     * and it's imported like require('path/to/file/tree').
     */
    @Test
    public void testSameNameFolderAndFolderName() throws Exception {
        final File base = folder.newFolder("base");
        final File tree = new File(base, "tree");
        tree.mkdir();
        final File treeJavascriptFile = new File(base, "tree.js");
        FileUtils.write(treeJavascriptFile, "hello");

        final Queue asyncCallback = mock(Queue.class);
        final ScriptableObject globalScope = new NodeJsGlobal();
        final ModuleScriptProvider moduleScriptProvider = mock(ModuleScriptProvider.class);
        ModuleScript moduleScript = mock(ModuleScript.class);
        when(moduleScript.getUri()).thenReturn(treeJavascriptFile.toURI());
        when(moduleScript.getBase()).thenReturn(treeJavascriptFile.toURI());
        when(moduleScript.getScript()).thenReturn(new Script() {
            @Override
            public Object exec(Context cx, Scriptable scope) {
                return null;
            }
        });
        when(moduleScriptProvider.getModuleScript(any(Context.class), eq(treeJavascriptFile.getAbsolutePath()),
                any(URI.class), any(URI.class), any(Scriptable.class))).thenReturn(moduleScript);
        final Script pre = mock(Script.class);
        final Script post = mock(Script.class);
        final ExitCallbackExecutor exitCallbackExecutor =
                mock(ExitCallbackExecutor.class);

        ContextFactory contextFactory = new ContextFactory();
        contextFactory.call(new ContextAction() {
            @Override
            public Object run(Context cx) {

                cx.initStandardObjects(globalScope);

                NodeRequire nodeRequire = new NodeRequire(asyncCallback,cx.newObject(globalScope), cx ,globalScope,
                        moduleScriptProvider, pre, post, false, exitCallbackExecutor);
                try {
                    Object call = nodeRequire.call(cx, globalScope, globalScope, new Object[]{tree.getAbsolutePath()});
                } catch (EvaluatorException e) {
                    fail("It should work and open the .js file!");
                }
                return null;
            }
        });
    }


    /**
     * Require of a directory fails
     */
    @Test(expected = JavaScriptException.class)
    public void testDirectoryRequire() throws Exception {
        final File tree = folder.newFolder("tree");

        final Queue asyncCallback = mock(Queue.class);
        final ScriptableObject globalScope = new NodeJsGlobal();
        final NodeJsUrlModuleSourceProvider moduleSourceProvider = mock(NodeJsUrlModuleSourceProvider.class);
        final ModuleScriptProvider moduleScriptProvider = mock(ModuleScriptProvider.class);
        final Script pre = mock(Script.class);
        final Script post = mock(Script.class);
        final ExitCallbackExecutor exitCallbackExecutor =
                mock(ExitCallbackExecutor.class);


        ContextFactory contextFactory = new ContextFactory();
        contextFactory.call(new ContextAction() {
            @Override
            public Object run(Context cx) {
                cx.initStandardObjects(globalScope);
                NodeRequire nodeRequire = new NodeRequire(asyncCallback,cx.newObject(globalScope), cx,globalScope,
                        moduleScriptProvider, pre, post, false, exitCallbackExecutor);
                nodeRequire.call(cx,globalScope,globalScope, new Object[]{tree.getAbsolutePath()});
                fail();
                return null;
            }
        });
    }

    /**
     * Require of a directory that contains a package.json file.
     */
    @Test
    public void testRequirePackageJsonDirectory() throws Exception {
        final File module = folder.newFolder("my_module");
        final File packageJSON = new File(module, "package.json");
        final File otherModule = folder.newFolder("my_module", "node_modules", "other_module");
        final File mainJs = new File(module, "main.js");
        final File otherModulePackageJSON = new File(otherModule, "package.json");
        final File otherModuleMain = new File(otherModule, "main.js");
        final ObjectMapper objectMapper = new ObjectMapper();

        HashMap<String, Object> packageJSONMap = new HashMap<String, Object>();
        packageJSONMap.put("main", "./main.js");

        HashMap<String, String> dependencies = new HashMap<String, String>();
        dependencies.put("other_module", "*");
        packageJSONMap.put("dependencies", dependencies);

        objectMapper.writeValue(packageJSON, packageJSONMap);

        HashMap<String, Object> otherModulePackageJSONMap = new HashMap<String, Object>();
        otherModulePackageJSONMap.put("main", "./main");
        otherModulePackageJSONMap.put("name", "other_module");

        objectMapper.writeValue(otherModulePackageJSON, otherModulePackageJSONMap);

        FileUtils.write(mainJs, "exports.hello = 'bye';" +
                "exports.other = require('other_module');");

        FileUtils.write(otherModuleMain, "exports.text = 'second';");

        final Queue asyncCallback = mock(Queue.class);
        final NodeJsGlobal globalScope = new NodeJsGlobal();
        final ExitCallbackExecutor exitCallbackExecutor =
                mock(ExitCallbackExecutor.class);


        ContextFactory contextFactory = new ContextFactory();
        contextFactory.call(new ContextAction() {
            @Override
            public Object run(Context cx) {
                cx.initStandardObjects(globalScope);
                NodeRequireBuilder rb = new NodeRequireBuilder(asyncCallback, exitCallbackExecutor);
                globalScope.installNodeJsRequire(cx,cx.newObject(globalScope),new NodeModuleProviderImpl(
                        new ArrayList<NodeModule>()),
                        rb, false);
                Function nodeRequire = ScriptableObject.getTypedProperty(
                        globalScope, "require", Function.class);
                NativeObject main = (NativeObject) nodeRequire.call(cx, globalScope, globalScope,
                        new Object[]{module.getAbsolutePath()});

                assertNotNull(main);

                assertEquals("bye", ScriptableObject.getTypedProperty(main, "hello", String.class));

                assertFalse(ScriptableObject.NOT_FOUND.equals(main));
                NativeObject other = ScriptableObject.getTypedProperty(main,
                        "other", NativeObject.class);

                assertNotNull(other);
                assertEquals("second", ScriptableObject.getProperty(other, "text"));

                return null;
            }
        });
    }

    @Test
    public void testRelativeRequire() throws Exception {
        final File module = folder.newFolder("my_module");
        final File otherFile = folder.newFolder("my_module","other_file");
        final File mainJs = new File(module, "my_module.js");
        final File otherModuleMain = new File(module, "other_file.js");

        FileUtils.write(mainJs, "exports.hello = 'bye';" +
                "exports.other = require('./other_file');");

        FileUtils.write(otherModuleMain, "exports.text = 'second';");
        final ExitCallbackExecutor exitCallbackExecutor =
                mock(ExitCallbackExecutor.class);


        final Queue asyncCallback = mock(Queue.class);
        final NodeJsGlobal globalScope = new NodeJsGlobal();

        ContextFactory contextFactory = new ContextFactory();
        contextFactory.call(new ContextAction() {
            @Override
            public Object run(Context cx) {
                cx.initStandardObjects(globalScope);
                NodeRequireBuilder rb = new NodeRequireBuilder(asyncCallback, exitCallbackExecutor);
                globalScope.installNodeJsRequire(cx,cx.newObject(globalScope),new NodeModuleProviderImpl(
                        new ArrayList<NodeModule>()),
                        rb, false);
                Function nodeRequire = ScriptableObject.getTypedProperty(
                        globalScope, "require", Function.class);
                String absolutePath = mainJs.getAbsolutePath();
                NativeObject main = (NativeObject) nodeRequire.call(cx, globalScope, globalScope,
                        new Object[]{absolutePath.substring(0, absolutePath.length() - 3)});

                assertNotNull(main);

                assertEquals("bye", ScriptableObject.getTypedProperty(main, "hello", String.class));

                assertFalse(ScriptableObject.NOT_FOUND.equals(main));
                NativeObject other = ScriptableObject.getTypedProperty(main,
                        "other", NativeObject.class);

                assertNotNull(other);
                assertEquals("second", ScriptableObject.getProperty(other, "text"));

                return null;
            }
        });
    }

    /**
     * Test require.extensions existence
     */
    @Test
    public void testRequireExtensions() throws Exception {
        ContextFactory contextFactory = new ContextFactory();
        contextFactory.call(new ContextAction() {
            @Override
            public Object run(Context cx) {
                final Queue asyncCallback = mock(Queue.class);
                final ScriptableObject globalScope = new NodeJsGlobal();
                final ModuleScriptProvider moduleScriptProvider = mock(ModuleScriptProvider.class);
                final Script pre = mock(Script.class);
                final Script post = mock(Script.class);
                final ExitCallbackExecutor exitCallbackExecutor =
                        mock(ExitCallbackExecutor.class);


                NodeRequire nodeRequire = new NodeRequire(asyncCallback, cx.newObject(globalScope),
                        cx, globalScope, moduleScriptProvider,
                        pre, post, false, exitCallbackExecutor);

                NativeObject extensions = ScriptableObject.getTypedProperty(nodeRequire, "extensions",
                        NativeObject.class);

                assertNotNull(extensions);
                assertTrue(ScriptableObject.hasProperty(extensions, ".js"));

                return null;
            }
        });
    }

    /**
     * Test adding a new filetype to require.extensions
     */
    @Test
    public void testAddNewExtension() throws Exception {

        final File fileDotPose = folder.newFile("hello.pose");

        FileUtils.write(fileDotPose, "This is a .pose file");
        final ExitCallbackExecutor exitCallbackExecutor =
                mock(ExitCallbackExecutor.class);


        ContextFactory contextFactory = new ContextFactory();
        contextFactory.call(new ContextAction() {
            @Override
            public Object run(Context cx) {

                final Queue asyncCallback = mock(Queue.class);
                final ScriptableObject globalScope = new NodeJsGlobal();
                cx.initStandardObjects(globalScope);

                final NodeJsUrlModuleSourceProvider moduleSourceProvider = mock(NodeJsUrlModuleSourceProvider.class);
                final ModuleScriptProvider moduleScriptProvider = mock(ModuleScriptProvider.class);
                final Script pre = mock(Script.class);
                final Script post = mock(Script.class);

                NodeRequire nodeRequire = new NodeRequire(asyncCallback, cx.newObject(globalScope), cx, globalScope,
                        moduleScriptProvider, pre, post, false, exitCallbackExecutor);

                NativeObject extensions = ScriptableObject.getTypedProperty(nodeRequire, "extensions",
                        NativeObject.class);

                Function compileExtension = mock(Function.class);
                assertNotNull(extensions);

                ScriptableObject.putProperty(extensions, ".pose", compileExtension);

                assertTrue(ScriptableObject.hasProperty(extensions, ".js"));
                assertTrue(ScriptableObject.hasProperty(extensions, ".pose"));

                nodeRequire.call(cx,globalScope,globalScope,new Object[]{fileDotPose.getAbsolutePath()});

                return null;
            }
        });
    }

    @Test
    public void testTryExtensions() throws Exception {

        final File file = folder.newFile("hello.pose");

        FileUtils.write(file, "This is a .pose file");
        final ExitCallbackExecutor exitCallbackExecutor =
                mock(ExitCallbackExecutor.class);


        ContextFactory contextFactory = new ContextFactory();
        contextFactory.call(new ContextAction() {
            @Override
            public Object run(Context cx) {

                final Queue asyncCallback = mock(Queue.class);
                final ScriptableObject globalScope = new NodeJsGlobal();
                cx.initStandardObjects(globalScope);

                final ModuleScriptProvider moduleScriptProvider = mock(ModuleScriptProvider.class);
                final Script pre = mock(Script.class);
                final Script post = mock(Script.class);

                NodeRequire nodeRequire = new NodeRequire(asyncCallback, cx.newObject(globalScope), cx, globalScope,
                        moduleScriptProvider, pre, post, false, exitCallbackExecutor);

                Scriptable extensions = ScriptableObject.getTypedProperty(nodeRequire, "extensions", Scriptable.class);
                BaseFunction myExtensionCallback = new BaseFunction() {
                };
                ScriptableObject.putProperty(extensions, ".pose", myExtensionCallback);

                ModuleScope moduleScope = mock(ModuleScope.class);
                when(moduleScope.getUri()).thenReturn(file.getParentFile().toURI());
                when(moduleScope.getBase()).thenReturn(file.getParentFile().toURI());
                NodeRequire.TryExtensionsResult extensionFound =
                        nodeRequire.tryExtensions(FilenameUtils.concat(file.getParent(),
                                FilenameUtils.getBaseName(file.getAbsolutePath())), moduleScope);

                assertNotNull(extensionFound);
                assertEquals(".pose", extensionFound.getExtensionAsString());
                assertSame(myExtensionCallback, extensionFound.getCallback());

                return null;
            }
        });
    }

    @Test
    public void dotSlash() throws Exception {

        final File file = folder.newFolder("myfolder");
        final File file1 = new File(file, "file1.js");
        final File index = new File(file, "index.js");

        FileUtils.write(index, "exports.msg = 'bar';");
        FileUtils.write(file1, "var x = require('./'); exports.foo = x.msg;");
        final ExitCallbackExecutor exitCallbackExecutor =
                mock(ExitCallbackExecutor.class);

        final Queue asyncCallback = mock(Queue.class);
        final NodeJsGlobal globalScope = new NodeJsGlobal();

        ContextFactory contextFactory = new ContextFactory();
        contextFactory.call(new ContextAction() {
            @Override
            public Object run(Context cx) {
                cx.initStandardObjects(globalScope);
                NodeRequireBuilder rb = new NodeRequireBuilder(asyncCallback, exitCallbackExecutor);
                globalScope.installNodeJsRequire(cx,cx.newObject(globalScope),new NodeModuleProviderImpl(
                        new ArrayList<NodeModule>()),
                        rb, false);
                Function nodeRequire = ScriptableObject.getTypedProperty(
                        globalScope, "require", Function.class);
                String absolutePath = file1.getAbsolutePath();
                Scriptable file1Object = (Scriptable) nodeRequire.call(cx, globalScope, globalScope,
                        new Object[]{absolutePath.substring(0, absolutePath.length() - 3)});

                assertNotNull(file1Object);

                assertEquals("bar", ScriptableObject.getTypedProperty(file1Object, "foo", String.class));

                return Undefined.instance;
            }
        });
    }
}
