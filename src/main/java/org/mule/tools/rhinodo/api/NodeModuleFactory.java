package org.mule.tools.rhinodo.api;

import java.util.Collection;

public interface NodeModuleFactory {
    Collection<? extends NodeModule> getModules();
}
