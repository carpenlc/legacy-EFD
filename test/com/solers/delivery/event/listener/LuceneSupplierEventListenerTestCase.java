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
package com.solers.delivery.event.listener;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import com.solers.delivery.BaseLuceneTestCase;
import com.solers.delivery.event.ContentEvent;
import com.solers.delivery.event.SynchronizationEvent;
import com.solers.delivery.event.ContentEvent.ContentEventAction;
import com.solers.delivery.event.ContentEvent.ContentEventResult;
import com.solers.delivery.lucene.LuceneHelper;
import com.solers.delivery.reports.history.ReportDetail;
import com.solers.delivery.reports.history.Synchronization;
import com.solers.delivery.reports.history.SynchronizationHistory;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class LuceneSupplierEventListenerTestCase extends BaseLuceneTestCase {

    public void testSynchronizationStarted() throws Exception {
        long contentSetId = 1l;
        String syncKey = "testSynchronizationStarted";
        SynchronizationEvent event = new SynchronizationEvent(syncKey, contentSetId);
        event.completed(2);
        
        EventListener listener = new LuceneSupplierEventListener(helper, history, 100, 1000);
        listener.supplierSynchronizationStarted(event);
        
        waitForListener();
        
        Synchronization sync = history.getSynchronization(contentSetId, event.getId());
        assertNotNull(sync);
    }
    
    public void testSupplyEvents() throws Exception {
        long contentSetId = 1l;
        String syncKey = "testSupplyEvents";
        String syncId = SynchronizationEvent.getId(syncKey, contentSetId);
        EventListener listener = new LuceneSupplierEventListener(helper, history, 2, 1000);
        
        ContentEvent event = new ContentEvent(contentSetId);
        event.setBytesManipulated(2l);
        event.setAction(ContentEventAction.SUPPLY);
        event.setPath("test path");
        event.setResult(ContentEventResult.SUCCESS);
        event.setSynchronizationId(syncId);
        listener.supplied(event);
        
        ContentEvent eventTwo = new ContentEvent(contentSetId);
        eventTwo.setBytesManipulated(4l);
        eventTwo.setAction(ContentEventAction.SUPPLY);
        eventTwo.setPath("second path");
        eventTwo.setResult(ContentEventResult.SUCCESS);
        eventTwo.setSynchronizationId(syncId);
        listener.supplied(eventTwo);
        
        waitForListener();
        
        List<ReportDetail> data = history.getSynchronizationDetails(contentSetId, syncId, null, null, SynchronizationHistory.PAGE_SIZE);
        assertEquals(2, data.size());
        Iterator<ReportDetail> iterator = data.iterator();
        assertEquals(eventTwo, iterator.next());
        assertEquals(event, iterator.next());
    }
    
    public void testCompleteSynchronization() throws Exception {
        long contentSetId = 1l;
        String syncKey = "testCompleteSynchronization";
        String syncId = SynchronizationEvent.getId(syncKey, contentSetId);
        SynchronizationEvent event = new SynchronizationEvent(syncKey, contentSetId);
        event.completed(2);
        
        CountDownLatch latch = new CountDownLatch(1);
        CountdownTask task = new CountdownTask();
        task.latch = latch;
        
        TestSupplierListener listener = new TestSupplierListener(helper, history, 75, 1000);
        listener.postComplete = task;
        listener.supplierSynchronizationStarted(event);
        
        int count = 112;
        
        for (int i=0; i < count; i++) {
            ContentEvent contentEvent = new ContentEvent(contentSetId);
            contentEvent.setBytesManipulated(2l);
            contentEvent.setAction(ContentEventAction.SUPPLY);
            contentEvent.setPath("test path "+i);
            contentEvent.setResult(ContentEventResult.SUCCESS);
            contentEvent.setSynchronizationId(syncId);
            contentEvent.setBytesRequested(2l);
            listener.supplied(contentEvent);
        }
        
        event = new SynchronizationEvent(syncKey, contentSetId);
        event.completed(2);
        listener.supplierSynchronizationCompleted(event);
        
        latch.await();
        
        List<ReportDetail> data = history.getSynchronizationDetails(contentSetId, syncId, null, null, SynchronizationHistory.PAGE_SIZE);
        assertEquals(count, data.size());
        
        Synchronization sync = history.getSynchronization(contentSetId, syncId);
        assertEquals(count, sync.getAdds());
        assertEquals(count*2, sync.getBytesAdded());
    }
    
    public void testSynchronizationWithFailedBytes() throws Exception {
        long contentSetId = 1l;
        String syncKey = "testSynchronizationWithFailedBytes";
        String syncId = SynchronizationEvent.getId(syncKey, contentSetId);
        SynchronizationEvent event = new SynchronizationEvent(syncKey, contentSetId);
        event.completed(2);
        
        CountDownLatch latch = new CountDownLatch(1);
        CountdownTask task = new CountdownTask();
        task.latch = latch;
        
        TestSupplierListener listener = new TestSupplierListener(helper, history, 75, 1000);
        listener.postComplete = task;
        listener.supplierSynchronizationStarted(event);
        
        ContentEvent contentEvent = new ContentEvent(contentSetId);
        contentEvent.setBytesRequested(10);
        contentEvent.setBytesManipulated(5);
        contentEvent.setAction(ContentEventAction.SUPPLY);
        contentEvent.setPath("test path 1");
        contentEvent.setResult(ContentEventResult.TRANSFER_FAILED);
        contentEvent.setSynchronizationId(syncId);
        listener.supplied(contentEvent);
        
        contentEvent = new ContentEvent(contentSetId);
        contentEvent.setBytesRequested(20);
        contentEvent.setBytesManipulated(20);
        contentEvent.setAction(ContentEventAction.SUPPLY);
        contentEvent.setPath("test path 2");
        contentEvent.setResult(ContentEventResult.SUCCESS);
        contentEvent.setSynchronizationId(syncId);
        listener.supplied(contentEvent);
        
        event = new SynchronizationEvent(syncKey, contentSetId);
        event.completed(2);
        listener.supplierSynchronizationCompleted(event);
        
        latch.await();
        
        List<ReportDetail> data = history.getSynchronizationDetails(contentSetId, syncId, null, null, SynchronizationHistory.PAGE_SIZE);
        assertEquals(2, data.size());
        
        Synchronization sync = history.getSynchronization(contentSetId, syncId);
        assertEquals(1, sync.getAdds());
        assertEquals(1, sync.getFailures());
        assertEquals(20, sync.getBytesAdded());
        assertEquals(10, sync.getBytesFailed());
    }
    
    public void testDifferentSynchronizationsSameContentSet() throws Exception {
        long contentSetId = 1;
        CountDownLatch latch = new CountDownLatch(2);
        CountdownTask task = new CountdownTask();
        task.latch = latch;
        
        TestSupplierListener listener = new TestSupplierListener(helper, history, 100, 1000);
        listener.postComplete = task;
        
        SynchronizationEvent first = new SynchronizationEvent(contentSetId);
        SynchronizationEvent second = new SynchronizationEvent(contentSetId);
        
        listener.supplierSynchronizationStarted(first);
        listener.supplierSynchronizationStarted(second);
        
        listener.supplied(newSupplyEvent(contentSetId, first.getId()));
        listener.supplied(newSupplyEvent(contentSetId, second.getId()));
        
        first.completed(2);
        second.completed(2);
        
        listener.supplierSynchronizationCompleted(first);
        listener.supplierSynchronizationCompleted(second);
        
        latch.await();
        //waitForListener();
        
        List<ReportDetail> data = history.getSynchronizationDetails(contentSetId, first.getId(), null, null, SynchronizationHistory.PAGE_SIZE);
        assertEquals(1, data.size());
        
        Synchronization sync = history.getSynchronization(contentSetId, first.getId());
        assertEquals(1, sync.getAdds());
        assertEquals(2, sync.getBytesAdded());
        
        data = history.getSynchronizationDetails(contentSetId, second.getId(), null, null, SynchronizationHistory.PAGE_SIZE);
        assertEquals(1, data.size());
        
        sync = history.getSynchronization(contentSetId, second.getId());
        assertEquals(1, sync.getAdds());
        assertEquals(2, sync.getBytesAdded());
        
    }
    
    private ContentEvent newSupplyEvent(long contentSetId, String syncId) {
        ContentEvent event = new ContentEvent(contentSetId);
        event.setAction(ContentEventAction.SUPPLY);
        event.setBytesManipulated(2L);
        event.setBytesRequested(2L);
        event.setPath("test");
        event.setResult(ContentEventResult.SUCCESS);
        event.setSynchronizationId(syncId);
        return event;
    }
    
    class TestSupplierListener extends LuceneSupplierEventListener {
        
        public Callable<Void> postComplete;
        
        public TestSupplierListener(LuceneHelper helper, SynchronizationHistory history, int batchSize, int flushTime) {
            super(helper, history, batchSize, flushTime);
        }

        @Override
        public void supplierSynchronizationCompleted(SynchronizationEvent event) {
            super.supplierSynchronizationCompleted(event);
            service.submit(postComplete);
        }
        
    }
    
    class CountdownTask implements Callable<Void> {
        public CountDownLatch latch;
        public Void call() {
            latch.countDown();
            return null;
        }
    }
}
