/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.node;

import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.tools.shell.Global;
import org.mule.tools.rhinodo.impl.JavascriptRunner;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.Assert.assertEquals;

public class PathTestCase {

    @Test
    public void basename() throws IOException {
        try {
            Context ctx = Context.enter();
            ctx.setLanguageVersion(170);

            final Global global = new Global();
            global.initStandardObjects(ctx,false);

            NativeObject exports = new NativeObject();
            global.put("exports", global, exports);

            InputStream resourceAsStream = JavascriptRunner.class.getClassLoader().getResourceAsStream("META-INF/env/path.js");
            ctx.evaluateReader(global,new InputStreamReader(resourceAsStream), "path",-1, null);

            Function function = (Function) exports.get("basename");
            Object result = (Object) function.call(ctx, global, exports, new Object[]{"hello/bye/now"});

            assertEquals("now", Context.toString(result));

        } finally {
            Context.exit();
        }
    }
}
