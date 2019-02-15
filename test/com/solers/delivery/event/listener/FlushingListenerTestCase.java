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

import java.util.List;

import junit.framework.TestCase;

import com.solers.delivery.event.ContentEvent;
import com.solers.delivery.event.GBSUpdateCompleteEvent;
import com.solers.delivery.event.PendingGBSUpdateEvent;
import com.solers.delivery.event.SynchronizationEvent;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class FlushingListenerTestCase extends TestCase {
    
    public void testFlushTime() throws Exception {
        TestFlushingListener listener = new TestFlushingListener(100, 1);
        assertFalse(listener.flushed);
        Thread.sleep(1500);
        assertTrue(listener.flushed);
    }
    
    public void testFlushBatch() throws Exception {
        TestFlushingListener listener = new TestFlushingListener(3, 1000);
        
        listener.supplied(new ContentEvent(1l));
        assertFalse(listener.flushed);
        listener.supplied(new ContentEvent(1l));
        assertFalse(listener.flushed);
        listener.received(new ContentEvent(1l));
        assertTrue(listener.flushed);
    }
    
    static class TestFlushingListener extends FlushingListener {

        boolean flushed = false;
        
        public TestFlushingListener(int batchSize, int flushTime) {
            super(batchSize, flushTime);
        }
        
        @Override
        protected void flush(List<ContentEvent> events) {
            flushed = true;
        }

        @Override
        public void consumerSynchronizationCompleted(SynchronizationEvent event) {
            
        }

        @Override
        public void consumerSynchronizationStarted(SynchronizationEvent event) {
            
        }

        @Override
        public void gbsUpdateComplete(GBSUpdateCompleteEvent event) {
            
        }

        @Override
        public void received(PendingGBSUpdateEvent event) {
            
        }

        @Override
        public void supplierSynchronizationCompleted(SynchronizationEvent event) {
            
        }

        @Override
        public void supplierSynchronizationStarted(SynchronizationEvent event) {
            
        }
        
    }
    
}
