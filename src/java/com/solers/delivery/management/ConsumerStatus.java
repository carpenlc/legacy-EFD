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
package com.solers.delivery.management;

import java.util.List;

import javax.management.MXBean;

import com.solers.delivery.transport.http.TransferStatus;

/**
 * Consumer-specific management interface
 * 
 * @author gvanore
 */
@MXBean 
public interface ConsumerStatus {
    /**
     * @return percent complete as a discrete percent, 0 - 100.
     */
    double getPercentCompleted();

    /**
     * @return state of task, based on SynchronizationState
     */
    String getState();

    /**
     * @return the amount of files in the consumer, total, or
     * -1 if the current count is not available
     */
    long getItemCount();
    
    /**
     * @return the size of the managed consumer, or -1 if the
     * current count is not available
     */
    long getSize();
    
    /**
     * @return the last time the task ran, or null if a 
     * synchronization is in progress
     */
    long getLastRuntime();
    
    /**
     * @return if content set is enabled
     */
    boolean isEnabled();
    
    /**
     * @return the amount of time elapsed, in millis, since the start
     */
    long getElapsedTime();
    
    /**
     * @return the estimated amount of time until the next synchronization
     */
    long getNextEstimatedRuntime();
    
    /**
     * @return Estimated remaining time for the synchronization
     */
    long getRemainingTime();

    /**
     * @return the list of in-progress tranfer status which includes files and
     * percentage completed
     */
    List<TransferStatus> getCurrentTransfers();
     
    long getItemsAdded();

    long getItemsUpdated();

    long getItemsDeleted();
    
    long getItemsAddedRemaining();
    
    long getItemsUpdatedRemaining();
    
    long getItemsDeletedRemaining();

    long getBytesAdded();

    long getBytesUpdated();

    long getBytesDeleted();
    
    long getBytesAddedRemaining();
    
    long getBytesUpdatedRemaining();
    
    long getBytesDeletedRemaining();
    
    long getFailures();
    
    long getBytesFailed();
}
