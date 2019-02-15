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
package com.solers.delivery.scripting;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.solers.delivery.event.ContentEvent;
import com.solers.delivery.event.SynchronizationEvent;
import com.solers.delivery.event.listener.ScriptingEventListener;
import com.solers.delivery.scripting.event.EventQueueImpl;
import com.solers.delivery.scripting.event.Consumer;
import com.solers.delivery.scripting.event.DeliveryEventConsumer;
import com.solers.delivery.scripting.event.MapEventConsumersImpl;

/**
 * @author mchen
 *
 */
public class RmiTestCase extends TestCase {
    private ScriptingEventListener listener;
    private ApplicationContext clientContext;
    private ApplicationContext serverContext;
    private EventQueueImpl queue;
    private int completedConsumerCount;
    private int completedSupplierCount;
    private int receivedContentCount;
    private int startedConsumerCount;
    private int startedSupplierCount;
    private int suppliedContentCount;
    
    @SuppressWarnings("unchecked")
    @Override
    @Before
    public void setUp() throws Exception {
        completedConsumerCount = 0;
        completedSupplierCount = 0;
        receivedContentCount = 0;
        startedConsumerCount = 0;
        startedSupplierCount = 0;
        suppliedContentCount = 0;
    
        System.setProperty("password.provider.bean", "passwordProviderTest");
        this.clientContext = new ClassPathXmlApplicationContext(new String[] {"spring/config.xml", "rmi-test.xml", "spring/scripting-client.xml"});
        this.listener = (ScriptingEventListener)this.clientContext.getBean("scriptingEventListener");
        this.serverContext = new ClassPathXmlApplicationContext(new String[] {"spring/config.xml", "spring/scripting-server.xml"});
        ((MapEventConsumersImpl<DeliveryEventConsumer>)this.serverContext.getBean("eventConsumers")).addConsumer(new Consumer<DeliveryEventConsumer>(new TestConsumer(), "foo"));
        this.queue = (EventQueueImpl)this.serverContext.getBean("eventQueue");
    }
        
    @Override
    @After
    public void tearDown() throws Exception {
        ((ConfigurableApplicationContext)this.serverContext).close();
        ((ConfigurableApplicationContext)this.clientContext).close();
    }
    
    @Test
    public void testServiceCalls() {
        try {
            this.assertCounts(0);
            this.sendEvents();
            this.assertCounts(1);
            this.sendEvents();
            this.assertCounts(2);
        } catch(Exception ex) {
            //e.g. server is not started due to errors in code/config
            fail(ex.getMessage());
        }
    }
    
    private void sendEvents(){
        this.listener.consumerSynchronizationCompleted(new SynchronizationEvent("foo", -1L));
        this.listener.supplierSynchronizationCompleted(new SynchronizationEvent("foo", -1L));
        this.listener.received(new ContentEvent(-1L));
        this.listener.consumerSynchronizationStarted(new SynchronizationEvent("foo", -1L));
        this.listener.supplierSynchronizationStarted(new SynchronizationEvent("foo", -1L));
        this.listener.supplied(new ContentEvent(-1L));
        this.queue.waitForEvents(5000);
    }
    
    private void assertCounts(int count) {
        assertEquals(count, completedConsumerCount);
        assertEquals(count, completedSupplierCount);
        assertEquals(count, receivedContentCount);
        assertEquals(count, startedConsumerCount);
        assertEquals(count, startedSupplierCount);
        assertEquals(count, suppliedContentCount);
    }
    
    private class TestConsumer implements DeliveryEventConsumer {
        public TestConsumer() {
            super();
        }
        
        /* (non-Javadoc)
         * @see com.solers.delivery.scripting.event.DeliveryEventConsumer#completedConsumer(com.solers.delivery.event.SynchronizationEvent)
         */
        @Override
        public void completedConsumer(SynchronizationEvent event) {
            completedConsumerCount++;
        }

        /* (non-Javadoc)
         * @see com.solers.delivery.scripting.event.DeliveryEventConsumer#completedSupplier(com.solers.delivery.event.SynchronizationEvent)
         */
        @Override
        public void completedSupplier(SynchronizationEvent event) {
            completedSupplierCount++;
        }

        /* (non-Javadoc)
         * @see com.solers.delivery.scripting.event.DeliveryEventConsumer#receivedContent(com.solers.delivery.event.ContentEvent)
         */
        @Override
        public void receivedContent(ContentEvent event) {
            receivedContentCount++;
        }

        /* (non-Javadoc)
         * @see com.solers.delivery.scripting.event.DeliveryEventConsumer#startedConsumer(com.solers.delivery.event.SynchronizationEvent)
         */
        @Override
        public void startedConsumer(SynchronizationEvent event) {
            startedConsumerCount++;
        }

        /* (non-Javadoc)
         * @see com.solers.delivery.scripting.event.DeliveryEventConsumer#startedSupplier(com.solers.delivery.event.SynchronizationEvent)
         */
        @Override
        public void startedSupplier(SynchronizationEvent event) {
            startedSupplierCount++;
        }

        /* (non-Javadoc)
         * @see com.solers.delivery.scripting.event.DeliveryEventConsumer#suppliedContent(com.solers.delivery.event.ContentEvent)
         */
        @Override
        public void suppliedContent(ContentEvent event) {
            suppliedContentCount++;
        }
        
    }
}
