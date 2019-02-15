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
package com.solers.delivery.content;

import java.util.HashMap;
import java.util.Map;
import java.util.Collection;

import javax.annotation.PreDestroy;

import com.solers.delivery.content.scheduler.ContentSetScheduler;
import com.solers.delivery.content.scheduler.ContentSetSchedulerFactory;
import com.solers.delivery.content.supplier.UnregisteredContentSetException;
import com.solers.delivery.domain.ContentSet;
import com.solers.delivery.transport.gbs.GBSService;

public abstract class ContentSetManager {

    protected final Map<Long, ContentSetScheduler> contentSetMap;
    private final GBSService gbsService;
    private final ContentSetSchedulerFactory factory;
    
    public ContentSetManager(GBSService gbsService, ContentSetSchedulerFactory factory) {
        this.gbsService = gbsService;
        this.factory = factory;
        contentSetMap = new HashMap<Long, ContentSetScheduler>();
    }

    public abstract void registerContentSet(ContentSet config);

    @PreDestroy
    public void shutdown() {
        for (ContentSetScheduler scheduler : contentSetMap.values()) {
            scheduler.stop();
        }
    }
    
    public String getName(Long id) {
        if (contentSetMap.containsKey(id)) {
            return contentSetMap.get(id).getConfig().getName();
        } else {
            throw new UnregisteredContentSetException("id: " + id + " is not registered " + "as a ContentSet", String.valueOf(id));
        }
    }

    protected void addContentSet(ContentSet contentSet) {
        gbsService.addContentSet(contentSet);
    }
    
    public void unregisterContentSet(Long id) {
        if (contentSetMap.containsKey(id)) {
            ContentSetScheduler scheduler = this.getScheduler(id);
            scheduler.stop();
            this.contentSetMap.remove(id);
            gbsService.removeContentSet(id);
        }
    }
    
    protected void startContentSet(Long id) {
        getScheduler(id).start();
        gbsService.startContentSet(id);
    }

    protected void stopContentSet(Long id) {
        getScheduler(id).stop();
        gbsService.stopContentSet(id);
    }
    
    protected void addSchedulerAndConfig(ContentSet contentSet, Runnable task) {
        ContentSetScheduler scheduler = factory.newScheduler(task, contentSet);
        contentSetMap.put(contentSet.getId(), scheduler);
    }
    
    protected Collection<ContentSetScheduler> getSchedulers() {
        return contentSetMap.values();
    }

    private ContentSetScheduler getScheduler(Long id) {
        if (contentSetMap.containsKey(id)) {
            return this.contentSetMap.get(id);
        } else {
            throw new UnregisteredContentSetException("id: " + id + " is not registered as a ContentSet", id.toString());
        }
    }
}