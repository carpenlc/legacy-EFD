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

import java.util.concurrent.Callable;

import org.apache.commons.httpclient.HttpClient;

import com.solers.delivery.transport.http.monitor.MockTransferMonitor;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class TestTransferService extends TransferServiceImpl {

    public HttpClient client;
    public MockTransferMonitor monitor;
    public Callable<Transfer> task;
    
    public TestTransferService() {
        this("test", "localhost", -1);
    }
    
    public TestTransferService(String contentSetName, String host, int port) {
        super(contentSetName, host, port);
        monitor = new MockTransferMonitor();
        setTransferMonitor(monitor);
    }

    @Override
    protected HttpClient getClient() {
        return client;
    }

    @Override
    public void init() {
    }

    @Override
    protected Callable<Transfer> getTask(Transfer transfer) {
        if (task == null) {
            return super.getTask(transfer);
        }
        return task;
    }

}
