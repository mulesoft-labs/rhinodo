/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.node.fs;

import org.mozilla.javascript.*;
import org.mule.tools.rhinodo.tools.JarURIHelper;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarEntry;

public class ReaddirSync extends BaseFunction {
    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        if ( args.length != 1) {
            throw new RuntimeException("Only readdirSync with 1 parameter supported");
        }
        String dir = Context.toString(args[0]);
        File file = new File(dir).getAbsoluteFile();

        String[] list = file.list();
        if ( list == null ) {
            return Undefined.instance;
        }
        Object[] objects = new Object[list.length];
        System.arraycopy(list, 0, objects, 0, list.length);

        return cx.newArray(scope, objects);

    }
}
