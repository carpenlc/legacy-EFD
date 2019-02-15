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
import java.io.FileFilter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.solers.delivery.content.ContentSetManager;
import com.solers.delivery.content.scheduler.ContentSetScheduler;
import com.solers.delivery.content.scheduler.ContentSetSchedulerFactory;
import com.solers.delivery.domain.ContentSet;
import com.solers.delivery.inventory.InventoryBundler;
import com.solers.delivery.inventory.InventoryFactory;
import com.solers.delivery.transport.gbs.GBSService;
import com.solers.util.Callback;


public class SupplierContentSetManager extends ContentSetManager implements ContentSetMapper {
    private static final Logger log = Logger.getLogger(SupplierContentSetManager.class);

    private final Set<Callback<ContentSet>> disabledListeners;
    
    public SupplierContentSetManager(GBSService gbsSupplier, ContentSetSchedulerFactory factory) {
        super(gbsSupplier, factory);
        this.disabledListeners = new HashSet<Callback<ContentSet>>();
    }

    @Override
    public void registerContentSet(ContentSet contentSet) {
        unregisterContentSet(contentSet.getId());

        Runnable task = new SupplierTask(contentSet);
        this.addSchedulerAndConfig(contentSet, task);
        addContentSet(contentSet);
        
        if (contentSet.isEnabled() == false) {
            for (Callback<ContentSet> c : this.disabledListeners) {
                c.call(contentSet);
            }
        }
        if (contentSet.isEnabled()) {
            startContentSet(contentSet.getId());
        }
    }

    @Override
    public boolean isInventory(String path) {
        return path.equals("/");
    }

    @Override
    public File getFile(String contentSetName, String pathFromRoot) {
        return getFile(contentSetName, pathFromRoot, 0);
    }
    
    @Override
    public File getFile(String contentSetName, String pathFromRoot, long timestamp) {
        ContentSet contentSet = getContentSet(contentSetName);
        
        if (!contentSet.isEnabled()) {
            throw new DisabledContentSetException("contentSetName: " + contentSetName + " is currently disabled");
        }
        
        File result = null;
         
        if (isInventory(pathFromRoot)) {
            File packageDir = InventoryFactory.getPackageLocation(contentSet);
            File requestedInventory = new File(packageDir, contentSetName + "_" + String.valueOf(timestamp) + InventoryBundler.EXTENSION);
            
            boolean requestExists = requestedInventory.exists();

            //Get list of available packages.
            File[] packages = packageDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isFile() && pathname.getName().endsWith(InventoryBundler.EXTENSION);
                }
            });
            
            //No packages available.
            if (packages == null || packages.length == 0) {
                throw new InventoryNotAvailableException("Content set " + contentSetName + " has no inventory bundles available to transport.");
            }
            
            Arrays.sort(packages);
            
            //Check to see if request is latest
            if (requestExists) {
                if (requestedInventory.getName().equals(packages[packages.length - 1].getName())) {
                    throw new InventoryNotChangedException(String.valueOf(timestamp));
                }
                return requestedInventory;
            }
            
            //Request was not the latest and it did not exist as a bundled package, so return
            //the latest inventory bundle available.
            return packages[packages.length - 1];
        } else {
            // this is a content request
            String pathToRoot = contentSet.getPath();
            // pathFromRoot will include the root itself
            // cut root out of the pathToRoot
            pathToRoot = pathToRoot.substring(0, pathToRoot.lastIndexOf("/"));
            // now append pathFromRoot to pathToRoot
            result = new File(pathToRoot, pathFromRoot);
        }
           
        log.debug("getFile returning: " + result.getPath());
        return result;
    }

    public Long getId(String contentSetName) {
        return this.getContentSet(contentSetName).getId();
    }

    @Override
    public boolean isAllowed(String contentSetName, String commonName) {
        ContentSet contentSet = getContentSet(contentSetName);
        return contentSet.allows(commonName);
    }

    @Override
    public void addDisabledListener(Callback<ContentSet> e) {
        this.disabledListeners.add(e);
    }

    @Override
    public void removeDisabledListener(Callback<ContentSet> e) {
        this.disabledListeners.remove(e);
    }
    
    private ContentSet getContentSet(String contentSetName) {
        for (ContentSetScheduler scheduler : getSchedulers()) {
            if (scheduler.getConfig().getName().equals(contentSetName)) {
                return scheduler.getConfig();
            }
        }
        throw new UnregisteredContentSetException("contentSetName: " + contentSetName + " is not registered as a ContentSet", contentSetName);
    }
}