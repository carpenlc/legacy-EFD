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
package com.solers.delivery.event;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.solers.delivery.event.listener.EventListener;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class EventManagerTestCase extends TestCase {
    
    public void testEventPropagation() {
        TestListener first = new TestListener();
        TestListener second = new TestListener();
        List<EventListener> listeners = new ArrayList<EventListener>();
        listeners.add(first);
        listeners.add(second);
        
        SynchronizationEvent consumerCompleted = new SynchronizationEvent("", -1);
        SynchronizationEvent consumerStarted = new SynchronizationEvent("", -1);
        SynchronizationEvent supplierCompleted = new SynchronizationEvent("", -1);
        SynchronizationEvent supplierStarted = new SynchronizationEvent("", -1);
        GBSUpdateCompleteEvent gbsCompleted = new GBSUpdateCompleteEvent(-1, "");
        ContentEvent received = new ContentEvent(-1);
        ContentEvent supplied = new ContentEvent(-1);
        PendingGBSUpdateEvent gbsUpdate = new PendingGBSUpdateEvent(-1, "", null, null, -1, null, null);
        
        EventManager manager = new EventManager(listeners);
        
        manager.consumerSynchronizationStarted(consumerStarted);
        manager.consumerSynchronizationCompleted(consumerCompleted);
        manager.supplierSynchronizationStarted(supplierStarted);
        manager.supplierSynchronizationCompleted(supplierCompleted);
        manager.gbsUpdateComplete(gbsCompleted);
        manager.received(received);
        manager.supplied(supplied);
        manager.received(gbsUpdate);
        
        assertSame(consumerStarted, first.consumerStarted);
        assertSame(consumerStarted, second.consumerStarted);
        
        assertSame(consumerCompleted, first.consumerCompleted);
        assertSame(consumerCompleted, second.consumerCompleted);
        
        assertSame(supplierStarted, first.supplierStarted);
        assertSame(supplierStarted, second.supplierStarted);
        
        assertSame(supplierCompleted, first.supplierCompleted);
        assertSame(supplierCompleted, second.supplierCompleted);
        
        assertSame(gbsCompleted, first.gbsCompleted);
        assertSame(gbsCompleted, second.gbsCompleted);
        
        assertSame(received, first.received);
        assertSame(received, second.received);
        
        assertSame(supplied, first.supplied);
        assertSame(supplied, second.supplied);
        
        assertSame(gbsUpdate, first.gbsUpdate);
        assertSame(gbsUpdate, second.gbsUpdate);
        
    }
    
    class TestListener implements EventListener {

        SynchronizationEvent consumerCompleted;
        SynchronizationEvent consumerStarted;
        SynchronizationEvent supplierCompleted;
        SynchronizationEvent supplierStarted;
        GBSUpdateCompleteEvent gbsCompleted;
        ContentEvent received;
        ContentEvent supplied;
        PendingGBSUpdateEvent gbsUpdate;
        
        @Override
        public void consumerSynchronizationCompleted(SynchronizationEvent event) {
            consumerCompleted = event;
        }

        @Override
        public void consumerSynchronizationStarted(SynchronizationEvent event) {
            consumerStarted = event;
        }

        @Override
        public void gbsUpdateComplete(GBSUpdateCompleteEvent event) {
            gbsCompleted = event;
        }

        @Override
        public void received(ContentEvent event) {
            received = event;
        }

        @Override
        public void received(PendingGBSUpdateEvent event) {
            gbsUpdate = event;
        }

        @Override
        public void supplied(ContentEvent event) {
            supplied = event;
        }

        @Override
        public void supplierSynchronizationCompleted(SynchronizationEvent event) {
            supplierCompleted = event;
        }

        @Override
        public void supplierSynchronizationStarted(SynchronizationEvent event) {
            supplierStarted = event;
        }
        
    }
}
