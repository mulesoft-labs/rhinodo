/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.main;

import org.mozilla.javascript.*;
import org.mule.tools.rhinodo.impl.Rhinodo;

import java.io.*;

public class Main {
    public static final String PROMPT = "> ";

    public static void main(String[] args) throws FileNotFoundException {
        String userHome = System.getProperty("user.home");

        stty("-echo -isig -icanon min 1");
        try {
            Rhinodo
                    .create()
                    .destDir(new File(new File(userHome), ".rhinodo"))
                    .build(new BaseFunction(){
                        @Override
                        public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                            Scriptable repl = (Scriptable) ScriptableObject.getTypedProperty(scope, "require", Function.class)
                                    .call(cx, scope, thisObj, new Object[]{"repl"});
                            Function start = ScriptableObject.getTypedProperty(repl, "start", Function.class);

                            Scriptable options = cx.newObject(scope);
                            ScriptableObject.putProperty(options, "prompt", Context.javaToJS(PROMPT, scope));
                            ScriptableObject.putProperty(options, "terminal", Context.javaToJS(true, scope));
                            start.call(cx, scope, thisObj, new Object[]{options});

                            return Undefined.instance;
                        }
                    });
        } finally {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    stty("sane");
                }
            });
        }

    }

    private static void stty(String sttyString) {
        String[] cmd2 = {"/bin/sh", "-c", "stty " + sttyString + " </dev/tty"};
        try {
            Runtime.getRuntime().exec(cmd2).waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
