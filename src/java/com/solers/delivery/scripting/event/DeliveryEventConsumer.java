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
package com.solers.delivery.scripting.event;

import com.solers.delivery.event.ContentEvent;
import com.solers.delivery.event.SynchronizationEvent;

public interface DeliveryEventConsumer {
    /**
     * Call this when there is a supplier synchronization started event
     * @param event - supplier synch started event
     */
    void startedSupplier(SynchronizationEvent event);

    /**
     * Call this when there is a supplier synchronization completed event
     * @param event - supplier synch completed event
     */
    void completedSupplier(SynchronizationEvent event);

    /**
     * Call this when there is a consumer synchronization started event
     * @param event - consumer synch started event
     */
    void startedConsumer(SynchronizationEvent event);

    /**
     * Call this when there is a consumer synchronization started event
     * @param event - consumer synch started event
     */
    void completedConsumer(SynchronizationEvent event);

    /**
     * Call this when there is a content set received event
     * @param event - content set received event
     */
    void receivedContent(ContentEvent event);
    
    /**
     * Call this when there is a content set supplied event
     * @param event - content set supplied event
     */
    void suppliedContent(ContentEvent event);
}
