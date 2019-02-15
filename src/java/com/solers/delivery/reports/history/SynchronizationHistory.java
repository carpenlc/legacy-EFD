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
package com.solers.delivery.reports.history;

import java.util.Date;

import com.solers.util.PaginatedList;


/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public interface SynchronizationHistory {
    
    int PAGE_SIZE = 20;
    
    /**
     * Find all synchronizations for a given content set that match the given 
     * parameters
     * @param contentSetId
     * @param startTime 
     * @param endTime
     * @param showEmptyRecords If true, include "empty" synchronizations in the result set. An empty
     * synchronization is one where nothing happens, e.g. there are 0 adds, 0 updates and 0 deletes.
     * @return
     */
    PaginatedList<Synchronization> getSynchronizations(Long contentSetId, Date startTime, Date endTime, boolean showEmptyRecords, int pageSize);
    
    /**
     * Find all synchronizations that ended after {@code endTime}
     * @param contentSetId
     * @param endTime
     * @param showEmptyRecords
     * @return
     */
    PaginatedList<Synchronization> getSynchronizationsAfter(Long contentSetId, Date endTime, boolean showEmptyRecords, int pageSize);
    
    /**
     * Get the details about a given synchronization
     * @param contentSetId
     * @param synchronizationId
     * @param action Either ADD, UPDATE, DELETE or null to show all
     * @param path A full or partial filename path
     * @return
     * @see com.solers.delivery.event.ContentEvent.ContentEventAction
     */
    PaginatedList<ReportDetail> getSynchronizationDetails(Long contentSetId, String synchronizationId, String action, String path, int pageSize);
    
    /**
     * @param contentSetId
     * @param id
     * @return The details for the given synchronization
     */
    Synchronization getSynchronization(Long contentSetId, String id);
}
