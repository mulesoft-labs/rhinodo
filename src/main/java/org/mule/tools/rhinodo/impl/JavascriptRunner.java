package org.mule.tools.rhinodo.impl;

import org.mozilla.javascript.*;
import org.mule.tools.rhinodo.api.NodeModuleFactory;
import org.mule.tools.rhinodo.api.Runnable;
import org.mule.tools.rhinodo.rhino.NodeJsGlobal;
import org.mule.tools.rhinodo.rhino.NodeRequireBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class JavascriptRunner {

    private URI env;
    private NodeModuleFactory nodeModuleFactory;
    private org.mule.tools.rhinodo.api.Runnable runnable;
    private final Queue<Function> asyncFunctionQueue = new LinkedList<Function>();


    public JavascriptRunner(NodeModuleFactoryImpl nodeModuleFactory,
                            Runnable runnable) {
        env = getURIFromResources(this.getClass(),"META-INF/env");
        this.nodeModuleFactory = new PrimitiveNodeModuleFactory(env, nodeModuleFactory);
        this.runnable = runnable;
    }

    public void run()  {
        final NodeJsGlobal global = new NodeJsGlobal();

        Context ctx = Context.enter();
        ctx.setOptimizationLevel(9);
        ctx.setLanguageVersion(170);

        global.initStandardObjects(ctx,false);

        try {
            global.installNodeJsRequire(ctx, nodeModuleFactory, new NodeRequireBuilder(asyncFunctionQueue), false);

            NativeObject console = new NativeObject();
            console.put("log", console, new BaseFunction() {
                @Override
                public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                    if(args.length > 0) {
                        System.out.println(args[0]);
                        if( args[0] instanceof NativeObject) {
                            NativeObject args0AsNativeObject = (NativeObject) args[0];
                            for (Map.Entry<Object, Object> objectObjectEntry : args0AsNativeObject.entrySet()) {
                                System.out.println(objectObjectEntry.getKey() + " -> " + objectObjectEntry.getValue());
                            }
                        } else {
                            System.out.println(Context.toString(args[0]));
                        }
                    } else {
                        System.out.println(Context.toString(Undefined.instance));
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

            Function asyncToExecute;
            while ( (asyncToExecute = asyncFunctionQueue.poll()) != null ) {
                asyncToExecute.call(ctx,global,global,new Object[] {});
            }

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
