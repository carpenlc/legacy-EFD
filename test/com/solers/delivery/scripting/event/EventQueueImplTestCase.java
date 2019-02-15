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
/**
 * 
 */
package com.solers.delivery.scripting.event;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.solers.delivery.Waiter;
import com.solers.delivery.event.ContentEvent;
import com.solers.delivery.event.SynchronizationEvent;

/**
 * @author mchen
 *
 */
public class EventQueueImplTestCase extends TestCase {
    private EventConsumers<Consumer<DeliveryEventConsumer>> consumers;
    private EventQueue queue;
    @SuppressWarnings("unchecked")
    private Map consumersMap;
    private LinkedBlockingQueue<QueuedEvent> innerQueue;
    
    private int completedConsumer;
    private int completedSupplier;
    private int receivedContent;
    private int startedConsumer;
    private int startedSupplier;
    private int suppliedContent;
    
    //custom consumer that increment counters based on events
    private class Foo implements DeliveryEventConsumer {

        /* (non-Javadoc)
         * @see com.solers.delivery.scripting.event.DeliveryEventConsumer#completedConsumer(com.solers.delivery.event.SynchronizationEvent)
         */
        @Override
        public void completedConsumer(SynchronizationEvent event) {
            completedConsumer++;
        }

        /* (non-Javadoc)
         * @see com.solers.delivery.scripting.event.DeliveryEventConsumer#completedSupplier(com.solers.delivery.event.SynchronizationEvent)
         */
        @Override
        public void completedSupplier(SynchronizationEvent event) {
            completedSupplier++;
        }

        /* (non-Javadoc)
         * @see com.solers.delivery.scripting.event.DeliveryEventConsumer#receivedContent(com.solers.delivery.event.ContentEvent)
         */
        @Override
        public void receivedContent(ContentEvent event) {
            receivedContent++;
        }

        /* (non-Javadoc)
         * @see com.solers.delivery.scripting.event.DeliveryEventConsumer#startedConsumer(com.solers.delivery.event.SynchronizationEvent)
         */
        @Override
        public void startedConsumer(SynchronizationEvent event) {
            startedConsumer++;
        }

        /* (non-Javadoc)
         * @see com.solers.delivery.scripting.event.DeliveryEventConsumer#startedSupplier(com.solers.delivery.event.SynchronizationEvent)
         */
        @Override
        public void startedSupplier(SynchronizationEvent event) {
            startedSupplier++;
        }

        /* (non-Javadoc)
         * @see com.solers.delivery.scripting.event.DeliveryEventConsumer#suppliedContent(com.solers.delivery.event.ContentEvent)
         */
        @Override
        public void suppliedContent(ContentEvent event) {
            suppliedContent++;
        }
        
    }
    
    @SuppressWarnings("unchecked")
    @Override
    @Before
    public void setUp() {
        this.completedConsumer = 0;
        this.completedSupplier = 0;
        this.receivedContent = 0;
        this.startedConsumer = 0;
        this.startedSupplier = 0;
        this.suppliedContent = 0;
        
        this.consumers = new MapEventConsumersImpl<DeliveryEventConsumer>();
        this.innerQueue = new LinkedBlockingQueue(5000);
        this.queue = new EventQueueImpl(this.innerQueue, this.consumers);
        
        try {
           Field queueField = EventQueueImpl.class.getDeclaredField("queue");
           queueField.setAccessible(true);
           Field mapField = MapEventConsumersImpl.class.getDeclaredField("consumers");
           mapField.setAccessible(true);
           this.consumersMap = (Map) mapField.get(this.consumers);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

    }

    @Override
    @After
    public void tearDown() {
        this.consumersMap.clear();
        try {
            FileUtils.forceDeleteOnExit(new File("scripting-test"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testHasConsumers() {
        assertFalse(this.queue.hasConsumers());
        this.consumers.addConsumer(new Consumer<DeliveryEventConsumer>(null, "foo"));
        assertTrue(this.queue.hasConsumers());
        this.consumers.removeConsumer(new Consumer<DeliveryEventConsumer>(null, "foo"));
        assertFalse(this.queue.hasConsumers());
    }
    
    @Test
    public void testReceivedEvent() throws Exception {
        assertEquals(0, this.innerQueue.size());
        this.queue.receivedEvent(new ContentEvent(-1L), EventType.RECEIVED_CONTENT);

        new Waiter(600) {
            public boolean condition() {
                return innerQueue.size() == 1;
            }
        }.doWait();
        
        assertEquals(1, this.innerQueue.size());
    }

    @Test
    public void testWaitForEvent() throws Exception {
        //add custom consumer that simply increment counters
        this.consumers.addConsumer(new Consumer<DeliveryEventConsumer>(new Foo(), "foo"));
        
        this.queue.receivedEvent(new ContentEvent(-1L), EventType.RECEIVED_CONTENT);
        this.queue.receivedEvent(new ContentEvent(-1L), EventType.SUPPLIED_CONTENT);
        this.queue.receivedEvent(new SynchronizationEvent("foo", -1L), EventType.STARTED_CONSUMER);
        this.queue.receivedEvent(new SynchronizationEvent("foo", -1L), EventType.COMPLETED_CONSUMER);
        this.queue.receivedEvent(new SynchronizationEvent("foo", -1L), EventType.STARTED_SUPPLIER);
        this.queue.receivedEvent(new SynchronizationEvent("foo", -1L), EventType.COMPLETED_SUPPLIER);

        //needs to wait because events are being added thru threads
        new Waiter(600) {
            public boolean condition() {
                return innerQueue.size() == 6;
            }
        }.doWait();
        assertEquals(6, this.innerQueue.size());

        this.assertCounters(0);
        //does nothing since wait time is 0
        this.queue.waitForEvents(0);
        
        assertEquals(6, this.innerQueue.size());

        //use 500 milliseconds just to be sure there is enough time to process all events
        this.queue.waitForEvents(500);
        assertEquals(0, this.innerQueue.size());
        this.assertCounters(1);
        
        //timeout at 5s
        this.queue.waitForEvents(5000);
        
        //make sure counters did not change
        assertCounters(1);

        //test invalid event type
        //classcastexception is caught and logged, only verify counts are still the same
        this.queue.receivedEvent(new ContentEvent(-1L), EventType.STARTED_CONSUMER);
        this.queue.waitForEvents(100);
        assertCounters(1);
    }
    
    private void assertCounters(int count){
        assertEquals(count, this.completedConsumer);
        assertEquals(count, this.startedConsumer);
        assertEquals(count, this.completedSupplier);
        assertEquals(count, this.startedSupplier);
        assertEquals(count, this.receivedContent);
        assertEquals(count, this.suppliedContent);
    }
}
