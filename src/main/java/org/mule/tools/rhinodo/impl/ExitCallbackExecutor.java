/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.impl;

import org.mozilla.javascript.Function;

public class ExitCallbackExecutor {
    private Function function;

    public void add(Function function) {
        this.function = function;
    }

    public Function get() {
        return function;
    }
}
