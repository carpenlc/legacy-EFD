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
package com.solers.jmx;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class MonitoredExecutorServiceTestCase {
    
    @Test
    public void testGetPercentFull() throws Exception {
        MonitoredExecutorService s = new MonitoredExecutorService(2, 4, "");
        
        Assert.assertEquals(0.0d, s.getPercentFull(), 0.0d);
        
        CountDownLatch latch = new CountDownLatch(2);
        
        s.submit(new BlockingCallable(latch));
        s.submit(new BlockingCallable(latch));
        s.submit(new BlockingCallable(latch));
        s.submit(new BlockingCallable(latch));
        
        latch.await(5, TimeUnit.SECONDS);
       
        Assert.assertEquals(50.0d, s.getPercentFull(), 0.0d); 
    }
    
    @Test
    public void testGetProcessedTasks() throws Exception {
        MonitoredExecutorService s = new MonitoredExecutorService(1, 4, "");
        
        Assert.assertEquals(0, s.getProcessedTasks());
        
        CountDownLatch latch = new CountDownLatch(4);
        
        s.submit(new DoNothing(latch));
        s.submit(new DoNothing(latch));
        s.submit(new DoNothing(latch));
        s.submit(new DoNothing(latch));
        
        latch.await(5, TimeUnit.SECONDS);
        
        Assert.assertEquals(4.0d, s.getProcessedTasks(), 1.0d);
    }
    
    @Test
    public void testGetActiveTasks() throws Exception {
        MonitoredExecutorService s = new MonitoredExecutorService(2, 4, "");
        
        Assert.assertEquals(0, s.getActiveTasks());
        
        CountDownLatch latch = new CountDownLatch(2);
        
        s.submit(new BlockingCallable(latch));
        s.submit(new BlockingCallable(latch));
        s.submit(new BlockingCallable(latch));
        s.submit(new BlockingCallable(latch));
        
        latch.await(5, TimeUnit.SECONDS);
       
        Assert.assertEquals(2.0d, s.getActiveTasks(), 1.0d);
    }
    
    @Test
    public void testGetWaitingTasks() throws Exception {
        MonitoredExecutorService s = new MonitoredExecutorService(3, 7, "");
        
        Assert.assertEquals(0, s.getWaitingTasks());
        
        CountDownLatch latch = new CountDownLatch(2);
        
        s.submit(new BlockingCallable(latch));
        s.submit(new BlockingCallable(latch));
        s.submit(new BlockingCallable(latch));
        s.submit(new BlockingCallable(latch));
        
        latch.await(5, TimeUnit.SECONDS);
       
        Assert.assertEquals(1.0d, s.getWaitingTasks(), 1.0d);
    }
    
    private static class BlockingCallable implements Callable<Void> {
        
        private final CountDownLatch latch;
        
        public BlockingCallable(CountDownLatch latch) {
            this.latch = latch;
        }

        public Void call() throws Exception {
            synchronized(this) {
                latch.countDown();
                wait();
            }
            return null;
        }
    }
    
    private static class DoNothing implements Runnable {
        private final CountDownLatch latch;
        
        public DoNothing(CountDownLatch latch) {
            this.latch = latch;
        }

        public void run() {
            latch.countDown();
        }
    }
    
}
