package org.simplelogic.executorservice;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.WorkHandler;

/**
 * Created by paggarwal on 10/28/14.
 */
public class ExecutorEventHandler implements WorkHandler<ExecutorEvent>, ExceptionHandler {
    @Override
    public void onEvent(ExecutorEvent event) throws Exception {
        event.getRunnable().run();
    }

    @Override
    public void handleEventException(Throwable ex, long sequence, Object event) {

    }

    @Override
    public void handleOnStartException(Throwable ex) {

    }

    @Override
    public void handleOnShutdownException(Throwable ex) {

    }
}
