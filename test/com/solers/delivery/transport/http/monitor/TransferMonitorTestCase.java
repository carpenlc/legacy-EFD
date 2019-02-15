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
package com.solers.delivery.transport.http.monitor;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.solers.delivery.content.status.CurrentSynchronization;
import com.solers.delivery.content.status.SupplierProgress;
import com.solers.delivery.content.status.SynchronizationResult;
import com.solers.delivery.content.supplier.MockSupplierContentSetManager;
import com.solers.delivery.domain.ContentSet;
import com.solers.delivery.event.SynchronizationEvent;
import com.solers.delivery.transport.http.DefaultTransferStatus;
import com.solers.delivery.transport.http.TransferStatus;
import com.solers.delivery.transport.http.TransferType;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class TransferMonitorTestCase {
    
    private TransferMonitor monitor;
    private MockSupplierContentSetManager manager;
    
    @Before
    public void setUp() {
        manager = new MockSupplierContentSetManager();
        monitor = new TransferMonitor();
        monitor.setSupplierManager(manager);
        
        ContentSet c = new ContentSet();
        c.setId(1l);
        c.setName("foo");
        manager.registerContentSet(c);
    }
    
    @Test
    public void testCompleteNonExistentSupplier() {
        monitor.completeSupplier(new SynchronizationEvent("1", 1));
    }
    
    @Test
    public void testCompleteSupplierWithModifications() {
        SynchronizationEvent e = new SynchronizationEvent("xxx", 1);
        monitor.startSupplier(e, new SupplierProgress(1, 1));
        
        SupplierProgress p = monitor.getSupplierProgress(e.getId());
        p.sentBytes(1);
        p.sentItem();
        
        e.setResult(SynchronizationResult.COMPLETE_MODIFIED);
        monitor.completeSupplier(e);
        
        List<CurrentSynchronization> data = monitor.getCurrentSynchronizations();
        
        Assert.assertEquals(1, data.size());
        
        CurrentSynchronization sync = data.get(0);
        
        Assert.assertFalse(sync.isActive());
        Assert.assertTrue(sync.isSupplier());
        Assert.assertEquals(SynchronizationResult.COMPLETE_MODIFIED, sync.getResult());
    }
    
    @Test
    public void testCompleteSupplierWithNoModifications() {
        SynchronizationEvent e = new SynchronizationEvent("xxx", 1);
        monitor.startSupplier(e, new SupplierProgress(0, 0));
        
        e.setResult(SynchronizationResult.COMPLETE_NOT_MODIFIED);
        monitor.completeSupplier(e);
        
        List<CurrentSynchronization> data = monitor.getCurrentSynchronizations();
        
        Assert.assertEquals(1, data.size());
        
        CurrentSynchronization sync = data.get(0);
        
        Assert.assertFalse(sync.isActive());
        Assert.assertTrue(sync.isSupplier());
        Assert.assertEquals(SynchronizationResult.COMPLETE_NOT_MODIFIED, sync.getResult());
    }
    
    @Test
    public void testCompleteSupplierFailed() {
        SynchronizationEvent e = new SynchronizationEvent("xxx", 1);
        monitor.startSupplier(e, new SupplierProgress(1, 1));
        
        e.setResult(SynchronizationResult.FAILED);
        monitor.completeSupplier(e);
        
        List<CurrentSynchronization> data = monitor.getCurrentSynchronizations();
        
        Assert.assertEquals(1, data.size());
        
        CurrentSynchronization sync = data.get(0);
        
        Assert.assertFalse(sync.isActive());
        Assert.assertTrue(sync.isSupplier());
        Assert.assertEquals(SynchronizationResult.FAILED, sync.getResult());
    }
    
    @Test
    public void testConcurrentGetCurrentConsumers() throws Exception {
        final SynchronizationEvent event = new SynchronizationEvent("xxx", 1l);
        monitor.startSupplier(event, new SupplierProgress(1, 1));
        
        int threads = 6;
        final int runs = 1000;
        final ExecutorService service = Executors.newFixedThreadPool(threads);
        final CyclicBarrier b = new CyclicBarrier(threads);
        final AtomicBoolean failed = new AtomicBoolean(false);
        final StringBuffer message = new StringBuffer();
        
        Callable<Void> reader = new Callable<Void>() {
            public Void call() throws Exception {
                b.await();
                
                try {
                    for (int i=0; i < runs; i++) {
                        for (CurrentSynchronization sync : monitor.getCurrentConsumers(1l)) {
                            Iterator<TransferStatus> j = sync.getCurrentTransfers().iterator();
                            while (j.hasNext()) {
                                j.next();
                            }
                        }
                    }
                } catch (RuntimeException ex) {
                    message.append(ex.getClass().toString());
                    failed.compareAndSet(false, true);
                }
                
                return null;
            }
        };
        
        Callable<Void> writer = new Callable<Void>() {
            public Void call() throws Exception {
                b.await();
                
                try {
                    for (int i=0; i < runs; i++) {
                        monitor.startTransfer(event.getId(), new DefaultTransferStatus("aaa", 1L, TransferType.ADD));
                    }
                } catch (RuntimeException ex) {
                    message.append(ex.getClass().toString());
                    failed.compareAndSet(false, true);
                }
                
                return null;
            }
        };
        
        for (int i=0; i< (threads / 2); i++) {
            service.submit(reader);
            service.submit(writer);
        }
        
        service.shutdown();
        boolean completed = service.awaitTermination(4, TimeUnit.MINUTES);
        
        if (!completed) {
            Assert.fail("Timed out waiting for executor");
        }
        
        if (failed.get()) {
            Assert.fail(message.toString());
        }
    }
    
    @Test
    public void testTransferClearedAfterStopSupplier() {
        SynchronizationEvent event = new SynchronizationEvent("xxx", 1l);
        monitor.startSupplier(event, new SupplierProgress(1, 1));
        Assert.assertNotNull(monitor.getCurrentConsumers(1L).get(0).getCurrentTransfers());
        monitor.completeSupplier(event);
        Assert.assertNull(monitor.getCurrentConsumers(1L).get(0).getCurrentTransfers());
    }
    
    @Test
    public void testConcurrentStartSupplier() throws Exception {
        int threads = 6;
        final int runs = 1000;
        final ExecutorService service = Executors.newFixedThreadPool(threads);
        final CyclicBarrier b = new CyclicBarrier(threads);
        final AtomicBoolean failed = new AtomicBoolean(false);
        final StringBuffer message = new StringBuffer();
        final AtomicLong counter = new AtomicLong(0);
        
        Callable<Void> reader = new Callable<Void>() {
            public Void call() throws Exception {
                b.await();
                
                try {
                    for (int i=0; i < runs; i++) {
                        SynchronizationEvent event = new SynchronizationEvent("xxx-"+counter.incrementAndGet(), 1l);
                        monitor.startSupplier(event, new SupplierProgress(1, 1));
                    }
                } catch (RuntimeException ex) {
                    message.append(ex.getClass().toString());
                    failed.compareAndSet(false, true);
                }
                
                return null;
            }
        };
        
        for (int i=0; i< threads; i++) {
            service.submit(reader);
        }
        
        service.shutdown();
        boolean completed = service.awaitTermination(5, TimeUnit.SECONDS);
        
        if (!completed) {
            Assert.fail("Timed out waiting for executor");
        }
        
        if (failed.get()) {
            Assert.fail(message.toString());
        }
    }
    
    @Test
    public void testTransferForNonExistentSupplier() {
        monitor.startTransfer("xxxxx", new DefaultTransferStatus("", 1, TransferType.ADD));
        monitor.completeTransfer("yyyy", new DefaultTransferStatus("", 1, TransferType.ADD));
    }
}
