/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.main;

import org.mozilla.javascript.*;
import org.mozilla.javascript.tools.ToolErrorReporter;
import org.mozilla.javascript.tools.shell.Global;
import org.mule.tools.rhinodo.impl.JavascriptRunner;
import org.mule.tools.rhinodo.impl.NodeModuleFactoryImpl;

import java.io.*;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;
import java.util.List;

public class Main implements org.mule.tools.rhinodo.api.Runnable {
    private static final int EXITCODE_RUNTIME_ERROR = 1;
    public static final String PROMPT = "> ";
    private Context ctx;
    private static boolean debug = true;

    public static void main(String [] args) {
        if (debug) {
            System.out.println("Called with " + Arrays.toString(args));
        }
        JavascriptRunner javascriptRunner;
        String userHome = System.getProperty("user.home");
        Main main = new Main();
        javascriptRunner = new JavascriptRunner(main, new File(new File(userHome), ".rhinodo"));
        javascriptRunner.run();
    }

    @Override
    public void executeJavascript(Context ctx, Global global) {
        this.ctx = ctx;
        ToolErrorReporter toolErrorReporter = new ToolErrorReporter(true);
        ctx.setErrorReporter(toolErrorReporter);

        int exitCode;
        Scriptable scope = global;
        PrintStream ps = System.err;
        BufferedReader in;
        try
        {
            in = new BufferedReader(new InputStreamReader(System.in,
                    "UTF-8"));
        }
        catch(UnsupportedEncodingException e)
        {
            throw new UndeclaredThrowableException(e);
        }
        int lineno = 1;
        boolean hitEOF = false;
        while (!hitEOF) {
            String source = "";
            ps.flush();

            ps.print(PROMPT);

            // Collect lines of source to compile.
            while (true) {
                String newline;
                try {
                    newline = in.readLine();
                }
                catch (IOException ioe) {
                    ps.println(ioe.toString());
                    break;
                }
                if (newline == null) {
                    hitEOF = true;
                    break;
                }
                source = source + newline + "\n";
                lineno++;
                if (ctx.stringIsCompilableUnit(source))
                    break;
                ps.print(PROMPT);
            }
            if (!hitEOF) {
                try {
                    Script script = ctx.compileString(source, "<stdin>", lineno, null);
                    if (script != null) {
                        Object result = script.exec(ctx, scope);
                        try {
                            ps.println(Context.toString(result));
                        } catch (RhinoException rex) {
                            ToolErrorReporter.reportException(
                                    ctx.getErrorReporter(), rex);
                        }
                    }
                } catch (RhinoException rex) {
                    ToolErrorReporter.reportException(
                            ctx.getErrorReporter(), rex);
                    exitCode = EXITCODE_RUNTIME_ERROR;
                } catch (VirtualMachineError ex) {
                    // Treat StackOverflow and OutOfMemory as runtime errors
                    ex.printStackTrace();
                    String msg = ToolErrorReporter.getMessage(
                            "msg.uncaughtJSException", ex.toString());
                    Context.reportError(msg);
                    exitCode = EXITCODE_RUNTIME_ERROR;

                }
            }
        }
        ps.println();
        ps.flush();
    }
}
