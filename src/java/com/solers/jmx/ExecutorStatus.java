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

import javax.management.MXBean;

/**
 * Metrics for a given ExecutorService
 * 
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
@MXBean
public interface ExecutorStatus {
    
    /**
     * @return If the work queue is bounded, return approximately how full the work queue is.  Otherwise -1
     */
    double getPercentFull();
    
    /**
     * @return The approximate number of processed tasks
     */
    long getProcessedTasks();
    
    /**
     * @return The approximate number of tasks currently waiting to be executed
     */
    long getWaitingTasks();
    
    /**
     * @return The approximate number of tasks current executing
     */
    long getActiveTasks();
    
    /**
     * @return The ratio of completed tasks / executors lifetime
     */
    double getThroughput();
    
    /**
     * @return Work queue size or -1 if unbounded
     */
    long getQueueSize();
    
    /**
     * @return Thread pool size
     */
    long getPoolSize();
}
