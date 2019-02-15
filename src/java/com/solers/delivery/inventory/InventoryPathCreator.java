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
package com.solers.delivery.inventory;

import java.io.File;

import com.solers.delivery.domain.ConsumerContentSet;
import com.solers.delivery.domain.ContentSet;

/**
 * Static methods to easily create inventory paths based on runtime directory and content set name being used
 * 
 * @author JGimourginas
 * 
 * Package protected; clients should go through InventoryFactory.
 */
final class InventoryPathCreator {
    private static final String INVENTORIES = "inventories";
    private static final String SUPPLIERS = "suppliers";
    private static final String CONSUMERS = "consumers";
    
    private static final String PARENT = "/" + INVENTORIES + "/%s/%s";
    
    private final String siteDirectory;;
    
    InventoryPathCreator(String siteDirectory) {
        this.siteDirectory = siteDirectory;
    }
        
    public String getRuntimeDirectory() {
        return siteDirectory;
    }
    
    public File getContentSetLocation(ContentSet config) {
        String type = config.isSupplier() ? SUPPLIERS : CONSUMERS;
        return new File(siteDirectory, String.format(PARENT, type, config.getName()));
    }
    
    public File getPackagedInventory(ContentSet config) {
        return getInventoryLocation(config, "packaged");
    }
    
    public File getPackagedInventoryFile(ConsumerContentSet config) {
        String fileName = String.format("%1$s" + InventoryBundler.EXTENSION, config.getName());
        File dir = getInventoryLocation(config, "packaged");
        return new File(dir, fileName);
    }
    
    public File getOpenInventory(ContentSet config) {
        return getInventoryLocation(config, "open");
    }
    
    private File getInventoryLocation(ContentSet config, String variant) {
        String type = null;
        String fileName = null;
        if (config.isSupplier()) {
            type = SUPPLIERS;
            fileName = String.format("/inventories/%s/%s/%s", type, config.getName(), variant);
        } else {
            type = CONSUMERS;
            fileName = String.format("/inventories/%s/%s/%s/%s", type, config.getName(), ((ConsumerContentSet)config).getSupplierName(), variant);
        }
        return new File(siteDirectory, fileName);
    }
}
