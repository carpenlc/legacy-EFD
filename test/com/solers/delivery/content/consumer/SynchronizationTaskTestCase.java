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
package com.solers.delivery.content.consumer;

import java.io.IOException;
import java.net.UnknownHostException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.junit.Assert;
import org.junit.Test;

import com.solers.delivery.content.status.SynchronizationResult;
import com.solers.delivery.domain.ConsumerContentSet;
import com.solers.delivery.event.SynchronizationEvent;
import com.solers.delivery.event.listener.BaseListener;
import com.solers.delivery.transport.http.client.TestTransferService;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class SynchronizationTaskTestCase {
    
    @Test
    public void testTransportUnavailable() {
        ConsumerContentSet contentSet = new ConsumerContentSet();
        contentSet.setId(1l);
        contentSet.setName("testTransportUnavailable");
        contentSet.setPath("path");
        
        MockHttpClient client = new MockHttpClient();
        client.ioException = new UnknownHostException();
        Listener l = new Listener();
        TestTransferService service = new TestTransferService();
        service.client = client;
        
        SynchronizationTask task = new SynchronizationTask(contentSet, service);
        task.setEventManager(l);
        
        task.run();
        
        Assert.assertEquals(SynchronizationResult.FAILED_NO_CONNECTION, l.end.getResult());
    }
    
    private class Listener extends BaseListener {

        public SynchronizationEvent start;
        public SynchronizationEvent end;
        
        @Override
        public void consumerSynchronizationCompleted(SynchronizationEvent event) {
            end = event;
        }

        @Override
        public void consumerSynchronizationStarted(SynchronizationEvent event) {
            start = event;
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
