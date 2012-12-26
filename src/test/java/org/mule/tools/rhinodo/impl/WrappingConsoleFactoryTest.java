/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.impl;

import org.junit.Test;
import org.mozilla.javascript.*;
import org.mozilla.javascript.tools.shell.Global;
import org.mule.tools.rhinodo.impl.console.SystemOutConsole;
import org.mule.tools.rhinodo.impl.console.WrappingConsoleFactory;

import static org.junit.Assert.assertFalse;

public class WrappingConsoleFactoryTest {
    @Test
    public void testGetConsoleAsScriptable() throws Exception {

        final Global globalScope = new Global();

        final WrappingConsoleFactory wrappingConsoleFactory =
                new WrappingConsoleFactory(new SystemOutConsole());

        ContextFactory contextFactory = new ContextFactory();
        contextFactory.call(new ContextAction() {
            @Override
            public Object run(Context cx) {
                cx.initStandardObjects(globalScope);
                Scriptable consoleAsScriptable = wrappingConsoleFactory.getConsoleAsScriptable(globalScope);

                Function log = ScriptableObject.getTypedProperty(consoleAsScriptable, "log", Function.class);
                Function apply = ScriptableObject.getTypedProperty(log, "apply", Function.class);

                assertFalse(Scriptable.NOT_FOUND.equals(apply));

                return null;
            }
        });




    }


}
