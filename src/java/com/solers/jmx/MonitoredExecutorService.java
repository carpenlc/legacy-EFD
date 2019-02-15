/****************************************************************
 *
 * Solers, Inc. as the author of Enterprise File Delivery 2.1 (EFD 2.1)
 * source code submitted herewith to the Government under contract
 * retains those intellectual property rights as set forth by the Federal 
 * Acquisition Regulations agreement (FAR). The Government has 
 * unlimited rights to redistribute copies of the EFD 2.1 in 
 * executable or source format to support operational installation 
 * and software maintenance. Additionally, the executable or 
 * source may be used or modified for by third parties as 
 * directed by the government.
 *
 * (c) 2009 Solers, Inc.
 ***********************************************************/
package com.solers.jmx;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.solers.jmx.registry.AutoRegister;
import com.solers.jmx.registry.AutoRegisterObjectName;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
@AutoRegister(category="Executors")
public class MonitoredExecutorService implements ExecutorService, ExecutorStatus {

    /**
     * Default to an unbounded queue
     */
    private static final int DEFAULT_QUEUE_SIZE = -1;
    
    /**
     * Default to a single thread pool
     */
    private static final int DEFAULT_POOL_SIZE = 1;
    
    private static final MathContext ROUNDING = new MathContext(3);
    private static final BigDecimal ONEHUNDRED = new BigDecimal(100);
    
    private final ThreadPoolExecutor delegate;
    private final int queueSize;
    private final long createTime;
    private final String name;
    
    /**
     * @param poolSize Number of threads to keep in the pool
     * @param queueSize Work queue size, -1 to be unbounded
     * @param name Thread name
     */
    public MonitoredExecutorService(int poolSize, int queueSize, String name) {
        this.queueSize = queueSize;
        BlockingQueue<Runnable> workQueue;
        if (queueSize == -1) {
            workQueue = new LinkedBlockingQueue<Runnable>();
        } else {
            workQueue = new LinkedBlockingQueue<Runnable>(queueSize);
        }
        delegate = new ThreadPoolExecutor(poolSize, poolSize, 0L, TimeUnit.MILLISECONDS, workQueue, new NamingThreadFactory(name));
        createTime = System.currentTimeMillis();
        this.name = name;
    }
    
    /**
     * Default to an unbounded queue and a single threaded executor
     */
    public MonitoredExecutorService(String name) {
        this(DEFAULT_POOL_SIZE, DEFAULT_QUEUE_SIZE, name);
    }
    
    @AutoRegisterObjectName
    public String getName() {
        return name;
    }
    
    @Override
    public long getPoolSize() {
        return delegate.getMaximumPoolSize();
    }

    @Override
    public long getQueueSize() {
        return queueSize;
    }

    @Override
    public double getPercentFull() {
        if (queueSize == -1) {
            return -1.0;
        }
        return new BigDecimal(delegate.getQueue().size()).divide(new BigDecimal(queueSize), ROUNDING).multiply(ONEHUNDRED, ROUNDING).doubleValue();
    }

    @Override
    public long getProcessedTasks() {
        return delegate.getCompletedTaskCount();
    }

    @Override
    public long getActiveTasks() {
        return delegate.getActiveCount();
    }

    @Override
    public double getThroughput() {
        long elapsed = System.currentTimeMillis() - createTime;
        return new BigDecimal(getProcessedTasks()).divide(new BigDecimal(elapsed), ROUNDING).multiply(ONEHUNDRED, ROUNDING).doubleValue();
    }

    @Override
    public long getWaitingTasks() {
        return delegate.getQueue().size();
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return delegate.awaitTermination(timeout, unit);
    }

    public void execute(Runnable command) {
        delegate.execute(command);
    }

    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return delegate.invokeAll(tasks, timeout, unit);
    }

    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return delegate.invokeAll(tasks);
    }

    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return delegate.invokeAny(tasks, timeout, unit);
    }

    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return delegate.invokeAny(tasks);
    }

    public boolean isShutdown() {
        return delegate.isShutdown();
    }

    public boolean isTerminated() {
        return delegate.isTerminated();
    }

    public void shutdown() {
        delegate.shutdown();
    }

    public List<Runnable> shutdownNow() {
        return delegate.shutdownNow();
    }

    public <T> Future<T> submit(Callable<T> task) {
        return delegate.submit(task);
    }

    public <T> Future<T> submit(Runnable task, T result) {
        return delegate.submit(task, result);
    }

    public Future<?> submit(Runnable task) {
        return delegate.submit(task);
    }
    
    protected void finalize() {
        shutdown();
    }
    
    private static class NamingThreadFactory implements ThreadFactory {
        private final String name;
        
        private NamingThreadFactory(String name) {
            this.name = name;
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, name);
        }
    }
}
