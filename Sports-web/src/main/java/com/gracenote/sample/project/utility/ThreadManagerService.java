package com.gracenote.sample.project.utility;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;

/**
 * REST Web Service
 *
 * @author adebowale.samuel
 */
@RequestScoped
public class ThreadManagerService {

    private Executor executor;

    private static AtomicInteger nrOfRejectedJobs = new AtomicInteger(0);
    private final static int NR_OF_THREADS = 3;
    private final static int MAX_POOL_SIZE = 10;
    private final static int QUEUE_CAPACITY = 20;
    public static final String REFERER = "referer";

    /**
     * Creates a new instance of PaymentService
     */
    public ThreadManagerService() {
    }

    @PostConstruct
    public void initService() {
        setupThreadPools();
    }

    void setupThreadPools() {
        MonitorableThreadFactory monitorableThreadFactory = new MonitorableThreadFactory();

        RejectedExecutionHandler ignoringHandler = (Runnable r, ThreadPoolExecutor executor1) -> {
            int rejectedJobs = nrOfRejectedJobs.incrementAndGet();

            Logger.getLogger(ThreadManagerService.class.getName()).log(Level.SEVERE,
                    "Utility Jobs: {0} rejected. Number of rejected jobs: {1}", new Object[]{r, rejectedJobs});
        };

        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
        executor = new ThreadPoolExecutor(NR_OF_THREADS,
                MAX_POOL_SIZE, Integer.MAX_VALUE, TimeUnit.SECONDS, workQueue,
                monitorableThreadFactory, ignoringHandler);
    }

    public String createName(final String uri, final String referer) {
        return uri + "|" + referer;
    }

    public Executor getManagedExecutorService() {
        if (executor == null) {
            setupThreadPools();
        }
        return executor;
    }
    
    public void configureTimeout(final AsyncResponse asyncResponse){
        asyncResponse.setTimeout(40, TimeUnit.SECONDS);
        asyncResponse.setTimeoutHandler(r ->
                r.resume(Response.status(Response.Status.SERVICE_UNAVAILABLE)));
    }

}
