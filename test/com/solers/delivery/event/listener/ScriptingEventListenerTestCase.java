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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;

import java.util.Properties;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import com.solers.delivery.event.ContentEvent;
import com.solers.delivery.event.DeliveryEvent;
import com.solers.delivery.event.GBSUpdateCompleteEvent;
import com.solers.delivery.event.PendingGBSUpdateEvent;
import com.solers.delivery.event.SynchronizationEvent;
import com.solers.delivery.event.ContentEvent.ContentEventAction;
import com.solers.delivery.event.ContentEvent.ContentEventResult;
import com.solers.delivery.scripting.service.EventService;
import com.solers.delivery.security.SecurityProviderUtil;

public class ScriptingEventListenerTestCase extends TestCase {
    Properties props;
    ScriptingEventListener listener;
    EventService mockService;
    SynchronizationEvent synchronizationEvent;
    GBSUpdateCompleteEvent completeEvent;
    ContentEvent contentEvent;
    PendingGBSUpdateEvent pendingEvent;
    
    {
        SecurityProviderUtil.init();
        SecurityProviderUtil.getProvider();
    }
    
    @Override
    @Before
    public void setUp() {
        props = new Properties();
        props.setProperty("listenToConsumerSynchronizationEvents", "true");
        props.setProperty("listenToSupplierSynchronizationEvents", "true");
        props.setProperty("listenToReceivedContentEvents", "true");
        props.setProperty("listenToSuppliedContentEvents", "true");

        mockService = createMock(EventService.class);
        
        synchronizationEvent = new SynchronizationEvent(-1L);
        completeEvent = new GBSUpdateCompleteEvent(-1L, "foo");
        contentEvent = new ContentEvent(-1L);
        pendingEvent = new PendingGBSUpdateEvent(0, "foo", "bar", "baz", 0, ContentEventAction.ADD, ContentEventResult.DELETE_FAILED);
    }
    
    @Test
    public void testListenToAllEvents() {
        listener = new ScriptingEventListener(props);
        listener.setEventService(mockService);

        expect(mockService.ready()).andReturn(true);
        mockService.startedConsumer(isA(DeliveryEvent.class));
        replay(mockService);
        listener.consumerSynchronizationStarted(synchronizationEvent);
        verify(mockService);

        reset(mockService);
        expect(mockService.ready()).andReturn(true);
        mockService.completedConsumer(isA(DeliveryEvent.class));
        replay(mockService);
        listener.consumerSynchronizationCompleted(synchronizationEvent);
        verify(mockService);

        reset(mockService);
        expect(mockService.ready()).andReturn(true);
        mockService.receivedContent(isA(DeliveryEvent.class));
        replay(mockService);
        listener.received(contentEvent);
        verify(mockService);

        reset(mockService);
        replay(mockService);
        //this is ignored
        listener.received(pendingEvent);
        verify(mockService);

        reset(mockService);
        expect(mockService.ready()).andReturn(true);
        mockService.suppliedContent(isA(DeliveryEvent.class));
        replay(mockService);
        listener.supplied(contentEvent);
        verify(mockService);

        reset(mockService);
        expect(mockService.ready()).andReturn(true);
        mockService.startedSupplier(isA(DeliveryEvent.class));
        replay(mockService);
        listener.supplierSynchronizationStarted(synchronizationEvent);
        verify(mockService);

        reset(mockService);
        expect(mockService.ready()).andReturn(true);
        mockService.completedSupplier(isA(DeliveryEvent.class));
        replay(mockService);
        listener.supplierSynchronizationCompleted(synchronizationEvent);
        verify(mockService);

        reset(mockService);
        replay(mockService);
        //this is ignored
        listener.gbsUpdateComplete(completeEvent);
        verify(mockService);

        reset(mockService);
        expect(mockService.ready()).andReturn(true);
        mockService.receivedContent(isA(DeliveryEvent.class));
        replay(mockService);
        listener.received(contentEvent);
        verify(mockService);
    }

    @Test
    public void testDoesNothingWhenServerIsNotReady() {
        listener = new ScriptingEventListener(props);
        listener.setEventService(mockService);

        expect(mockService.ready()).andReturn(false);
        expectLastCall().times(6);
        replay(mockService);
        
        listener.consumerSynchronizationStarted(synchronizationEvent);
        listener.consumerSynchronizationCompleted(synchronizationEvent);

        listener.received(contentEvent);
        
        listener.received(pendingEvent);

        listener.supplied(contentEvent);
        listener.supplierSynchronizationStarted(synchronizationEvent);
        listener.supplierSynchronizationCompleted(synchronizationEvent);

        listener.gbsUpdateComplete(completeEvent);
        listener.received(pendingEvent);

        verify(mockService);
    }
    
    @Test
    public void testSelectiveListening() {
        String[] flags = new String[] {
            "listenToConsumerSynchronizationEvents",
            "listenToSupplierSynchronizationEvents",
            "listenToReceivedContentEvents",
            "listenToSuppliedContentEvents"
            };
        
        for (String flag : flags) {
            props.setProperty(flag, "false");
            listener = new ScriptingEventListener(props);
            listener.setEventService(mockService);

            if (flag.equals(flags[0])) {
                expect(mockService.ready()).andReturn(true);
                mockService.receivedContent(isA(DeliveryEvent.class));
                expect(mockService.ready()).andReturn(true);
                mockService.suppliedContent(isA(DeliveryEvent.class));
                expect(mockService.ready()).andReturn(true);
                mockService.startedSupplier(isA(DeliveryEvent.class));
                expect(mockService.ready()).andReturn(true);
                mockService.completedSupplier(isA(DeliveryEvent.class));
            } else if (flag.equals(flags[1])){
                expect(mockService.ready()).andReturn(true);
                mockService.receivedContent(isA(DeliveryEvent.class));
                expect(mockService.ready()).andReturn(true);
                mockService.suppliedContent(isA(DeliveryEvent.class));
                expect(mockService.ready()).andReturn(true);
                mockService.startedConsumer(isA(DeliveryEvent.class));
                expect(mockService.ready()).andReturn(true);
                mockService.completedConsumer(isA(DeliveryEvent.class));
            } else if (flag.equals(flags[2])){
                expect(mockService.ready()).andReturn(true);
                mockService.suppliedContent(isA(DeliveryEvent.class));
                expect(mockService.ready()).andReturn(true);
                mockService.startedConsumer(isA(DeliveryEvent.class));
                expect(mockService.ready()).andReturn(true);
                mockService.completedConsumer(isA(DeliveryEvent.class));
                expect(mockService.ready()).andReturn(true);
                mockService.startedSupplier(isA(DeliveryEvent.class));
                expect(mockService.ready()).andReturn(true);
                mockService.completedSupplier(isA(DeliveryEvent.class));
            } else {
                expect(mockService.ready()).andReturn(true);
                mockService.receivedContent(isA(DeliveryEvent.class));
                expect(mockService.ready()).andReturn(true);
                mockService.startedConsumer(isA(DeliveryEvent.class));
                expect(mockService.ready()).andReturn(true);
                mockService.completedConsumer(isA(DeliveryEvent.class));
                expect(mockService.ready()).andReturn(true);
                mockService.startedSupplier(isA(DeliveryEvent.class));
                expect(mockService.ready()).andReturn(true);
                mockService.completedSupplier(isA(DeliveryEvent.class));
            }

            replay(mockService);
            
            listener.consumerSynchronizationStarted(synchronizationEvent);
            System.out.println("start consumer");
            listener.consumerSynchronizationCompleted(synchronizationEvent);
            System.out.println("stop consumer");
            listener.received(contentEvent);
            System.out.println("received");
            listener.supplied(contentEvent);
            System.out.println("supplied");
            listener.supplierSynchronizationStarted(synchronizationEvent);
            System.out.println("start supplier");
            listener.supplierSynchronizationCompleted(synchronizationEvent);
            System.out.println("stop supplier");
            listener.received(pendingEvent);
            listener.gbsUpdateComplete(completeEvent);
        
            verify(mockService);

            reset(mockService);
            props.setProperty(flag, "true");
        }
    }
}
