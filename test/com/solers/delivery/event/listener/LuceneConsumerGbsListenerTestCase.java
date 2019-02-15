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
import com.solers.delivery.event.GBSUpdateCompleteEvent;
import com.solers.delivery.event.PendingGBSUpdateEvent;
import com.solers.delivery.event.SynchronizationEvent;
import com.solers.delivery.event.ContentEvent.ContentEventAction;
import com.solers.delivery.event.ContentEvent.ContentEventResult;
import com.solers.delivery.reports.history.ReportDetail;
import com.solers.delivery.reports.history.SynchronizationHistory;
import com.solers.lucene.DocumentCreator;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class LuceneConsumerGbsListenerTestCase extends BaseLuceneTestCase {
    
    public void testComplete() throws Exception {
        long contentSetId = 1l;
        String syncKey = "testReceiveEvents";
        String syncId = SynchronizationEvent.getId(syncKey, contentSetId);
        EventListener listener = new LuceneConsumerGbsListener(helper, 1, 1000);
        
        ContentEvent eventOne = new ContentEvent(contentSetId);
        eventOne.setBytesManipulated(0l);
        eventOne.setAction(ContentEventAction.ADD);
        eventOne.setPath("path");
        eventOne.setResult(ContentEventResult.PENDING_GBS);
        eventOne.setSynchronizationId(syncId);
        
        ContentEvent eventTwo = new ContentEvent(contentSetId);
        eventTwo.setBytesManipulated(0l);
        eventTwo.setAction(ContentEventAction.ADD);
        eventTwo.setPath("second path");
        eventTwo.setResult(ContentEventResult.PENDING_GBS);
        eventTwo.setSynchronizationId(syncId);
        
        helper.addDocument(helper.getIndex(contentSetId, syncId), DocumentCreator.create(eventOne));
        helper.addDocument(helper.getIndex(contentSetId, syncId), DocumentCreator.create(eventTwo));
        
        List<ReportDetail> data = history.getSynchronizationDetails(contentSetId, syncId, null, null, SynchronizationHistory.PAGE_SIZE);
        assertEquals(2, data.size());
        Iterator<ReportDetail> iterator = data.iterator();
        assertEquals(eventTwo, iterator.next());
        assertEquals(eventOne, iterator.next());
        
        PendingGBSUpdateEvent updateOne = new PendingGBSUpdateEvent(contentSetId, "", syncKey, "path", 2l, ContentEventAction.ADD, ContentEventResult.SUCCESS);
        listener.received(updateOne);
        PendingGBSUpdateEvent updateTwo = new PendingGBSUpdateEvent(contentSetId, "", syncKey, "second path", 5l, ContentEventAction.ADD, ContentEventResult.SUCCESS);
        listener.received(updateTwo);
        
        listener.gbsUpdateComplete(new GBSUpdateCompleteEvent(contentSetId, syncKey));
        
        try {
            Thread.sleep(2000);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        
        data = history.getSynchronizationDetails(contentSetId, syncId, null, null, SynchronizationHistory.PAGE_SIZE);
        assertEquals(2, data.size());
        iterator = data.iterator();
        assertEquals(updateTwo.getContentEvent(), iterator.next());
        assertEquals(updateOne.getContentEvent(), iterator.next());
    }
}
