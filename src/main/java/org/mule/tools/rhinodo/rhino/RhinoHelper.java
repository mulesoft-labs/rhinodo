package org.mule.tools.rhinodo.rhino;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeObject;

import java.util.Map;

public class RhinoHelper {

    public  NativeObject mapToNativeObject(Map<String,Object> config) {
        NativeObject nobj = new NativeObject();
        for (Map.Entry<String, Object> entry : config.entrySet()) {
            nobj.defineProperty(entry.getKey(), Context.javaToJS(entry.getValue(), nobj), NativeObject.READONLY);
        }
        return nobj;
    }
}
