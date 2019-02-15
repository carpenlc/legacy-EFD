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

import com.solers.delivery.domain.ContentSet;

public class MockConsumerContentSetManager extends ConsumerContentSetManager {
    
    public ContentSet added;
    public Long removed;
    public Long started;
    
    public MockConsumerContentSetManager() {
        super(null,null);
    }
    
    @Override
    public void registerContentSet(ContentSet config) {
        this.contentSetMap.put(config.getId(), null);
        this.added = config;
    }
    
    @Override
    public void unregisterContentSet(Long id) {
        this.contentSetMap.remove(id);
        this.removed = id;
    }
    
    @Override
    public void startContentSet(Long id) {
        started = id;
    }
}