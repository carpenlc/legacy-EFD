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
package com.solers.delivery.transport.http.client.util;

import static com.solers.delivery.transport.http.HTTPStatusCodes.OK;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.solers.delivery.transport.http.HTTPHeaders;
import com.solers.delivery.transport.http.HTTPStatusCodes;
import com.solers.delivery.transport.http.client.Transfer;
import com.solers.delivery.transport.http.client.TransferContent;
import com.solers.delivery.transport.http.client.methods.ContentGetMethod;
import com.solers.delivery.transport.http.client.retry.RetryPolicy;
import com.solers.delivery.transport.http.client.retry.RetryPolicyLinearTime;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class TransferTaskTestCase {
    
    private MockHttpClient client;
    private MockContentCopy copier;
    private MockTransferContent transfer;
    private RetryPolicy policy;
    
    @Before
    public void setUp() {
        client = new MockHttpClient();
        copier = new MockContentCopy();
        transfer = new MockTransferContent("localPath", "remotePth", "contentSet", 1l, "syncId");
        policy = new RetryPolicyLinearTime(30, TimeUnit.SECONDS, new int []{1, 1}, TimeUnit.SECONDS);
    }
    
    @After
    public void tearDown() {
        // Clear the interrupt of the current thread.
        Thread.interrupted();
        Assert.assertFalse(Thread.currentThread().isInterrupted());
    }
    
    @Test
    public void testGetWithoutPermission() {
        transfer.code = HTTPStatusCodes.ACCESS_DENIED.code();
        
        TransferTask task = new TransferTask(client, transfer, copier, policy);
        
        task.call();
        
        Assert.assertEquals(Transfer.Status.FAILED_UNAUTHORIZED, transfer.getStatus());
        Assert.assertEquals(1, client.executes);
    }
    
    @Test
    public void testGetFromDisabledSupplier() {
        transfer.code = HTTPStatusCodes.DISABLED_CONTENT_SET.code();
        
        TransferTask task = new TransferTask(client, transfer, copier, policy);
        
        task.call();
        
        Assert.assertEquals(Transfer.Status.FAILED_SUPPLIER_IS_DISABLED, transfer.getStatus());
        Assert.assertEquals(1, client.executes);
    }
    
    @Test
    public void testGetGbs() {
        transfer.code = HTTPStatusCodes.GBS_DELIVERY.code();
       
        TransferTask task = new TransferTask(client, transfer, copier, policy);
        
        task.call();
        
        Assert.assertEquals(Transfer.Status.PENDING_GBS, transfer.getStatus());
        Assert.assertEquals(1, client.executes);
    }
    
    @Test
    public void testGetNonExistentSupplier() {
        transfer.code = HTTPStatusCodes.NO_CONTENT_SET_NAME.code();
        TransferTask task = new TransferTask(client, transfer, copier, policy);
        
        task.call();
        
        Assert.assertEquals(Transfer.Status.FAILED_INVALID_SUPPLIER, transfer.getStatus());
        Assert.assertEquals(1, client.executes);
    }
    
    @Test
    public void testInterruptedBeforeRetry() {
        int[] delaytimes = {5,5,5,5};
        TransferTask task = new TransferTask(client, transfer, copier, new RetryPolicyLinearTime(30, TimeUnit.SECONDS, delaytimes, TimeUnit.SECONDS));
        
        task.call();
        
        Assert.assertEquals(1, client.executes);
    }
    
    @Test
    public void testInterruptedDuringRetry() {
        transfer.callsBeforeCodeChangesToOK = 2;
        transfer.code = 500;
        int[] delaytimes = {1, 1};
        TransferTask task = new TransferTask(client, transfer, copier, new RetryPolicyLinearTime(2, TimeUnit.SECONDS, delaytimes, TimeUnit.SECONDS));
        
        task.call();
        
        Assert.assertEquals(2, client.executes);
    }
    
    @Test
    public void testSuccessOK() {
        transfer.callsBeforeCodeChangesToOK = 10;
        transfer.code = OK.code();
        copier = new DoNothingContentCopy();
        TransferTask task = new TransferTask(client, transfer, copier, policy);
        task.call();
        Assert.assertEquals(1, client.executes);
        Assert.assertEquals(Transfer.Status.COMPLETED, transfer.getStatus());
    }
    
    @Test
    public void testSuccessOKPartial() {
        transfer.callsBeforeCodeChangesToOK = 10;
        transfer.code = HTTPStatusCodes.OK_PARTIAL.code();
        copier = new DoNothingContentCopy();
        TransferTask task = new TransferTask(client, transfer, copier, policy);
        task.call();
        Assert.assertEquals(1, client.executes);
        Assert.assertEquals(Transfer.Status.COMPLETED, transfer.getStatus());
    }
    
    @Test
    public void testSuccessNotModified() {
        transfer.callsBeforeCodeChangesToOK = 10;
        transfer.code = HTTPStatusCodes.OK_NOT_MODIFIED.code();
        copier = new DoNothingContentCopy();
        TransferTask task = new TransferTask(client, transfer, copier, policy);
        task.call();
        Assert.assertEquals(1, client.executes);
        Assert.assertEquals(Transfer.Status.COMPLETE_NOT_MODIFIED, transfer.getStatus());
    }
    
    class MockTransferContent extends TransferContent {
        
        public int callsBeforeCodeChangesToOK = 10;
        public int code = OK.code();
        private int count = 0;
        
        public MockTransferContent(String localPath, String remotePath, String contentSetName, long fileSize, String syncId) {
            super(localPath, remotePath, contentSetName, fileSize, syncId);
        }

        @Override
        public HttpMethod getHttpMethod() {
            
            return new ContentGetMethod(new Session(contentSetName, syncId, HTTPHeaders.USER_AGENT.defaultValue()) , remotePath) {
                @Override
                public int getStatusCode() {
                    if (++count < callsBeforeCodeChangesToOK) {
                        return code;
                    }
                    return OK.code();
                }

                @Override
                public String getStatusText() {
                    return "";
                }
                
            };
        }
    }

    class MockContentCopy extends ContentCopy {
        
        @Override
        public void createFile(InputStream in, TransferContent transfer) throws FailedStreamToFileException, InterruptedException {
            throw new InterruptedException();
        }
        
    }
    
    class DoNothingContentCopy extends MockContentCopy {

        @Override
        public void createFile(InputStream in, TransferContent transfer) throws FailedStreamToFileException, InterruptedException {
            
        }
        
    }
    
    class MockHttpClient extends HttpClient {

        public int executes = 0;
        
        @Override
        public int executeMethod(HttpMethod method) throws IOException, HttpException {
            executes++;
            return 0;
        }
        
    }
    
}
