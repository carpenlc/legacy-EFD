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

import com.solers.delivery.content.status.CurrentSynchronization;

@MXBean
public interface SupplierStatus {
    /**
     * @return whether the content set is enabled or not.
     */
    boolean isEnabled();
    
    /**
     * @return the size, in bytes, of the files in the Supplier's root path
     */
    long getSize();
    
    /**
     * @return the number of files in the supplier content set
     */
    long getItemCount();
    
    /**
     * @return the time at which the last inventory ended
     */
    long getLastRuntime();
    
    /**
     * @return estimated time in milliseconds to the next inventory creation
     */
    long getNextEstimatedRuntime();
    
    /**
     * @return a string describing the current state of the Inventory Creation task.
     */
    String getState();
    
    /**
     * @return Current synchronizations for this supplier
     */
    List<CurrentSynchronization> getCurrentSynchronizations();
}
