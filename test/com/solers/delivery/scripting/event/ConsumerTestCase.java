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

import org.junit.Before;
import org.junit.Test;

import com.solers.delivery.event.ContentEvent;
import com.solers.delivery.event.SynchronizationEvent;

import junit.framework.TestCase;

/**
 * @author mchen
 *
 */
public class ConsumerTestCase extends TestCase {
    Consumer<DeliveryEventConsumer> consumer1;
    Consumer<DeliveryEventConsumer> consumer2;
    Consumer<DeliveryEventConsumer> consumer3;
    Consumer<DeliveryEventConsumer> consumer4;
    Consumer<DeliveryEventConsumer> consumer5;
    
    @Override
    @Before
    public void setUp() {
        this.consumer1 = new Consumer<DeliveryEventConsumer>(new Foo(), "foo");
        this.consumer2 = new Consumer<DeliveryEventConsumer>(new Bar(), "bar");
        this.consumer3 = new Consumer<DeliveryEventConsumer>(new Foo(), "foo");
        this.consumer4 = new Consumer<DeliveryEventConsumer>(new Foo(), "foo");
        this.consumer5 = new Consumer<DeliveryEventConsumer>(new Foo(), "bar");
    }
    
    @Test
    public void testEquals() {
        //test reflexive
        assertTrue(this.consumer1.equals(this.consumer1));
        
        //test symmetric
        assertTrue(this.consumer1.equals(this.consumer3));
        assertTrue(this.consumer3.equals(this.consumer1));
        
        //test transitive
        assertTrue(this.consumer1.equals(this.consumer3));
        assertTrue(this.consumer3.equals(this.consumer4));
        assertTrue(this.consumer1.equals(this.consumer4));
        
        //test null
        assertFalse(this.consumer1.equals(null));

        //test equals
        assertTrue(this.consumer2.equals(this.consumer5));
        
        //test not equals
        assertFalse(this.consumer1.equals(this.consumer2));
        assertFalse(this.consumer2.equals(this.consumer1));
    }
    
    @Test
    public void testHashCode() {
        assertTrue(this.consumer1.hashCode() == this.consumer3.hashCode());
        assertFalse(this.consumer1.hashCode() == this.consumer2.hashCode());
        assertTrue(this.consumer2.hashCode() == this.consumer5.hashCode());
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
    
    private class Bar implements DeliveryEventConsumer {

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
