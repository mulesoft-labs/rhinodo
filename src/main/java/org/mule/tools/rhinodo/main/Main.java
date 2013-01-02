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
import org.mule.tools.rhinodo.impl.Rhinodo;

import java.io.*;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;

public class Main {
    private static final int EXITCODE_RUNTIME_ERROR = 1;
    public static final String PROMPT = "> ";
    private Context ctx;
    private static boolean debug = true;
    private InputStream in;

    public Main(InputStream in) {
        this.in = in;
    }

    public static void main(String[] args) throws FileNotFoundException {
        if (debug) {
            System.out.println("Called with " + Arrays.toString(args));
        }

        InputStream in;
        /* Read from a file */
        if (args.length > 0 && !"".equals(args[0].trim())) {
            in = new FileInputStream(args[0]);
        } else {
            in = System.in;
        }

        String userHome = System.getProperty("user.home");
        final Main main = new Main(in);
        Rhinodo
                .create()
                .destDir(new File(new File(userHome), ".rhinodo"))
                .build(new BaseFunction(){
                    @Override
                    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                        main.executeJavascript(cx,scope);
                        return Undefined.instance;
                    }
                });
    }

    public void executeJavascript(Context ctx, Scriptable globalWannaBe) {
        this.ctx = ctx;
        ToolErrorReporter toolErrorReporter = new ToolErrorReporter(true);
        ctx.setErrorReporter(toolErrorReporter);

        Global global = (Global) globalWannaBe;
        global.setIn(in);

        int exitCode;
        Scriptable scope = global;
        PrintStream ps = System.err;
        BufferedReader in;
        try
        {
            in = new BufferedReader(new InputStreamReader(global.getIn(),
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
                if (lineno == 1 && newline.startsWith("#!")) {
                    source = "//" + newline + "\n";
                }
                lineno++;
                if (ctx.stringIsCompilableUnit(source))
                    break;


                ps.print(PROMPT);
            }
            if (!hitEOF) {
                if (debug) {
                    System.out.println("Source to compile: [" + source.trim() + "]");
                }
                try {
                    Script script = ctx.compileString(source, "<stdin>", lineno - 1, null);
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
