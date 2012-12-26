/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.node.fs;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mule.tools.rhinodo.tools.JarURIHelper;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;

public class ReaddirSync extends BaseFunction {
    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        if ( args.length != 1) {
            throw new RuntimeException("Only readdirSync with 1 parameter supported");
        }

        String dir = args[0].toString();

        URI uri = new File(dir).toURI();

        String [] lst;

        if ( "file".equals(uri.getScheme())) {
            lst = new File(uri.getPath()).list();
        } else if("jar".equals(uri.getScheme()) ) {
            List<String> list = new ArrayList<String>();
            JarURIHelper jarURIHelper = new JarURIHelper(uri);
            for (JarEntry jarEntry : jarURIHelper.getListOfJarFiles()) {
                String name = jarEntry.getName();
                if (name.startsWith(jarURIHelper.getInsideJarRelativePath()) &&
                        !name.equals(jarURIHelper.getInsideJarRelativePath()) &&
                    !name.equals(jarURIHelper.getInsideJarRelativePath() + "/")) {
                    list.add(name.substring(jarURIHelper.getInsideJarRelativePath().length() + 1));
                }
            }
            lst = list.toArray(new String[list.size()]);
        } else {
            throw new IllegalStateException(String.format("Scheme [%s] not supported", uri.getScheme()));
        }

        Scriptable newLst = cx.newArray(scope, 0);

        if ( lst == null ) {
            throw new IllegalArgumentException("Error listing directory: " + dir);
        }

        for (String aLst : lst) {
            Function push = (Function) newLst.getPrototype().get("push", newLst);
            push.call(cx,scope,newLst, new Object[] {aLst});
        }

        return newLst;

    }
}
