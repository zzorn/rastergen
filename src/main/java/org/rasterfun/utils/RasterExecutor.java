package org.rasterfun.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Executor service optimized for raster generation.
 * Provides a static instance for convenience.
 */
public final class RasterExecutor {

    private final int MAXIMUM_THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 4;

    private final ExecutorService executor = new ThreadPoolExecutor(
            0, // Initial thread count
            MAXIMUM_THREAD_POOL_SIZE, // Limit maximum threads, no much point in having much more threads than cores that can calculate them.
            60L, TimeUnit.SECONDS, // Timeout until idle thread cleared
            new LinkedBlockingQueue<Runnable>()); // Queue tasks when there are no free threads for them

    private static RasterExecutor sharedInstance = new RasterExecutor();

    /**
     * @return a common executor optimized for raster calculation.
     */
    public static ExecutorService getSharedExecutor() {
        return sharedInstance.executor;
    }

    /**
     * @return the executor for this instance.
     */
    public ExecutorService getExecutor() {
        return executor;
    }
}
