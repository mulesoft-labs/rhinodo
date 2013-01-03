/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.node.fs;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class ReadFileSync extends BaseFunction {

    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        if ( args.length != 2 && args.length != 1 ) {
            throw new RuntimeException("Only readFile with 2 parameters supported");
        }

        final String file = Context.toString(args[0]);
        String encoding = args.length > 1 ? Context.toString(args[1]) : "UTF-8";

        try {
            return FileUtils.readFileToString(new File(file).getAbsoluteFile(), encoding);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
