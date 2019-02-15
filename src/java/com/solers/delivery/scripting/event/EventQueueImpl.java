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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.solers.delivery.event.DeliveryEvent;

/**
 * @author mchen
 *
 */
public class EventQueueImpl implements EventQueue {
    private final static Logger log = Logger.getLogger(EventQueueImpl.class);
    private final BlockingQueue<QueuedEvent> queue;
    private final EventConsumers<Consumer<DeliveryEventConsumer>> consumers;
    private final ExecutorService pool = Executors.newSingleThreadExecutor();
    
    public EventQueueImpl(LinkedBlockingQueue<QueuedEvent> queue, EventConsumers<Consumer<DeliveryEventConsumer>> consumers) {
        this.queue = queue;
        this.consumers = consumers;
    }
        
    /* (non-Javadoc)
     * @see com.solers.delivery.scripting.foo.EventQueue#receivedEvent(com.solers.delivery.event.DeliveryEvent, com.solers.delivery.scripting.event.EventType)
     */
    @Override
    public void receivedEvent(DeliveryEvent event, EventType eventType) {
        try {
            final QueuedEvent queuedEvent = new QueuedEvent(eventType, event);
            //execute in a different thread to not block the caller 
            //since queue.put() is a blocking call.
            this.pool.execute(new Runnable() {

                @Override
                public void run() {
                    try {
                        queue.put(queuedEvent);
                    } catch (InterruptedException e) {
                        //ignore
                    }
                    
                }});
        } catch (Exception ex) {
            log.error("Error putting event into queue - " + eventType + ": " + event);
            log.error(ex);
        }
    }
    
    /* (non-Javadoc)
     * @see com.solers.delivery.scripting.foo.EventQueue#waitForEvents(long)
     */
    @Override
    public void waitForEvents(long timeoutMillis) {
        
        long startTime = System.currentTimeMillis();
        long waitTime = timeoutMillis;
        
        try {
            while(!Thread.currentThread().isInterrupted() && waitTime > 0) {
                QueuedEvent event = queue.poll(timeoutMillis, TimeUnit.MILLISECONDS);
                if (event != null) {
                    Iterator<Consumer<DeliveryEventConsumer>> it = this.consumers.iterator();
                    while(it.hasNext()) {
                        event.getEventType().receivedEvent(it.next().getConsumer(), event.getDeliveryEvent());
                    }
                }

                waitTime = timeoutMillis - (System.currentTimeMillis() - startTime);
            }
        } catch (Exception ex) {
            log.error("Error retrieving event from the queue");
            log.error(ex);
        }
    }

    /* (non-Javadoc)
     * @see com.solers.delivery.scripting.foo.EventQueue#hasConsumers()
     */
    @Override
    public boolean hasConsumers() {
        return this.consumers.size() > 0;
    }
}
