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
package com.solers.delivery.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.solers.delivery.event.listener.EventListener;
import com.solers.delivery.event.listener.MockEventListener;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class MockEventManager extends EventManager {

    List<BaseEvent> events = new ArrayList<BaseEvent>();

    public MockEventManager() {
        super(listener());
    }

    @Override
    public void consumerSynchronizationStarted(SynchronizationEvent event) {
        events.add(event);
    }

    @Override
    public void received(PendingGBSUpdateEvent event) {
        events.add(event);
    }
    
    @Override
    public void consumerSynchronizationCompleted(SynchronizationEvent event) {
        events.add(event);
    }
    
    @Override
    public void received(ContentEvent event) {
        events.add(event);
    }

    @Override
    public void supplied(ContentEvent event) {
        events.add(event);
    }

    public BaseEvent getMostRecent() {
        return events.get(0);
    }
    
    public List<BaseEvent> getEvents() {
        return Collections.unmodifiableList(events);
    }
    
    private static List<EventListener> listener() {
        EventListener x = new MockEventListener(0, 6000);
        List<EventListener> result = Arrays.asList(x);
        return result;
    }
}
