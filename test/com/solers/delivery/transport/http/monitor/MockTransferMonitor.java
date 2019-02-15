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

import com.solers.delivery.event.SynchronizationEvent;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class MockTransferMonitor extends TransferMonitor {

    public SynchronizationEvent start;
    public SynchronizationEvent end;
    
    @Override
    public void startConsumer(SynchronizationEvent event) {
        start = event;
    }

    @Override
    public void completeConsumer(SynchronizationEvent event) {
        end = event;
    }

}
