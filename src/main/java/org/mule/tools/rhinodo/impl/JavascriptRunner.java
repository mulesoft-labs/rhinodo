package org.mule.tools.rhinodo.impl;

import org.mule.tools.rhinodo.api.NodeModuleFactory;
import org.mule.tools.rhinodo.api.Runnable;
import org.mozilla.javascript.Context;
import org.mule.tools.rhinodo.rhino.NodeJsGlobal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class JavascriptRunner {

    private URI env;
    private NodeModuleFactory nodeModuleFactory;
    private org.mule.tools.rhinodo.api.Runnable runnable;


    public JavascriptRunner(NodeModuleFactoryImpl nodeModuleFactory,
                            Runnable runnable) {
        env = getURIFromResources(this.getClass(),"META-INF/env");
        this.nodeModuleFactory = new PrimitiveNodeModuleFactory(env, nodeModuleFactory);
        this.runnable = runnable;
    }

    public void run()  {
        NodeJsGlobal global = new NodeJsGlobal();

        Context ctx = Context.enter();
        ctx.setOptimizationLevel(9);
        ctx.setLanguageVersion(170);

        global.initStandardObjects(ctx,false);

        try {
            global.installNodeJsRequire(ctx, nodeModuleFactory, false);

            ctx.evaluateReader(global, new FileReader(
                    new File(getURIFromResources(this.getClass(), "META-INF/env/builtins.js").getPath())), "builtins", -1, null);

            runnable.run(ctx, global);

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            Context.exit();
        }
    }

    public static URI getURIFromResources(Class<?> klass, String path) {
        ClassLoader classLoader = klass.getClassLoader();
        URL root = classLoader.getResource(path);

        if ( root == null ) {
            throw new IllegalStateException("root cannot be null");
        }

        try {
            return root.toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
