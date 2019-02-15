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
package com.solers.delivery.scripting.service;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;

import com.solers.delivery.event.ContentEvent;
import com.solers.delivery.event.SynchronizationEvent;
import com.solers.delivery.scripting.event.EventQueueImpl;
import com.solers.delivery.scripting.event.Consumer;
import com.solers.delivery.scripting.event.DeliveryEventConsumer;
import com.solers.delivery.scripting.event.EventConsumers;
import com.solers.delivery.scripting.event.EventQueue;
import com.solers.delivery.scripting.event.MapEventConsumersImpl;
import com.solers.delivery.security.SecurityProviderUtil;

public class EventServiceImplTestCase extends TestCase {
    private EventServiceImpl service;
    private EventQueue queue;
    private EventConsumers<Consumer<DeliveryEventConsumer>> consumers;
    
    {
        SecurityProviderUtil.init();
        SecurityProviderUtil.getProvider();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    @Before
    public void setUp() {
        this.service = new EventServiceImpl();
        this.consumers = new MapEventConsumersImpl<DeliveryEventConsumer>();
        this.queue = new EventQueueImpl(new LinkedBlockingQueue(5000), this.consumers);
        this.service.setEventQueue(this.queue);
    }
    
    @Override
    @After
    public void tearDown() {
        try {
            FileUtils.forceDeleteOnExit(new File("scripting-test"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testReady() {
        //false when there's no consumers
        assertFalse(this.service.ready());
        this.consumers.addConsumer(new Consumer<DeliveryEventConsumer>(this.getConsumer(), "foo"));
        //true when there's at least one consumer
        assertTrue(this.service.ready());
    }
    
    private DeliveryEventConsumer getConsumer(){
        return new DeliveryEventConsumer(){

            @Override
            public void receivedContent(ContentEvent event) {
                //do nothing
            }

            @Override
            public void startedConsumer(SynchronizationEvent event) {
                //do nothing
            }

            @Override
            public void startedSupplier(SynchronizationEvent event) {
                //do nothing
            }

            @Override
            public void completedConsumer(SynchronizationEvent event) {
                //do nothing
            }

            @Override
            public void completedSupplier(SynchronizationEvent event) {
                //do nothing
            }

            @Override
            public void suppliedContent(ContentEvent event) {
                //do nothing
            }};
        }
}
