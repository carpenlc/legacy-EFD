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

import com.solers.delivery.BaseLuceneTestCase;
import com.solers.delivery.event.ContentEvent;
import com.solers.delivery.event.SynchronizationEvent;
import com.solers.delivery.event.ContentEvent.ContentEventAction;
import com.solers.delivery.event.ContentEvent.ContentEventResult;
import com.solers.delivery.reports.history.ReportDetail;
import com.solers.delivery.reports.history.Synchronization;
import com.solers.delivery.reports.history.SynchronizationHistory;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class LuceneConsumerEventListenerTestCase extends BaseLuceneTestCase {
    
    public void testReceiveEvents() throws Exception {
        long contentSetId = 1l;
        String syncKey = "testReceiveEvents";
        String syncId = SynchronizationEvent.getId(syncKey, contentSetId);
        
        EventListener listener = new LuceneConsumerEventListener(helper, 2, 1000);
        
        ContentEvent eventOne = new ContentEvent(contentSetId);
        eventOne.setBytesRequested(2l);
        eventOne.setBytesManipulated(2l);
        eventOne.setAction(ContentEventAction.ADD);
        eventOne.setPath("test path");
        eventOne.setResult(ContentEventResult.SUCCESS);
        eventOne.setSynchronizationId(syncId);
        listener.received(eventOne);
        
        ContentEvent eventTwo = new ContentEvent(contentSetId);
        eventTwo.setBytesRequested(4l);
        eventTwo.setBytesManipulated(4l);
        eventTwo.setAction(ContentEventAction.UPDATE);
        eventTwo.setPath("second path");
        eventTwo.setResult(ContentEventResult.SUCCESS);
        eventTwo.setSynchronizationId(syncId);
        listener.received(eventTwo);
        
        waitForListener();
        
        List<ReportDetail> data = history.getSynchronizationDetails(contentSetId, syncId, null, null, SynchronizationHistory.PAGE_SIZE);
        assertEquals(2, data.size());
        Iterator<ReportDetail> iterator = data.iterator();
        assertEquals(eventTwo, iterator.next());
        assertEquals(eventOne, iterator.next());
    }
    
    public void testCompleteSynchronization() {
        long contentSetId = 1l;
        String syncKey = "testCompleteSynchronization";
        String syncId = SynchronizationEvent.getId(syncKey, contentSetId);
        
        EventListener listener = new LuceneConsumerEventListener(helper, 2, 1000);
        SynchronizationEvent event = new SynchronizationEvent(syncKey, contentSetId);
        event.completed(2);
        
        listener.consumerSynchronizationCompleted(event);
        
        waitForListener();
        
        Synchronization sync = history.getSynchronization(contentSetId, syncId);
        assertEquals(event, sync);
    }
}
