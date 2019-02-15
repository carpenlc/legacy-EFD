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
package com.solers.delivery.content.supplier;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.solers.delivery.domain.ContentSet;

public class MockSupplierContentSetManager extends SupplierContentSetManager {
    
    public ContentSet added;
    public Long removed;
    public Long started;
    private Map<Long, ContentSet> data = new HashMap<Long, ContentSet>();
    private File file;
    private Long id;
    
    public MockSupplierContentSetManager() {
        super(null,null);
    }
    
    @Override
    public void registerContentSet(ContentSet config) {
        this.contentSetMap.put(config.getId(), null);
        this.added = config;
        data.put(config.getId(), config);
    }
    
    @Override
    public void unregisterContentSet(Long id) {
        this.contentSetMap.remove(id);
        this.removed = id;
        
        data.remove(id);
    }
    
    @Override
    public void startContentSet(Long id) {
        started = id;
    }

    @Override
    public String getName(Long id) {
        ContentSet c = data.get(id);
        return c == null ? null : c.getName();
    }

    @Override
    public File getFile(String contentSetName, String pathFromRoot) {
        if (file != null) {
            return file;
        }
        return super.getFile(contentSetName, pathFromRoot);
    }
    
    @Override
    public Long getId(String contentSetName) {
        if (id != null) {
            return id;
        }
        return super.getId(contentSetName);
    }

    public void setFile(File file) {
        this.file = file;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
}
