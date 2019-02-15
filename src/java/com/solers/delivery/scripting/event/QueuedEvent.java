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

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

import com.solers.delivery.event.DeliveryEvent;

/**
 * @author mchen
 *
 */
public final class QueuedEvent implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 6923530381533860486L;
    private final static transient AtomicLong count = new AtomicLong(1);
    private long id;
    private EventType eventType;
    private DeliveryEvent event;
    private String out = null;
    
    public QueuedEvent(EventType type, DeliveryEvent event) {
        this.id = count.getAndIncrement();
        this.setEventType(type);
        this.setEvent(event);
    }

    /**
     * @param type the type to set
     */
    private void setEventType(EventType type) {
        this.eventType = type;
    }

    /**
     * @return the type
     */
    public EventType getEventType() {
        return eventType;
    }

    /**
     * @param event the event to set
     */
    private void setEvent(DeliveryEvent event) {
        this.event = event;
    }

    /**
     * @return the event
     */
    public DeliveryEvent getDeliveryEvent() {
        return event;
    }

    /**
     * Objects are equal if the id is the same
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof QueuedEvent)) {
            return false;
        }
        
        QueuedEvent target = (QueuedEvent) obj;
        
        return this.id == target.id;
    }

    /**
     * Returns sum of hash codes of significant fields
     */
    @Override
    public int hashCode() {
        return this.eventType.hashCode() + (this.event == null ? 0 : this.event.hashCode());
    }

    @Override
    public String toString() {
        if (out == null) {
            StringBuilder builder = new StringBuilder();
            builder.append("Id    : ");
            builder.append(this.id);
            builder.append("\n");
            builder.append("Type  : ");
            builder.append(this.eventType);
            builder.append("\n");
            builder.append("Event : ");
            builder.append(this.event);
            out = builder.toString();
        }
        
        return out;
    }

    /**
     * Returns auto generated id
     * @return id - sequential id of long type
     */
    public long getId() {
        return this.id;
    }
}
