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
/**
 * 
 */
package com.solers.delivery.scripting.service;

import com.solers.delivery.event.DeliveryEvent;

/**
 * @author mchen
 *
 */
public interface EventService {

    /**
     * Call this to check if the server has consumers ready to consume the events.
     * Client side will throw a connection exception if the server is not up.
     * 
     * @return true if there is at least one consumer ready to consume the events, false otherwise.
     */
    public boolean ready();

    /**
     * Call this when there is a consumer synchronization started event
     * 
     * @param event - the synchronization event.
     */
    public void startedConsumer(DeliveryEvent event) ;

    /**
     * Call this when there is a consumer synchronization completed event
     * 
     * @param event - the synchronization event.
     */
    public void completedConsumer(DeliveryEvent event) ;

    /**
     * Call this when there is a supplier synchronization started event
     * 
     * @param event - the synchronization event.
     */
    public void startedSupplier(DeliveryEvent event) ;

    /**
     * Call this when there is a supplier synchronization started event
     * 
     * @param event - the synchronization event.
     */
    public void completedSupplier(DeliveryEvent event) ;

    /**
     * Call this when there is a content set supplied event
     * 
     * @param event - the content set event.
     */
    public void suppliedContent(DeliveryEvent event) ;

    /**
     * Call this when there is a content set received event
     * 
     * @param event - the content set event.
     */
    public void receivedContent(DeliveryEvent event) ;
}
