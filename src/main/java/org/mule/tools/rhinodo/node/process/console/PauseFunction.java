/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.rhinodo.node.process.console;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

public class PauseFunction extends BaseFunction {
    private final boolean[] shouldIPause;

    public PauseFunction(boolean[] shouldIPause) {
        this.shouldIPause = shouldIPause;
    }

    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        shouldIPause[0] = true;
        return Undefined.instance;
    }
}
