import com.beust.jcommander.internal.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.simplelogic.executorservice.RingBufferExecutorService;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.testng.Assert.fail;

/**
 * Created by paggarwal on 10/28/14.
 */
public class RingBufferExecutorServiceTest {
    private static final Logger log = LogManager.getLogger(RingBufferExecutorServiceTest.class);
    private RingBufferExecutorService ringBufferExecutorService;

    @BeforeMethod
    public void beforeMethod() {
        ringBufferExecutorService = new RingBufferExecutorService(3,3, "RingBufferExecutor");
    }

    @Test
    public void executeTest() throws InterruptedException {
        ringBufferExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                log.info("Hello 1");
            }
        });
        ringBufferExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                log.info("Hello 2");
            }
        });
        ringBufferExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                log.info("Hello 3");
            }
        });
        ringBufferExecutorService.shutdown();
        ringBufferExecutorService.awaitTermination(1000, TimeUnit.MILLISECONDS);
    }

    @Test
    public void callableTest() throws InterruptedException, ExecutionException {
        Future<Integer> future1 = ringBufferExecutorService.submit(new Callable<Integer>() {
            @Override
            public Integer call() {
                log.info("Executing Callable 1");
                return 1;
            }
        });

        Future<Integer> future2 = ringBufferExecutorService.submit(new Callable<Integer>() {
            @Override
            public Integer call() {
                log.info("Executing Callable 2");
                return 2;
            }
        });

        Future<Integer> future3 = ringBufferExecutorService.submit(new Callable<Integer>() {
            @Override
            public Integer call() {
                log.info("Executing Callable 3");
                return 3;
            }
        });
        log.info(future1.get());
        log.info(future2.get());
        log.info(future3.get());
        ringBufferExecutorService.shutdown();
        ringBufferExecutorService.awaitTermination(1000, TimeUnit.MILLISECONDS);
    }

    @Test
    public void invokeAllTest() throws InterruptedException, ExecutionException {
        List<Callable<Integer>> callables = new ArrayList<Callable<Integer>>();
        callables.add(new Callable<Integer>() {
            @Override
            public Integer call() {
                log.info("Executing Callable 1");
                return 1;
            }
        });
        callables.add(new Callable<Integer>() {
            @Override
            public Integer call() {
                log.info("Executing Callable 2");
                return 2;
            }
        });
        callables.add(new Callable<Integer>() {
            @Override
            public Integer call() {
                log.info("Executing Callable 3");
                return 3;
            }
        });

        List<Future<Integer>> futures = ringBufferExecutorService.invokeAll(callables);
        for(Future<Integer> future: futures) {
            log.info(future.get());
        }
        ringBufferExecutorService.shutdown();
        ringBufferExecutorService.awaitTermination(1000, TimeUnit.MILLISECONDS);
    }

    @Test
    public void timeoutTest() throws InterruptedException, ExecutionException {
        Future<Object> future = ringBufferExecutorService.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                Thread.sleep(10000);
                log.info("Executing Callable");
                return null;
            }
        });
        try {
            future.get(1000,TimeUnit.MILLISECONDS);
            fail("Timeout didn't happen");
        } catch (TimeoutException e) {
            future.cancel(true);
        }
        ringBufferExecutorService.shutdown();
        ringBufferExecutorService.awaitTermination(1000, TimeUnit.MILLISECONDS);
    }
}
