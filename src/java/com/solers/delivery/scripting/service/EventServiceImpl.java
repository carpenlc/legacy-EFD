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
import com.solers.delivery.scripting.event.EventQueue;
import com.solers.delivery.scripting.event.EventType;

/**
 * @author mchen
 *
 */
public class EventServiceImpl implements EventService {
    private EventQueue queue;
    
    public void setEventQueue(EventQueue queue) {
        this.queue = queue;
    }
    
    /* (non-Javadoc)
     * @see com.solers.delivery.scripting.service.EventService#ready()
     */
    @Override
    public boolean ready() {
        return this.queue.hasConsumers();
    }

    /* (non-Javadoc)
     * @see com.solers.delivery.scripting.service.EventService#endConsumer(com.solers.delivery.event.DeliveryEvent)
     */
    @Override
    public void completedConsumer(DeliveryEvent event) {
        if(this.ready()) {
            this.queue.receivedEvent(event, EventType.COMPLETED_CONSUMER);
        }
    }

    /* (non-Javadoc)
     * @see com.solers.delivery.scripting.service.EventService#endSupplier(com.solers.delivery.event.DeliveryEvent)
     */
    @Override
    public void completedSupplier(DeliveryEvent event) {
        if(this.ready()) {
            this.queue.receivedEvent(event, EventType.COMPLETED_SUPPLIER);
        }
    }

    /* (non-Javadoc)
     * @see com.solers.delivery.scripting.service.EventService#receivedContent(com.solers.delivery.event.DeliveryEvent)
     */
    @Override
    public void receivedContent(DeliveryEvent event) {
        if(this.ready()) {
            this.queue.receivedEvent(event, EventType.RECEIVED_CONTENT);
        }
    }

    /* (non-Javadoc)
     * @see com.solers.delivery.scripting.service.EventService#startConsumer(com.solers.delivery.event.DeliveryEvent)
     */
    @Override
    public void startedConsumer(DeliveryEvent event) {
        if(this.ready()) {
            this.queue.receivedEvent(event, EventType.STARTED_CONSUMER);
        }
    }

    /* (non-Javadoc)
     * @see com.solers.delivery.scripting.service.EventService#startSupplier(com.solers.delivery.event.DeliveryEvent)
     */
    @Override
    public void startedSupplier(DeliveryEvent event) {
        if(this.ready()) {
            this.queue.receivedEvent(event, EventType.STARTED_SUPPLIER);
        }
    }

    /* (non-Javadoc)
     * @see com.solers.delivery.scripting.service.EventService#suppliedContent(com.solers.delivery.event.DeliveryEvent)
     */
    @Override
    public void suppliedContent(DeliveryEvent event) {
        if(this.ready()) {
            this.queue.receivedEvent(event, EventType.SUPPLIED_CONTENT);
        }
    }

}
