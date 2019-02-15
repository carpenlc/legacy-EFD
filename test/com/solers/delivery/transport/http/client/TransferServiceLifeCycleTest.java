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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.solers.delivery.event.SynchronizationEvent;
import com.solers.delivery.transport.http.client.TransferService.TransportLifeCycleException;
import com.solers.delivery.transport.http.client.util.CompressionRules;


public class TransferServiceLifeCycleTest {
    
    private TestTransferService transferService;
    private MockHttpClient client;
    
    @Before
    public void setUp() {
        client = new MockHttpClient();
        transferService = new TestTransferService("name", "localhost", -1);
        transferService.client = client;
        transferService.setCompressionRules(new CompressionRules());
    }
    
    @Test
    public void testGoodLifeCycle() throws Exception{
        
        SynchronizationEvent event = new SynchronizationEvent("xxx", 1l);
        try {
            transferService.start(event);
            Assert.assertTrue(transferService.isRunning());
        } catch (TransportLifeCycleException e) {
            Assert.fail();
        }
        
        try {
            transferService.stop(event);
        } catch (TransportLifeCycleException e) {
            Assert.fail();
        }
        Assert.assertFalse(transferService.isRunning());
    }
    
    @Test
    public void testBadLifeCycle() throws Exception{
        
        SynchronizationEvent event = new SynchronizationEvent("xxx", 1l);
        try {
            transferService.start(event);
            Assert.assertTrue(transferService.isRunning());
        } catch (TransportLifeCycleException e) {
            Assert.fail();
        }
        
        try {
            transferService.start(event);
            Assert.fail();
        } catch (TransportLifeCycleException expected) {
            Assert.assertTrue(transferService.isRunning());
        }
    }
    
    @Test
    public void testProcessBeforeStart() throws Exception{
        
        try {
            transferService.process(new TransferContent("","","",1l,""));
            Assert.fail();
        } catch (TransportLifeCycleException expected) {
            Assert.assertFalse(transferService.isRunning());
        }
    }
    
    
    private class MockHttpClient extends HttpClient {
        @Override
        public int executeMethod(HttpMethod method) throws IOException {
            return 0;
        }
    }
}
