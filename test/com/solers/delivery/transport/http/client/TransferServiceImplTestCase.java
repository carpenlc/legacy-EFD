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
package com.solers.delivery.transport.http.client;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.solers.delivery.event.SynchronizationEvent;
import com.solers.delivery.transport.http.TransferStatus;
import com.solers.delivery.transport.http.client.TransferService.TransportUnavailableException;
import com.solers.delivery.transport.http.client.util.CompressionRules;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class TransferServiceImplTestCase {
    
    private TestTransferService transferService;
    private MockHttpClient client;
    
    @Before
    public void setUp() {
        client = new MockHttpClient();
        transferService = new TestTransferService("name", "localhost", -1);
        transferService.client = client;
        transferService.setCompressionRules(new CompressionRules());
    }
    
    // Management methods should not submit while a current task is running
    //@Test
    public void testManagementMethod() throws Exception {
        ExecutorService s = Executors.newSingleThreadExecutor();
        final CountDownLatch latch = new CountDownLatch(1);
        Callable<Transfer> task = new Callable<Transfer>() {
            public Transfer call() throws Exception {
                latch.await();
                return null;
            }
        };
        transferService.task = task;
        
        Assert.assertEquals(0, client.executes);
        transferService.start(new SynchronizationEvent(1L));
        Assert.assertEquals(2, client.executes);
        
        
        s.submit(new Runnable() {
            public void run() {
                transferService.process(new TransferInventory("", "", 1L, ""));
            }
        });
        transferService.stop(new SynchronizationEvent(1L));
        Assert.assertEquals(1, client.executes);
        latch.countDown();
        s.shutdown();
        s.awaitTermination(5, TimeUnit.SECONDS);
        Assert.assertEquals(2, client.executes);
    }
    
    @Test
    public void testConnectError() throws Exception{
        client.ioException = new UnknownHostException();
        
        SynchronizationEvent event = new SynchronizationEvent("xxx", 1l);
        try {
            transferService.start(event);
            Assert.fail();
        } catch (TransportUnavailableException expected) {
        }
        
        Assert.assertFalse(transferService.isRunning());
        
        try {
            transferService.stop(event);
        } catch (TransportUnavailableException expected) {
            Assert.fail();
        }
        Assert.assertFalse(transferService.isRunning());
    }
    
    @Test
    public void testConcurrentGetCurrentTransfers() throws Exception {
        transferService.task = new Callable<Transfer>() { public Transfer call() {return null;} };
        
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
                    for (int i=0; i < runs && failed.get() == false; i++) {
                        List<TransferStatus> list = transferService.getCurrentTransfers();
                        
                        // Simulate the conversion list undergoes by the mbean OpenConverter
                        Object[] openArray = (Object[])
                        Array.newInstance(TransferStatus.class, list.size());
                        int j = 0;
                        for (Object o : list) {
                            openArray[j++] = o;
                        }
                    }
                } catch (RuntimeException ex) {
                    ex.printStackTrace();
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
                    for (int i=0; i < runs && failed.get() == false; i++) {
                        TransferContent c = new TransferContent("", "", "", 1L, "");
                        transferService.process(c);
                    }
                } catch (RuntimeException ex) {
                    ex.printStackTrace();
                    message.append(ex.getClass().toString());
                    failed.compareAndSet(false, true);
                }
                
                return null;
            }
        };
        
        transferService.start(new SynchronizationEvent("",1l));
        for (int i=0; i< (threads / 2); i++) {
            service.submit(reader);
            service.submit(writer);
        }
        //transferService.stop(new SynchronizationEvent("",1l));
        service.shutdown();
        boolean completed = service.awaitTermination(4, TimeUnit.MINUTES);
        
        if (!completed) {
            Assert.fail("Timed out waiting for executor");
        }
        
        if (failed.get()) {
            Assert.fail(message.toString());
        }
    }
    
    private class MockHttpClient extends HttpClient {

        public int executes = 0;
        public IOException ioException = null;
        
        @Override
        public int executeMethod(HttpMethod method) throws IOException, HttpException {
            executes++;
            if (ioException != null) {
                throw ioException;
            }
            return 0;
        }
        
    }
}
