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

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;
import org.springframework.beans.factory.annotation.Configurable;

import com.solers.delivery.domain.ConsumerContentSet;
import com.solers.delivery.inventory.InventoryFactory;
import com.solers.delivery.transport.http.client.Transfer;
import com.solers.delivery.transport.http.client.TransferInventory;
import com.solers.delivery.transport.http.client.TransferService;

/**
 * InventoryRetriever is used to retrieve inventories. It is used as part of the SynchronizationTask to backup the existing inventory, request a new inventory,
 * and the unpackage that inventory to the correct location.
 * 
 * @author JGimourginas
 */
@Configurable("inventoryRetriever")
public class InventoryRetriever extends Observable {

    private static final Logger log = Logger.getLogger(InventoryRetriever.class);
    private static final boolean IS_DEBUG_ENABLED = log.isDebugEnabled();
    private static final int BUFFER_SIZE = 1024 * 1024;

    private int numBackups;

    public void setNumBackups(int numBackups) {
        this.numBackups = numBackups;
    }
 
    /**
     * Method to retrieve and extract the Inventory from the Supplier that manages the content set with which the SynchronizationTask must interact. If a
     * packaged inventory exists, the method will back it up. The new packaged Inventory is then placed in the same directory. Its contents are extracted to the
     * open inventory location for this consumer. Unlike the packaged inventory, no backup is made of the open inventory files. The boolean return value is in
     * place to prevent further execution if Inventory retrieval/extraction fails.
     * 
     * @return true if Inventory retrieved, false otherwise
     */
    public InventoryResult getNewInventory(ConsumerContentSet contentSet, long timestamp, TransferService transferService, String syncId) {
        String consumerContentSetName = contentSet.getName();
        String supplierContentSetName = contentSet.getSupplierName();
        if (IS_DEBUG_ENABLED)
            log.debug("Getting new inventory for " + consumerContentSetName);
        String packagedInventoryPath = InventoryFactory.getPackage(contentSet).getAbsolutePath();
        String openInventoryDirPath = InventoryFactory.getOpenLocation(contentSet).getAbsolutePath();
        // need to backup existing before overwriting with new inventory
        backupExistingPackagedInventory(packagedInventoryPath);
        // create transfer for inventory - Transport will stream directly to file system location, which is why backup
        // must be done first
        notifyObservers(State.REQUESTING);
        TransferInventory transfer = createInventoryTransfer(packagedInventoryPath, supplierContentSetName, timestamp, syncId);
        if (IS_DEBUG_ENABLED)
            log.debug("Making inventory request for " + consumerContentSetName);
        transfer = (TransferInventory)transferService.process(transfer);
        if (IS_DEBUG_ENABLED)
            log.debug("Got response for inventory request for " + consumerContentSetName);
        
        if (transfer == null) {
            return new InventoryResult(false, Transfer.Status.FAILED_TRANSFER);
        }
        
        if (transfer.failed()) {
            return new InventoryResult(false, transfer.getStatus());
        }
        
        if (transfer.isNotModified()) {
            log.info("No changes in inventory for " + consumerContentSetName);
            return new InventoryResult(true, transfer.getStatus());
        }
        
        File packagedInventory = new File(packagedInventoryPath);
        File openInventoryDirectory = new File(openInventoryDirPath);
        
        // if this directory does not exist, make it
        openInventoryDirectory.mkdirs();
        
        //Extract the inventory data sent from the Supplier
        notifyObservers(State.UNARCHIVING);
        List<File> entries = Collections.emptyList();
        try {
            entries = extractNewInventory(packagedInventory, openInventoryDirectory);
            //Fail if there were no entries in the bundle- an error condition.
            if (entries.size() == 0) {
                log.error("There were no entries in the provided bundle.");
                return new InventoryResult(false, transfer.getStatus());
            }
            
            //A single entry may be a complete inventory
            if (entries.size() == 1) {
                if (entries.get(0).getName().endsWith(InventoryFactory.INDEX_EXTENSION)) {
                    //we don't need to do any more.  InventoryFactory scans for the latest
                    //inventory and deletes stale entries.
                    return new InventoryResult(true, transfer.getStatus());
                }
            }
            
            //We have diffs, not an inventory, so apply them
            log.info(String.format("Merging %d differences into inventory for %s", entries.size(), consumerContentSetName));
            notifyObservers(State.MERGING);
            long retval = InventoryFactory.applyDifferences(
                contentSet,
                entries.toArray(new File[entries.size()])
            );
            return new InventoryResult((retval > -1), transfer.getStatus());
        } catch (IOException ioe) {
            log.error("Failed to extract new inventory.", ioe);
            return new InventoryResult(false, transfer.getStatus());
        } finally {
            //Clean up unused difference files.
            notifyObservers(State.CLEANUP);
            File[] diffs = openInventoryDirectory.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isFile() && pathname.getName().endsWith(InventoryFactory.DIFF_EXTENSION);
                }
            });
            for (File diff : diffs) {
                boolean success = diff.delete();
                if (!success) diff.deleteOnExit();
            }
        }
    }

    private List<File> extractNewInventory(File packagedInventory, File openInventoryDirectory) throws IOException {
        List<File> entries = new ArrayList<File>();
        FileInputStream in = null;
        FileOutputStream fos = null;
        TarInputStream tIn = null;
        try {
            in = new FileInputStream(packagedInventory);
            tIn = new TarInputStream(new GZIPInputStream(in));
            TarEntry entry = null;
            while ((entry = tIn.getNextEntry()) != null) {
                File next = new File(openInventoryDirectory, entry.getName());
                entries.add(next);
                fos = new FileOutputStream(next);
                byte[] buffer = new byte[BUFFER_SIZE];
                int length = 0;
                while ((length = tIn.read(buffer)) >= 0) {
                    fos.write(buffer, 0, length);
                }
                fos.close();
                fos = null;
            }
            if (IS_DEBUG_ENABLED)
                log.debug("Inventory successfully extracted to " + openInventoryDirectory);
            return entries;
        } catch (FileNotFoundException e) {
            log.error("Could not find Packaged Inventory file: " + e.getMessage());
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(fos);
            IOUtils.closeQuietly(tIn);
        }
        
        return entries;
    }

    /**
     * Creats a backup of the existing packaged inventory for tracking.
     * 
     * @param path
     *            path to existing packaged inventory
     */
    private void backupExistingPackagedInventory(String path) {
        notifyObservers(State.BACKUP);
        
        if (IS_DEBUG_ENABLED)
            log.debug("Backing up inventory: " + path);
        File packagedInventoryFile = new File(path);
        if (packagedInventoryFile.exists()) {
            File backup = new File(packagedInventoryFile.getPath() + "_" + System.currentTimeMillis() + ".bak");
            packagedInventoryFile.renameTo(backup);
            File[] existingFiles = packagedInventoryFile.getParentFile().listFiles();
            if (existingFiles.length > numBackups) {
                int numToDelete = existingFiles.length - numBackups;
                for (int i = 0; i < numToDelete; i++) {
                    // listing will show the oldest backup first, so remove starting with the oldest
                    existingFiles[i].delete();
                }
            }
        }
    }

    /**
     * Creates a Transfer object for an Inventory request. The returned Transfer contains all the necessary fields need by the TransferService to obtain the
     * Inventory and place it on the file system.
     * 
     * @return Transfer object that should be used to make an Inventory request.
     */
    private TransferInventory createInventoryTransfer(String packagedInventoryPath, String supplierContentSetName, long timestamp, String syncId) {
        return new TransferInventory(packagedInventoryPath, supplierContentSetName, timestamp, syncId);
    }
    
    private enum State {
        BACKUP,
        REQUESTING,
        UNARCHIVING,
        MERGING,
        CLEANUP
    }
}
