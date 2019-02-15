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
package com.solers.delivery.scripting.event;

import java.util.concurrent.LinkedBlockingQueue;

import org.junit.Assert;
import org.junit.Test;

import com.solers.delivery.event.SynchronizationEvent;

public class QueuedEventTestCase {

    @Test
    public void testEquals() {
        QueuedEvent event1 = new QueuedEvent(EventType.COMPLETED_CONSUMER, new SynchronizationEvent(-1L));
        QueuedEvent event2 = new QueuedEvent(EventType.COMPLETED_CONSUMER, new SynchronizationEvent(-1L));

        Assert.assertFalse("events are not the same since the ids are different", event1.equals(event2));
        Assert.assertFalse("events are not the same since the ids are different", event2.equals(event1));
        Assert.assertTrue("event should equal to itself", event1.equals(event1));
        Assert.assertTrue("event should equal to itself", event2.equals(event2));
        Assert.assertFalse("event should not equal to null", event1.equals(null));
    }
    
    @Test
    public void testHashCode() {
        QueuedEvent event1 = new QueuedEvent(EventType.COMPLETED_CONSUMER, new SynchronizationEvent(-1L));
        QueuedEvent event2 = new QueuedEvent(EventType.COMPLETED_CONSUMER, new SynchronizationEvent(-1L));
        QueuedEvent event3 = new QueuedEvent(EventType.COMPLETED_SUPPLIER, new SynchronizationEvent(-2L));
        
        Assert.assertFalse("significant fields are the same but hashcodes should not be the same", event1.hashCode() == event2.hashCode());
        Assert.assertFalse("hashcode should not be the same", event1.hashCode() == event2.hashCode());
        Assert.assertFalse("hashcode should not be the same", event2.hashCode() == event3.hashCode());
    }
    
    @Test
    public void testAddingToQueue() throws InterruptedException {
        LinkedBlockingQueue<QueuedEvent> q = new LinkedBlockingQueue<QueuedEvent>(2);
        QueuedEvent event = new QueuedEvent(EventType.STARTED_CONSUMER, new SynchronizationEvent(123L));
        System.out.println(event);
        q.put(event);
        Assert.assertTrue(q.contains(event));
    }
}
