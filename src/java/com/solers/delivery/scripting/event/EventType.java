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

import com.solers.delivery.event.BaseEvent;
import com.solers.delivery.event.ContentEvent;
import com.solers.delivery.event.DeliveryEvent;
import com.solers.delivery.event.SynchronizationEvent;
import com.solers.delivery.scripting.service.EventService;

/**
 * @author mchen
 *
 */
public enum EventType {
    STARTED_SUPPLIER {
        public void sendEvent(EventService service, DeliveryEvent event) {
            service.startedSupplier(event);
        }

        @Override
        public void receivedEvent(DeliveryEventConsumer deliveryEventConsumer, DeliveryEvent event) {
            deliveryEventConsumer.startedSupplier((SynchronizationEvent)event);
            
        }

        @Override
        public Class<SynchronizationEvent> getType() {
            return SynchronizationEvent.class;
        }
    },
    COMPLETED_SUPPLIER {
        public void sendEvent(EventService service, DeliveryEvent event) {
            service.completedSupplier(event);
        }

        @Override
        public void receivedEvent(DeliveryEventConsumer deliveryEventConsumer, DeliveryEvent event) {
            deliveryEventConsumer.completedSupplier((SynchronizationEvent)event);
        }

        @Override
        public Class<SynchronizationEvent> getType() {
            return SynchronizationEvent.class;
        }
    },
    STARTED_CONSUMER {
        public void sendEvent(EventService service, DeliveryEvent event) {
            service.startedConsumer(event);
        }

        @Override
        public void receivedEvent(DeliveryEventConsumer deliveryEventConsumer, DeliveryEvent event) {
            deliveryEventConsumer.startedConsumer((SynchronizationEvent)event);
        }

        @Override
        public Class<SynchronizationEvent> getType() {
            return SynchronizationEvent.class;
        }
    },
    COMPLETED_CONSUMER {
        public void sendEvent(EventService service, DeliveryEvent event) {
            service.completedConsumer(event);
        }

        @Override
        public void receivedEvent(DeliveryEventConsumer deliveryEventConsumer, DeliveryEvent event) {
            deliveryEventConsumer.completedConsumer((SynchronizationEvent)event);
        }

        @Override
        public Class<SynchronizationEvent> getType() {
            return SynchronizationEvent.class;
        }
    },
    SUPPLIED_CONTENT {
        public void sendEvent(EventService service, DeliveryEvent event) {
            service.suppliedContent(event);
        }

        @Override
        public void receivedEvent(DeliveryEventConsumer deliveryEventConsumer, DeliveryEvent event) {
            deliveryEventConsumer.suppliedContent((ContentEvent)event);
        }

        @Override
        public Class<ContentEvent> getType() {
            return ContentEvent.class;
        }
    },
    RECEIVED_CONTENT {
        public void sendEvent(EventService service, DeliveryEvent event) {
            service.receivedContent(event);
        }

        @Override
        public void receivedEvent(DeliveryEventConsumer deliveryEventConsumer, DeliveryEvent event) {
            deliveryEventConsumer.receivedContent((ContentEvent)event);
        }

        @Override
        public Class<ContentEvent> getType() {
            return ContentEvent.class;
        }
    };

    public abstract void sendEvent(EventService service, DeliveryEvent event);
    public abstract void receivedEvent(DeliveryEventConsumer deliveryEventConsumer, DeliveryEvent event);
    public abstract Class<? extends BaseEvent> getType();
}
