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

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author mchen
 *
 */
public class MapEventConsumersImpl<V> implements EventConsumers<Consumer<V>> {
    private transient final Map<String, Consumer<V>> consumers = new ConcurrentHashMap<String, Consumer<V>>();

    /* (non-Javadoc)
     * @see com.solers.delivery.scripting.event.DeliveryEventProducer#addConsumer(com.solers.delivery.scripting.event.Consumer)
     */
    @Override
    public void addConsumer(Consumer<V> consumer) {
        if (consumer != null) {
            this.consumers.put(consumer.getFileName(), consumer);
        }
    }

    /* (non-Javadoc)
     * @see com.solers.delivery.scripting.event.DeliveryEventProducer#consumerCount()
     */
    @Override
    public int size() {
        return this.consumers.size();
    }

    /* (non-Javadoc)
     * @see com.solers.delivery.scripting.event.DeliveryEventProducer#removeConsumer(com.solers.delivery.scripting.event.Consumer)
     */
    @Override
    public void removeConsumer(Consumer<V> consumer) {
        if (consumer != null) {
            this.consumers.remove(consumer.getFileName());
        }
    }

    /* (non-Javadoc)
     * @see com.solers.delivery.scripting.foo.DeliveryConsumerSet#iterator()
     */
    @Override
    public Iterator<Consumer<V>> iterator() {
        return this.consumers.values().iterator();
    }
    
}
