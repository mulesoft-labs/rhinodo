package org.mule.tools.rhinodo.api;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.tools.shell.Global;

public interface Runnable {
    void executeJavascript(Context ctx, Global global);
}
