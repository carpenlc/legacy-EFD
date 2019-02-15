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

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.solers.delivery.BaseLuceneTestCase;
import com.solers.delivery.content.status.ConsumerProgress;
import com.solers.delivery.event.ContentEvent;
import com.solers.delivery.event.SynchronizationEvent;
import com.solers.delivery.event.ContentEvent.ContentEventAction;
import com.solers.delivery.event.ContentEvent.ContentEventResult;
import com.solers.lucene.DocumentCreator;
import com.solers.util.unit.TimeIntervalUnit;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class LuceneSynchronizationHistoryTestCase extends BaseLuceneTestCase {
    
    public void testGetSynchronizationsByEndDate() throws Exception {
        long contentSetId = 1l;
        String syncKey = "testGetSynchronizationsByEndDate";
        SynchronizationEvent event = new SynchronizationEvent(syncKey, contentSetId);
        event.setTimestamp(date(2008, Calendar.MARCH, 3).getTime());
        event.setProgress(createProgress());
        event.completed(TimeIntervalUnit.DAYS.toMillis(10));
        
        SynchronizationEvent emptyEvent = new SynchronizationEvent(syncKey, contentSetId);
        emptyEvent.setTimestamp(date(2008, Calendar.MARCH, 3).getTime());
        emptyEvent.setProgress(null);
        emptyEvent.completed(TimeIntervalUnit.DAYS.toMillis(10));
        
        helper.addDocument(helper.getIndex(contentSetId), DocumentCreator.create(event));
        helper.addDocument(helper.getIndex(contentSetId), DocumentCreator.create(emptyEvent));
        
        Date endTime = date(2008, Calendar.MARCH, 14);
        
        List<Synchronization> data = history.getSynchronizationsAfter(contentSetId, endTime, false, SynchronizationHistory.PAGE_SIZE);
        assertEquals(0, data.size());
        
        endTime = date(2008, Calendar.MARCH, 13);
        data = history.getSynchronizationsAfter(contentSetId, endTime, false, SynchronizationHistory.PAGE_SIZE);
        assertEquals(1, data.size());
        assertEquals(event, data.iterator().next());
        
        endTime = date(2008, Calendar.MARCH, 5);
        data = history.getSynchronizationsAfter(contentSetId, endTime, false, SynchronizationHistory.PAGE_SIZE);
        assertEquals(1, data.size());
        assertEquals(event, data.iterator().next());
        
        endTime = null;
        data = history.getSynchronizationsAfter(contentSetId, endTime, false, SynchronizationHistory.PAGE_SIZE);
        assertEquals(1, data.size());
        assertEquals(event, data.iterator().next());
        
        data = history.getSynchronizationsAfter(contentSetId, endTime, true, SynchronizationHistory.PAGE_SIZE);
        assertEquals(2, data.size());
    }
    
    public void testGetSynchronizationsNotFound() {
        List<Synchronization> result = history.getSynchronizations(1l, null, null, false, SynchronizationHistory.PAGE_SIZE);
        assertNotNull(result);
        assertEquals(0, result.size());
    }
    
    public void testGetSynchronizationNotFound() {
        Synchronization sync = history.getSynchronization(1l, "not found");
        assertNull(sync);
    }
    
    public void testGetSynchronizationDetailsNotFound() {
        List<ReportDetail> result = history.getSynchronizationDetails(1l, "not found", null, null, SynchronizationHistory.PAGE_SIZE);
        assertNotNull(result);
        assertEquals(0, result.size());
    }
    
    public void testGetSynchronizationDetails() throws Exception {
        long contentSetId = 1l;
        String syncKey = "testGetSynchronizationDetails";
        String syncId = SynchronizationEvent.getId(syncKey, contentSetId);
        ContentEvent event = new ContentEvent(contentSetId);
        event.setBytesManipulated(2l);
        event.setAction(ContentEventAction.ADD);
        event.setPath("test path");
        event.setResult(ContentEventResult.SUCCESS);
        
        ContentEvent eventTwo = new ContentEvent(contentSetId);
        eventTwo.setBytesManipulated(3l);
        eventTwo.setAction(ContentEventAction.DELETE);
        eventTwo.setPath("deleted path");
        eventTwo.setResult(ContentEventResult.SUCCESS);
        
        helper.addDocument(helper.getIndex(contentSetId, syncId), DocumentCreator.create(event));
        helper.addDocument(helper.getIndex(contentSetId, syncId), DocumentCreator.create(eventTwo));
        
        List<ReportDetail> data = history.getSynchronizationDetails(contentSetId, syncId, null, null, SynchronizationHistory.PAGE_SIZE);
        assertEquals(2, data.size());
        Iterator<ReportDetail> iterator = data.iterator();
        assertEquals(eventTwo, iterator.next());
        assertEquals(event, iterator.next());
        
        data = history.getSynchronizationDetails(contentSetId, syncId, ContentEventAction.ADD.value(), null, SynchronizationHistory.PAGE_SIZE);
        assertEquals(1, data.size());
        iterator = data.iterator();
        assertEquals(event, iterator.next());
        
        data = history.getSynchronizationDetails(contentSetId, syncId, null, "delet", SynchronizationHistory.PAGE_SIZE);
        assertEquals(1, data.size());
        iterator = data.iterator();
        assertEquals(eventTwo, iterator.next());
        
        data = history.getSynchronizationDetails(contentSetId, syncId, null, "foo", SynchronizationHistory.PAGE_SIZE);
        assertEquals(0, data.size());
    }
    
    public void testGetSynchronization() throws Exception {
        long contentSetId = 1l;
        String syncKey = "testGetSynchronization";
        SynchronizationEvent event = new SynchronizationEvent(syncKey, contentSetId);
        event.setProgress(createProgress());
        event.completed(2);
        
        helper.addDocument(helper.getIndex(contentSetId), DocumentCreator.create(event));
        
        Synchronization sync = history.getSynchronization(contentSetId, event.getId());
        assertEquals(event, sync);
    }
    
    public void testGetAllSynchronizations() throws Exception {
        long contentSetId = 1l;
        String syncKey = "testGetAllSynchronizations";
        SynchronizationEvent event = new SynchronizationEvent(syncKey, contentSetId);
        event.setProgress(createProgress());
        event.completed(2);
        
        SynchronizationEvent emptyEvent = new SynchronizationEvent("emptySyncEvent", contentSetId);
        emptyEvent.completed(2);
        
        helper.addDocument(helper.getIndex(contentSetId), DocumentCreator.create(event));
        helper.addDocument(helper.getIndex(contentSetId), DocumentCreator.create(emptyEvent));
        
        List<Synchronization> data = history.getSynchronizations(contentSetId, null, null, false, SynchronizationHistory.PAGE_SIZE);
        assertEquals(1, data.size());
        assertEquals(event, data.iterator().next());
        
        // Get all records (including "empty" records)
        data = history.getSynchronizations(contentSetId, null, null, true, SynchronizationHistory.PAGE_SIZE);
        Iterator<Synchronization> iterator = data.iterator();
        assertEquals(2, data.size());
        assertEquals(emptyEvent, iterator.next());
        assertEquals(event, iterator.next());
    }
    
    public void testGetSynchronizationsByDate() throws Exception {
        long contentSetId = 1l;
        String syncKey = "testGetSynchronizationsByDate";
        SynchronizationEvent event = new SynchronizationEvent(syncKey, contentSetId);
        event.setTimestamp(date(2008, Calendar.MARCH, 3).getTime());
        event.setProgress(createProgress());
        event.completed(2);
        
        helper.addDocument(helper.getIndex(contentSetId), DocumentCreator.create(event));
        
        Date startTime = date(2008, Calendar.JANUARY, 1);
        Date endTime = date(2008, Calendar.MARCH, 2);
        
        List<Synchronization> data = history.getSynchronizations(contentSetId, startTime, endTime, true, SynchronizationHistory.PAGE_SIZE);
        assertEquals(0, data.size());
        
        endTime = date(2008, Calendar.MARCH, 4);
        data = history.getSynchronizations(contentSetId, startTime, endTime, true, SynchronizationHistory.PAGE_SIZE);
        assertEquals(1, data.size());
        assertEquals(event, data.iterator().next());
    }
    
    public void testGetDetailsByPath() throws Exception {
        long contentSetId = 1l;
        String syncKey = "testGetDetailsByPath";
        String syncId = SynchronizationEvent.getId(syncKey, contentSetId);
        ContentEvent event = new ContentEvent(contentSetId);
        event.setBytesManipulated(2l);
        event.setAction(ContentEventAction.ADD);
        event.setPath("239supplier/Copy (9) of 25M");
        event.setResult(ContentEventResult.SUCCESS);
        
        ContentEvent eventTwo = new ContentEvent(contentSetId);
        eventTwo.setBytesManipulated(3l);
        eventTwo.setAction(ContentEventAction.DELETE);
        eventTwo.setPath("239supplier/dirtwo");
        eventTwo.setResult(ContentEventResult.SUCCESS);
        
        helper.addDocument(helper.getIndex(contentSetId, syncId), DocumentCreator.create(event));
        helper.addDocument(helper.getIndex(contentSetId, syncId), DocumentCreator.create(eventTwo));
        
        List<ReportDetail> data = null;
        
        data = history.getSynchronizationDetails(contentSetId, syncId, null, "dirtwo", SynchronizationHistory.PAGE_SIZE);
        assertEquals(1, data.size());
        assertEquals(eventTwo, data.iterator().next());
        
        data = history.getSynchronizationDetails(contentSetId, syncId, null, "25M", SynchronizationHistory.PAGE_SIZE);
        assertEquals(1, data.size());
        assertEquals(event, data.iterator().next());
        
        data = history.getSynchronizationDetails(contentSetId, syncId, null, "Copy of", SynchronizationHistory.PAGE_SIZE);
        assertEquals(1, data.size());
        assertEquals(event, data.iterator().next());
        
        data = history.getSynchronizationDetails(contentSetId, syncId, null, "239supplier", SynchronizationHistory.PAGE_SIZE);
        assertEquals(2, data.size());
    }
    
    public void testGetDetailsByPathAndAction() throws Exception {
        long contentSetId = 1l;
        String syncKey = "testGetDetailsByPathAndAction";
        String syncId = SynchronizationEvent.getId(syncKey, contentSetId);
        ContentEvent event = new ContentEvent(contentSetId);
        event.setBytesManipulated(2l);
        event.setAction(ContentEventAction.ADD);
        event.setPath("239supplier/Copy (9) of 25M");
        event.setResult(ContentEventResult.SUCCESS);
        
        ContentEvent eventTwo = new ContentEvent(contentSetId);
        eventTwo.setBytesManipulated(3l);
        eventTwo.setAction(ContentEventAction.DELETE);
        eventTwo.setPath("239supplier/dirtwo");
        eventTwo.setResult(ContentEventResult.SUCCESS);
        
        helper.addDocument(helper.getIndex(contentSetId, syncId), DocumentCreator.create(event));
        helper.addDocument(helper.getIndex(contentSetId, syncId), DocumentCreator.create(eventTwo));
        
        List<ReportDetail> data = null;
        
        data = history.getSynchronizationDetails(contentSetId, syncId, ContentEventAction.ADD.toString(), "dirtwo", SynchronizationHistory.PAGE_SIZE);
        assertEquals(0, data.size());
        
        data = history.getSynchronizationDetails(contentSetId, syncId, ContentEventAction.ADD.toString(), "Copy", SynchronizationHistory.PAGE_SIZE);
        assertEquals(1, data.size());
        assertEquals(event, data.iterator().next());
        
        data = history.getSynchronizationDetails(contentSetId, syncId, ContentEventAction.DELETE.toString(), "dirtwo", SynchronizationHistory.PAGE_SIZE);
        assertEquals(1, data.size());
        assertEquals(eventTwo, data.iterator().next());
        
        data = history.getSynchronizationDetails(contentSetId, syncId, ContentEventAction.DELETE.toString(), "Copy", SynchronizationHistory.PAGE_SIZE);
        assertEquals(0, data.size());
    }
    
    private ConsumerProgress createProgress() {
        ConsumerProgress progress = new ConsumerProgress();
        progress.initialize(1, 2, 3, 4, 5, 6);
        progress.added(1, true);
        progress.updated(2, true);
        progress.removed(3, true);
        progress.added(4, false); //throw in a failure
        return progress;
    }
    
    private Date date(int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        
        return c.getTime();
    }
}
