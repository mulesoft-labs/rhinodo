/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.node.fs;

import org.mozilla.javascript.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;

public class StatSync extends BaseFunction {
    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {

        if ( args.length != 1) {
            throw new RuntimeException("Only statSync 1 parameter supported");
        }

        final File file = new File(Context.toString(args[0]));

        if ( !new File(Context.toString(args[0])).exists() ) {
            throw new WrappedException(new RuntimeException(String.format("Error: file [%s] does not exist", args[0])));
        }
        return Context.javaToJS(new Stats(file), scope);
    }
}
