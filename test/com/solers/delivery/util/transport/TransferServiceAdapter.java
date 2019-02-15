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
package com.solers.delivery.util.transport;

import com.solers.delivery.event.SynchronizationEvent;
import com.solers.delivery.transport.http.client.Transfer;
import com.solers.delivery.transport.http.client.TransferService;

public abstract class TransferServiceAdapter implements TransferService {
    
    @Override
    public void start(SynchronizationEvent event) {
    }

    @Override
    public void stop(SynchronizationEvent event) {
    }
    
    @Override
    public void sendMetrics(long totalRequests, long totalBytes) {
    }
    
    @Override
    public abstract Transfer process(Transfer transfer);
}
