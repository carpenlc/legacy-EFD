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

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
@MXBean
public interface StatusService {
    
    /**
     * @param contentSetId
     * @return The status about the given supplier content set
     */
    SupplierStatus getSupplierStatus(Long contentSetId);
    
    /**
     * @param contentSetId
     * @return The status about the given consumer content set
     */
    ConsumerStatus getConsumerStatus(Long contentSetId);
    
    /**
     * @return Status of the current synchronizations
     */
    List<CurrentSynchronization> getCurrentSynchronizations();
}
