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

import com.solers.delivery.content.status.SynchronizationResult;
import com.solers.delivery.transport.http.client.Transfer;
import com.solers.delivery.transport.http.client.Transfer.Status;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class InventoryResult {
    
    private final boolean successful;
    private final Transfer.Status status;
    
    public InventoryResult(boolean successful, Status status) {
        this.successful = successful;
        this.status = status;
    }
 
    public boolean successful() {
        return successful;
    }
    
    public Transfer.Status getStatus() {
        return status;
    }
    
    public SynchronizationResult getSynchronizationResult() {
        switch(status) {
            case FAILED_NO_CONNECTION:
                return SynchronizationResult.FAILED_NO_CONNECTION;
            case FAILED_INVALID_SUPPLIER:
                return SynchronizationResult.FAILED_INVALID_SUPPLIER;
            case FAILED_SUPPLIER_IS_DISABLED:
                return SynchronizationResult.FAILED_SUPPLIER_IS_DISABLED;
            case FAILED_UNAUTHORIZED:
                return SynchronizationResult.FAILED_UNAUTHORIZED;
            default:
                return SynchronizationResult.FAILED;
        }
    }
}
