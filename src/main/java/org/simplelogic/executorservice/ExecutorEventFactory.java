package org.simplelogic.executorservice;

import com.lmax.disruptor.EventFactory;

/**
 * Created by paggarwal on 10/28/14.
 */
public class ExecutorEventFactory implements EventFactory<ExecutorEvent> {
    @Override
    public ExecutorEvent newInstance() {
        return new ExecutorEvent();
    }
}
