package org.simplelogic.executorservice;

import com.lmax.disruptor.*;
import com.lmax.disruptor.util.Util;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

/**
 * Created by paggarwal on 10/28/14.
 */
public class RingBufferExecutorService extends AbstractExecutorService implements EventTranslatorOneArg<ExecutorEvent, Runnable> {
    private final RingBuffer<ExecutorEvent> ringBuffer;
    private final Thread[] threads;
    private Thread shutdownThread;
    private final WorkProcessor<ExecutorEvent>[] workProcessors;
    AtomicBoolean shuttingDown = new AtomicBoolean(false);
    AtomicBoolean terminated = new AtomicBoolean(false);

    public RingBufferExecutorService(int queueSize, int noOfThreads, String threadNamePrefix) {
        queueSize = findNextPositivePowerOfTwo(queueSize);
        ringBuffer = RingBuffer.createMultiProducer(new ExecutorEventFactory(), queueSize, new YieldingWaitStrategy());
        threads = new Thread[noOfThreads];
        workProcessors = new WorkProcessor[noOfThreads];
        ThreadFactory factory = new NamingThreadFactory(threadNamePrefix);

        Sequence workSequence = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
        SequenceBarrier barrier = ringBuffer.newBarrier();

        for(int i=0; i< noOfThreads; i++) {
            ExecutorEventHandler handler = new ExecutorEventHandler();
            workProcessors[i] = new WorkProcessor<ExecutorEvent>(ringBuffer,
                    barrier,
                    handler,
                    handler,
                    workSequence);
            threads[i] = factory.newThread(workProcessors[i]);
        }
        ringBuffer.addGatingSequences(getWorkerSequences());

        for(Thread thread: threads) {
            thread.start();
        }
    }


    @Override
    public void shutdown() {
        if(shuttingDown.compareAndSet(false, true)) {
            shutdownThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Sequence[] workerSequences = getWorkerSequences();
                    while (ringBuffer.getCursor() > Util.getMinimumSequence(workerSequences)) {
                        Thread.yield();
                    }

                    for (WorkProcessor<?> processor : workProcessors) {
                        processor.halt();
                    }
                    shuttingDown.set(false);
                    terminated.set(true);
                }
            });
            shutdownThread.start();
        }
    }

    @Override
    public List<Runnable> shutdownNow() {
        throw new UnsupportedOperationException("Operation not supported");
    }

    @Override
    public boolean isShutdown() {
        return shuttingDown.get();
    }

    @Override
    public boolean isTerminated() {
        return terminated.get();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        if(shuttingDown.get()) {
            shutdownThread.join(TimeUnit.MILLISECONDS.convert(timeout, unit));
            return !shutdownThread.isAlive();
        } else {
            return false;
        }
    }

    @Override
    public void execute(Runnable command) {
        if(!isShutdown()) {
            ringBuffer.publishEvent(this, command);
        } else {
            throw new IllegalStateException("Cannot add new tasks when executor service is shutting down");
        }
    }

    @Override
    public void translateTo(ExecutorEvent event, long sequence, Runnable runnable) {
        event.setRunnable(runnable);
    }

    private static int findNextPositivePowerOfTwo(final int value) {
        return 1 << (32 - Integer.numberOfLeadingZeros(value - 1));
    }

    private Sequence[] getWorkerSequences() {
        Sequence[] sequences = new Sequence[workProcessors.length];
        for(int i=0;i<workProcessors.length; i++) {
            sequences[i] = workProcessors[i].getSequence();
        }
        return sequences;
    }
}
