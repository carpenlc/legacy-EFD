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
package com.solers.delivery.scripting.event;

import com.solers.delivery.event.DeliveryEvent;

/**
 * @author mchen
 *
 */
public interface EventQueue {
    /**
     * Processes an event received from the event provider
     * @param event - a delivery event
     * @param type - Enum depicting the type of event
     */
    public void receivedEvent(DeliveryEvent event, EventType type);

    /**
     * Waits for event to process
     * @param timeInMillis - milliseconds to wait before timing out
     */
    public void waitForEvents(long timeInMillis);
    
    /**
     * Checks if there are consumers waiting for events
     * @return true if there are consumers, false otherwise
     */
    public boolean hasConsumers();

}
