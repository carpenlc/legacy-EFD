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
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;

import com.solers.delivery.content.scheduler.ScheduleUtil;
import com.solers.delivery.domain.ContentSet;
import com.solers.delivery.inventory.Inventory;
import com.solers.delivery.inventory.InventoryBundler;
import com.solers.delivery.inventory.InventoryFactory;
import com.solers.delivery.inventory.plugin.PluginException;
import com.solers.delivery.util.FileSystemUtil;
import com.solers.delivery.util.FileSystemUtil.Stats;
import com.solers.util.unit.TimeIntervalUnit;

/**
 * InventoryCreationTask is responsible for creating/updating an Inventory to reflect the content in a Supplier Content Set. The task will then package the
 * Inventory and place it in a location where the Supplier can distribute it to Consumers. The ICT should always be created through the ICTFactory, which is
 * responsible for retrieving required properties from Spring and populating the member variables of the class.
 * 
 * @author JGimourginas
 */
public class InventoryCreationTask implements Runnable {

    private static final Logger log = Logger.getLogger(InventoryCreationTask.class);
    
    private final InventoryBundler bundler;
    private final ContentSet config;
    private final InventoryDifferencePruner pruner;
    
    private long size = -1;
    private long fileCount = -1;
    private long lastRuntime = -1;
    private InventoryCreationState state = InventoryCreationState.IDLE;
    protected Enum<?> substate = null;

    public InventoryCreationTask(ContentSet config) {
        this.config = config;
        this.pruner = new InventoryDifferencePruner(config);
        this.bundler = new InventoryBundler(config);
        this.bundler.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                if (arg instanceof Enum) {
                    substate = (Enum<?>) arg;
                }
            }
        });
    }

    public void run() {
        try {
            log.info("Creating inventory for: " + config.getPath());
            long elapsed = createInventory();
            log.info(String.format("Created Inventory for %s in %s.", config.getPath(), TimeIntervalUnit.format(elapsed)));
        } catch (Exception e) {
            if (Thread.currentThread().isInterrupted()) {
                Thread.currentThread().interrupt();
                log.warn("Cannot generate inventory for " + config.getPath() + " - " + e.getMessage());
            } else {
                log.error("Error generating inventory for " + config.getPath() + ". New inventory may not exist.", e);
            }
        } finally {
            try {
                state = InventoryCreationState.INSPECTING_FILESYSTEM;
                File supplierDir = new File(config.getPath());
                Stats s = FileSystemUtil.calculateDirectoryStatistics(supplierDir);
                if (s != null) {
                    size = s.getSize();
                    fileCount = s.getItems();
                }
            } finally {
                lastRuntime = System.currentTimeMillis();
                substate = null; //sanity check
                state = InventoryCreationState.IDLE;
            }
        }
    }

    private long createInventory() throws IOException, PluginException {
        long timeElapsed = 0;
        Inventory current = null;
        try {
            current = InventoryFactory.getInventory(config, false);
            File path = new File(config.getPath());
            if (current != null && current.getRootName().equals(path.getName())) {
                //scan for differences
                long start = System.currentTimeMillis();
                state = InventoryCreationState.SCANNING_FOR_DIFFERENCES;
                File inventoryDifference = InventoryFactory.scanDifferences(current, config);
                long stop = System.currentTimeMillis();
                timeElapsed = stop - start;

                if (inventoryDifference == null) {
                    //no differences, abort
                    log.info("No changes in " + config.getPath());
                    //Fix issue where inventory exists but packages have been cleared.
                    if (!bundler.bundlesAvailable()) {
                        bundleInventory();
                        return timeElapsed + (System.currentTimeMillis() - stop);
                    }
                    return timeElapsed;
                }
                
                //merge found differences into inventory file
                log.info("Differences captured in " + config.getPath());
                state = InventoryCreationState.MERGING_DIFFERENCES;
                timeElapsed += InventoryFactory.applyDifferences(current, config, inventoryDifference);
           } else {
               if (current != null) current.close();
               //no current inventory, so create a fresh one
               InventoryFactory.cleanup(config);
               state = InventoryCreationState.CREATING_INVENTORY;
               timeElapsed = InventoryFactory.createInventory(config);
           }
        } finally {
            if (current != null) current.close();
        }
        
        bundleInventory();
        return timeElapsed;
    }

    private void bundleInventory() {
        if (log.isDebugEnabled()) {
            log.debug("Bunding inventory for: " + config.getPath());
        }
        state = InventoryCreationState.BUNDLING_INVENTORY;
        pruneDifferences();
        bundler.bundleInventory();
        //bundler is observable and will trigger changes to substate.  set
        //null here to indicate that it is done.
        substate = null;
    }
    
    private void pruneDifferences() {
        boolean prune = pruner.prune();
        if (log.isDebugEnabled()) {
            log.debug("Inventory pruning returned " + prune);
        }
    }

    long getItemCount() {
        return fileCount;
    }

    long getSize() {
        return size;
    }
    
    long getLastRuntime() {
        return lastRuntime;
    }
    
    String getState() {
        return (substate == null) ? state.name() : state.name() + "_" + substate.name();
    }
    
    long getNextEstimatedRuntime() {
        if (config.isEnabled()) {
            long remaining = TimeIntervalUnit.MILLISECONDS.convert(config.getInventoryInterval(), config.getInventoryIntervalUnit());
            return ScheduleUtil.getTimeToNextFire(lastRuntime, remaining,
                                        config.getScheduleExpressions());
        } else {
            return -1;
        }
    }
}
