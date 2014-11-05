package org.simplelogic.executorservice;

import java.util.concurrent.Callable;

/**
 * Created by paggarwal on 10/28/14.
 */
public class ExecutorEvent {
    private Runnable runnable;

    public void setRunnable(Runnable runnable) {
        this.runnable = runnable;
    }

    public Runnable getRunnable() {
        return runnable;
    }
}
