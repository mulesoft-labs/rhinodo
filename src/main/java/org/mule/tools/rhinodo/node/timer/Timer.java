package org.mule.tools.rhinodo.node.timer;

public class Timer {
    private boolean shouldExecute = true;

    public boolean shouldExecute() {
        return shouldExecute;
    }

    public void setShouldExecute(boolean shouldExecute) {
        this.shouldExecute = shouldExecute;
    }
}
