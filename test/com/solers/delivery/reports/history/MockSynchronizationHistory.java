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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.solers.delivery.event.ContentEvent.ContentEventResult;
import com.solers.util.PaginatedList;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class MockSynchronizationHistory implements SynchronizationHistory {
    
    public static final Date DATE = new Date();
    
    public Long contentSetId;
    public Date startTime;
    public Date endTime;
    public String host;
    public String path;
    public boolean showEmpty;
    
    public List<ReportDetail> details;
    
    public List<Synchronization> syncData;
    
    public Synchronization sync;

    @Override
    public PaginatedList<Synchronization> getSynchronizationsAfter(Long contentSetId, Date endTime, boolean showEmptyRecords, int pageSize) {
        return getSynchronizations(contentSetId, null, endTime, showEmptyRecords, pageSize);
    }

    @Override
    public PaginatedList<ReportDetail> getSynchronizationDetails(Long id, String synchronizationId, String action, String path, int pageSize) {
        this.contentSetId = id;
        this.path = path;
        
        if (details == null) {
            List<ReportDetail> data = new ArrayList<ReportDetail>();
            
            ReportDetail d = new ReportDetail();
            
            d.setAction("ADD");
            d.setPath("path");
            d.setSize(5000);
            d.setStatus(ContentEventResult.SUCCESS.value());
            d.setTimestamp(DATE.getTime());
            d.setTransferred(5000);
            data.add(d);
            
            return new MockPaginatedList<ReportDetail>(data);
        } else {
            return new MockPaginatedList<ReportDetail>(details);
        }
    }

    @Override
    public PaginatedList<Synchronization> getSynchronizations(Long contentSetId, Date startTime, Date endTime, boolean showEmptyRecords, int pageSize) {
        
        this.contentSetId = contentSetId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.showEmpty = showEmptyRecords;
        
        if (syncData == null) {
            List<Synchronization> result = new ArrayList<Synchronization>();
            
            Synchronization sync = new Synchronization();
            
            sync.setId("id-1");
            sync.setElapsedTime(100);
            sync.setBytesAdded(1);
            sync.setBytesUpdated(2);
            sync.setBytesDeleted(3);
            sync.setBytesFailed(4);
            sync.setAdds(4);
            sync.setHost("127.0.0.1");
            sync.setUpdates(5);
            sync.setDeletes(6);
            sync.setFailures(1);
            sync.setTimestamp(DATE.getTime());
            
            result.add(sync);
            
            return new MockPaginatedList<Synchronization>(result);
        } else {
            return new MockPaginatedList<Synchronization>(syncData);
        }
    }

    @Override
    public Synchronization getSynchronization(Long contentSetId, String id) {
        return sync;
    }
    
    private static class MockPaginatedList<T> extends PaginatedList<T> {

        List<T> data;
        public MockPaginatedList(List<T> data) {
            super(data.size());
            this.data = data;
            initialized(data.size());
        }

        @Override
        protected List<T> getSubList(int fromIndex, int toIndex) {
            return data.subList(fromIndex, toIndex);
        }

        @Override
        public void initialize() {
            
        }
        
    }
}
