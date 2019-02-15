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

import java.util.ArrayList;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import com.solers.delivery.event.ContentEvent;
import com.solers.delivery.event.SynchronizationEvent;

import junit.framework.TestCase;

/**
 * @author mchen
 *
 */
public class MapEventConsumersImplTestCase extends TestCase {
    private EventConsumers<Consumer<DeliveryEventConsumer>> consumers;
    
    @Override
    @Before
    public void setUp() {
        this.consumers = new MapEventConsumersImpl<DeliveryEventConsumer>();
    }
    
    @Test
    public void testSize() {
        assertEquals(0, this.consumers.size());
        this.consumers.addConsumer(new Consumer<DeliveryEventConsumer>(null, "foo"));
        assertEquals(1, this.consumers.size());
    }
    
    @Test
    public void testAddConsumer() {
        assertEquals(0, this.consumers.size());
        this.consumers.addConsumer(new Consumer<DeliveryEventConsumer>(null, "foo"));
        assertEquals(1, this.consumers.size());
        this.consumers.addConsumer(new Consumer<DeliveryEventConsumer>(null, "bar"));
        assertEquals(2, this.consumers.size());
        this.consumers.addConsumer(new Consumer<DeliveryEventConsumer>(null, "batz"));
        assertEquals(3, this.consumers.size());
        
        DeliveryEventConsumer consumer = new Foo();
        //stays 3 because it replaces the original one
        this.consumers.addConsumer(new Consumer<DeliveryEventConsumer>(consumer, "foo"));
        assertEquals(3, this.consumers.size());
        
        //test new add replaces existing entry
        Iterator<Consumer<DeliveryEventConsumer>> it = this.consumers.iterator();
        while(it.hasNext()) {
            Consumer<DeliveryEventConsumer> next = it.next();
            if (next.getFileName().equals("foo")) {
                assertTrue(consumer.equals(next.getConsumer()));
            }
        }
    }

    @Test
    public void testRemoveConsumer() {
        this.consumers.addConsumer(new Consumer<DeliveryEventConsumer>(null, "foo"));
        this.consumers.addConsumer(new Consumer<DeliveryEventConsumer>(null, "bar"));
        this.consumers.addConsumer(new Consumer<DeliveryEventConsumer>(null, "batz"));
        
        assertEquals(3, this.consumers.size());
        
        this.consumers.removeConsumer(new Consumer<DeliveryEventConsumer>(new Foo(), "bar"));
        assertEquals(2, this.consumers.size());
        
        Iterator<Consumer<DeliveryEventConsumer>> it = this.consumers.iterator();
        while(it.hasNext()) {
            if (it.next().getFileName().endsWith("bar")) {
                fail("\"bar\" should have been removed");
            }
        }
    }

    @Test
    public void testIterator() {
        ArrayList<Consumer<DeliveryEventConsumer>> array = new ArrayList<Consumer<DeliveryEventConsumer>>();
        array.add(new Consumer<DeliveryEventConsumer>(null, "foo"));
        array.add(new Consumer<DeliveryEventConsumer>(null, "bar"));
        array.add(new Consumer<DeliveryEventConsumer>(null, "batz"));
        
        this.consumers.addConsumer(array.get(0));
        this.consumers.addConsumer(array.get(1));
        this.consumers.addConsumer(array.get(2));

        int count = 0;
        
        Iterator<Consumer<DeliveryEventConsumer>> it = this.consumers.iterator();
        
        //test that iterator only contains items that were put into it
        while(it.hasNext()) {
            Consumer<DeliveryEventConsumer> next = it.next();
            assertTrue(array.remove(next));
            count++;
        }

        assertEquals(3, count);
        assertTrue(array.isEmpty());
    }
    
    private class Foo implements DeliveryEventConsumer {

        /* (non-Javadoc)
         * @see com.solers.delivery.scripting.event.DeliveryEventConsumer#completedConsumer(com.solers.delivery.event.SynchronizationEvent)
         */
        @Override
        public void completedConsumer(SynchronizationEvent event) {
            //do nothing
        }

        /* (non-Javadoc)
         * @see com.solers.delivery.scripting.event.DeliveryEventConsumer#completedSupplier(com.solers.delivery.event.SynchronizationEvent)
         */
        @Override
        public void completedSupplier(SynchronizationEvent event) {
            //do nothing
        }

        /* (non-Javadoc)
         * @see com.solers.delivery.scripting.event.DeliveryEventConsumer#receivedContent(com.solers.delivery.event.ContentEvent)
         */
        @Override
        public void receivedContent(ContentEvent event) {
            //do nothing
        }

        /* (non-Javadoc)
         * @see com.solers.delivery.scripting.event.DeliveryEventConsumer#startedConsumer(com.solers.delivery.event.SynchronizationEvent)
         */
        @Override
        public void startedConsumer(SynchronizationEvent event) {
            //do nothing
        }

        /* (non-Javadoc)
         * @see com.solers.delivery.scripting.event.DeliveryEventConsumer#startedSupplier(com.solers.delivery.event.SynchronizationEvent)
         */
        @Override
        public void startedSupplier(SynchronizationEvent event) {
            //do nothing
        }

        /* (non-Javadoc)
         * @see com.solers.delivery.scripting.event.DeliveryEventConsumer#suppliedContent(com.solers.delivery.event.ContentEvent)
         */
        @Override
        public void suppliedContent(ContentEvent event) {
            //do nothing
        }
        
    }
}
