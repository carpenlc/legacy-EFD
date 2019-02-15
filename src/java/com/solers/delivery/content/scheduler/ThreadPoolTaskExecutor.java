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
package com.solers.delivery.content.scheduler;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;
import org.quartz.listeners.SchedulerListenerSupport;
import org.springframework.scheduling.SchedulingTaskExecutor;

import com.solers.util.NamedThreadFactory;

public class ThreadPoolTaskExecutor extends SchedulerListenerSupport implements SchedulingTaskExecutor {
    private static final Logger log = Logger.getLogger(ThreadPoolTaskExecutor.class);
    
    private ExecutorService executor;
    private Future<?> taskFuture;
    
    public ThreadPoolTaskExecutor() {
     	ThreadFactory threadFactory = new NamedThreadFactory();
    	executor = Executors.newSingleThreadExecutor(threadFactory);
    }   

    /**
     * Implementation of the Spring TaskExecutor interface, delegating to the 
     * Single thread ThreadPoolExecutor instance.
     * @param task
     * @see org.springframework.core.task.TaskExecutor#execute(Runnable)
     */
    @Override
    public void execute(Runnable task) {
        if (taskFuture != null && !taskFuture.isDone()) {
            log.debug("Previous task not done yet, skip, current thread:" + Thread.currentThread().getName() + ", executor:" + this);
        } else {
            log.debug("Executing task, current thread:" + Thread.currentThread().getName() + ", executor:" + this);            
            taskFuture = executor.submit(task);
        } 
    }

    /**
     * This task executor prefers short-lived work units.
     * @return 
     */
    
    @Override
    public boolean prefersShortLivedTasks() {
            return true;
    }

    @Override
    public void schedulerShutdown() {	    
        log.debug("scheduler shuting down:" + Thread.currentThread().getName());
        cancel();
    }

    @Override
    public void jobUnscheduled(String triggerName, String triggerGroup)  {       
        log.debug("jobUnscheduled:" + Thread.currentThread().getName());
    cancel();
    }

    private void cancel() {
        if (taskFuture != null) {
            taskFuture.cancel(true);
            taskFuture = null;
        }
    }

    @Override
    public <T extends Object> Future<T> submit(Callable<T> clbl)
    {
        Future<T> future = null;

        return future;
    }

    @Override
    public Future<?> submit(Runnable r)
    {
        Future<?> future = null;

        return future;
    }

    @Override
    public void execute(Runnable r, long l)
    {
    }
}