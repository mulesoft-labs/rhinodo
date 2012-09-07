/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.api;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.tools.shell.Global;

public interface Runnable {
    void executeJavascript(Context ctx, Global global);
}
