package org.mule.tools.rhinodo.impl;

import org.mozilla.javascript.*;
import org.mule.tools.rhinodo.api.NodeModuleFactory;
import org.mule.tools.rhinodo.api.Runnable;
import org.mule.tools.rhinodo.rhino.NodeJsGlobal;

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

            NativeObject console = new NativeObject();
            console.put("log", console, new BaseFunction() {
                @Override
                public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                    if(args.length > 0) {
                        System.out.println(args[0]);
                    } else {
                        System.out.println("undefined");
                    }
                    return Undefined.instance;
                }
            });
            global.put("console", global, console);

            NativeObject process = new NativeObject();
            process.put("platform", process, Context.toString("darwin"));
            process.put("env",  process, new NativeObject());
            global.put("process", global, process);

            runnable.run(ctx, global);

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
