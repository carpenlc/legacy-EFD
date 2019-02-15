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


import com.solers.delivery.content.ContentSetManager;
import com.solers.delivery.content.scheduler.ContentSetSchedulerFactory;
import com.solers.delivery.domain.ConsumerContentSet;
import com.solers.delivery.domain.ContentSet;
import com.solers.delivery.transport.gbs.GBSService;

public class ConsumerContentSetManager extends ContentSetManager {
    
    public ConsumerContentSetManager(GBSService poller, ContentSetSchedulerFactory factory) {
        super(poller, factory);
    }
    
    @Override
    public void registerContentSet(ContentSet contentSet) {
        unregisterContentSet(contentSet.getId());

        if (contentSet instanceof ConsumerContentSet) {
            Runnable task = new SynchronizationTask((ConsumerContentSet) contentSet);
            this.addSchedulerAndConfig(contentSet, task);
            addContentSet(contentSet);
        }
        
        if (contentSet.isEnabled()) {
            startContentSet(contentSet.getId());
        }
    }
}
