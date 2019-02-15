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
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.solers.delivery.content.ConsumerContentFilter;
import com.solers.delivery.domain.ConsumerContentSet;
import com.solers.delivery.domain.ContentSet;
import com.solers.delivery.inventory.comparer.Comparer;
import com.solers.delivery.inventory.comparer.UnorderedInventoryComparer;
import com.solers.delivery.inventory.fs.FileSystemInventoryProvider;
import com.solers.delivery.inventory.index.ReaderFactory;
import com.solers.delivery.inventory.index.WriterFactory;
import com.solers.delivery.inventory.index.diff.DifferenceFileEventHandler;
import com.solers.delivery.inventory.index.diff.DifferenceFileReader;
import com.solers.delivery.inventory.index.diff.MergedNode;
import com.solers.delivery.inventory.plugin.InventoryPlugin;
import com.solers.delivery.inventory.plugin.PluginException;
import com.solers.delivery.inventory.plugin.PluginLoadException;
import com.solers.delivery.inventory.plugin.PluginProviderNotFoundException;
import com.solers.delivery.inventory.plugin.provider.ProviderInfo;
import com.solers.util.HashFunction;

public final class InventoryFactory {
    private static final Logger log = Logger.getLogger(InventoryFactory.class);
    
    public static final String INDEX_EXTENSION = ".index";
    public static final String DIFF_EXTENSION = ".diff";
    private static final String TEMP_EXTENSION = ".tmp";
    
    //give default value because this cannot be null.
    private static InventoryPathCreator path = new InventoryPathCreator(".");
    
    private InventoryFactory() {
        //do not allow default constructor
    }
    
    public static void setSiteDirectory(String siteDirectory) {
        path = new InventoryPathCreator(siteDirectory);
    }
    
    /**
     * Clean up a content set
     * @param contentset
     */
    public static synchronized void cleanup(ContentSet contentset) {
        try {
            FileUtils.deleteDirectory(path.getContentSetLocation(contentset));
        } catch (IOException ex) {
            log.warn("Error cleaning up content set: "+contentset.getName(), ex);
        }
    }

    
    public static File getPackageLocation(ContentSet contentset) {
        return path.getPackagedInventory(contentset);
    }
    
    public static File getPackage(ConsumerContentSet contentset) {
        return path.getPackagedInventoryFile(contentset);
    }
    
    public static File getOpenLocation(ContentSet contentset) {
        return path.getOpenInventory(contentset);
    }
    
    /**
     * Find the most recent inventory file for the given location based on EFD's
     * business logic.
     * @param inventoryLocation
     */
    protected static File getInventoryFile(ContentSet contentset) {
        File inventoryLocation = path.getOpenInventory(contentset);
        File[] candidates = inventoryLocation.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(INDEX_EXTENSION);
            }
        });
        
        if (candidates == null) return null;
        
        Arrays.sort(candidates);
        for (int i = 0; i < candidates.length; ++i) {
            //if we are at the end (latest), return it.
            if (i == candidates.length - 1) return candidates[i];
            //attempt to prune older inventory files
            boolean success = candidates[i].delete();
            if (!success) candidates[i].deleteOnExit();
            if (log.isDebugEnabled()) {
                log.debug("Deleting stale inventory file " + candidates[i].getAbsolutePath() + ": " + success);
            }
        }
        return null;
    }
    
    public static java.util.Date getInventoryFileTimestamp(ContentSet contentSet) {
        File file = getInventoryFile(contentSet);
        if (file != null) {
            return new java.util.Date(file.lastModified());
        }
        return null;
    }
    
    /**
     * Create an inventory and return the time it took to complete the operation.
     * @param location The location where the index file will be generated.
     * @param contentSetPath The path that will be indexed.
     * @return the duration of the operation
     * @throws IOException
     */
    public static long createInventory(ContentSet contentset) throws IOException, PluginException {
        long start = System.currentTimeMillis();
        File inventoryDir = path.getOpenInventory(contentset);
        Inventory backingInventory = getBackingInventory(contentset);
        Writer writer = WriterFactory.newInstance(backingInventory.root(), HashFunction.FNV.name(), start);
        inventoryDir.mkdirs();
        File target = new File(inventoryDir, "" + start + INDEX_EXTENSION);
        File index = new File(inventoryDir, target.getName() + TEMP_EXTENSION);
        writer.write(index);
        long stop = System.currentTimeMillis();
        index.renameTo(target);
        return stop - start;
    }
    
    /**
     * Return the root node of the inventory located by the specified path.
     * Callers of this method must obey the contract that if you retrieve
     * an Inventory resource, you must close it to free the backing data on
     * the file system.
     * @param pathToOpenInventory location of open inventory
     * @return most recent inventory, or null if it cannot be read
     */
    public static Inventory getInventory(ContentSet contentset) {
        return getInventory(contentset, true);
    }
    
    public static Inventory getInventory(ContentSet contentset, boolean useFilter) {
        File index = getInventoryFile(contentset);
        if (index == null) return null;
        try {
            Inventory i = ReaderFactory.newInstance(index);
            if (useFilter && contentset instanceof ConsumerContentSet) {
                ConsumerContentSet ccs = (ConsumerContentSet) contentset;
                i.useFilter(new ConsumerContentFilter(ccs));
            }
            return i;
        } catch (IOException ioe) {
            log.warn("Encountered error while getting inventory", ioe);
        } catch (IllegalArgumentException iae) {
            //inventory could not be read - file may not exist
            log.debug("Encountered exception while getting inventory", iae);
        }
        return null;
    }
    
    /**
     * Returns the "backing inventory" for a Consumer, given the desired root node.
     * If the consumer is configured to use a virtual manifest, it will return that.
     * Otherwise, it will use the desired root node to calculate the filesystem inventory
     * for the specified consumer.
     * @param cs the consumer content set descriptor
     * @param rootName the desired/expected root node
     * @return an inventory for the consumer
     * @throws PluginException
     */
    public static Inventory getBackingInventory(ConsumerContentSet cs, String rootName) throws PluginException {
        String virtualManifest = cs.getVirtualManifest();
        
        //if there is a virtual manifest, attempt to load it, failing back on file:// URI.
        if (virtualManifest != null && !"".equals(virtualManifest)) {
            log.debug("Loading virtual inventory for " + cs.getName());
            return getVirtualInventory(cs);
        }
        
        //attempt to load the virtual manifest
        return getBackingInventory(new File(cs.getPath()), rootName);
    }
    
    /**
     * Return an instance to a FileSystemInventory
     * 
     * @param rootDirectory File system root directory
     * @param rootName Root directory name
     * @return FileSystemInventory
     */
    protected static Inventory getBackingInventory(File rootDirectory, String rootName) {
        try {
            return getBackingInventory(new File(rootDirectory, rootName).toURI());
        } catch (PluginProviderNotFoundException ppnfe) {
            if (isFileSystemInventoryLoaded()) {
                log.error("The specified file is not a valid inventory.", ppnfe);
            } else {
                log.error("File system provider was not found!", ppnfe);
            }
            return null;
        }
    }
    
    public static Inventory getBackingInventory(URI inventoryResource) throws PluginProviderNotFoundException {
        return getBackingInventory(inventoryResource, new HashMap<String, Object>(0));
    }
    
    public static Inventory getBackingInventory(URI inventoryResource, Map<String, Object> parameters) throws PluginProviderNotFoundException {
        try {
            return InventoryPlugin.newInstance(inventoryResource, parameters);
        } catch (PluginProviderNotFoundException pe) {
            throw pe;
        } catch (PluginException pe) {
            log.error("Unexpected exception during inventory provider instantiation.", pe);
        }
        return null;
    }
    
    public static Inventory getVirtualInventory(ConsumerContentSet cs) throws PluginException {
        return findBackingInventory(cs, cs.getVirtualManifest());
    }
    
    public static Inventory getBackingInventory(ContentSet cs) throws PluginException {
        return findBackingInventory(cs, cs.getPath());
    }
    
    protected static Inventory findBackingInventory(ContentSet cs, String path) throws PluginException {
        Map<String, Object> parameters = cs.getResourceParametersAsMap();
        try {
            return getBackingInventory(new URI(path), parameters);
        } catch (PluginProviderNotFoundException ppnfe) {
            log.debug("No provider found for " + cs.getName() + " resource URI, forcing to file.", ppnfe);
        } catch (URISyntaxException use) {
            log.debug("URI syntax exception for " + cs.getName() + ", forcing to file.", use);
        }
        
        try {
            return getBackingInventory(new File(path).toURI(), parameters);
        } catch (PluginProviderNotFoundException ppnfe) {
            log.error("No inventory provider found for " + cs.getName());
            throw new PluginLoadException("Could not construct valid URI for inventory.", ppnfe);
        }
    }
    
    /**
     * Call scanDifferences using the most recent inventory found inside inventoryLocation 
     */
    public static File scanDifferences(ContentSet contentset) {
        Inventory i = null;
        try {
            //don't use filter when scanning for differences
            i = getInventory(contentset, false);
            return scanDifferences(i, contentset);
        } finally {
            if (i != null) i.close();
        }
    }
    
    /**
     * Scan an inventory for differences, recording them to disk if available.
     * When there are differences, this method returns the resultant difference
     * file.  If there are no differences, <i>null</i> is returned back to the
     * caller.
     * @param existing The inventory to use as the basis for differences
     * @param inventoryLocation Location where differences will be saved.
     * @param contentSetPath Path to the actual tree for the content set.
     * @return the difference file or null
     */
    public static File scanDifferences(Inventory existing, ContentSet contentset) {
        String contentSetPath = contentset.getPath();
        File inventoryLocation = path.getOpenInventory(contentset);
        //String rootName = existing.getRootName();
        String rootName = new File(contentSetPath).getName();
        long sourceTime = (Long) existing.getProperty(TimeProperties.TIMESTAMP);
        Inventory desired = getBackingInventory(new File(contentSetPath).getParentFile(), rootName);
        Comparer comparer = new UnorderedInventoryComparer(false, false);
        long timestamp = System.currentTimeMillis();
        String hash = HashFunction.FNV.name();
        if (existing.properties().contains(HashProperties.ALGORITHM)) {
            hash = (String) existing.getProperty(HashProperties.ALGORITHM);
        }
        File targetName = new File(inventoryLocation, "" + sourceTime + DIFF_EXTENSION);
        File diffFile = new File(inventoryLocation, targetName.getName() + TEMP_EXTENSION);
        DifferenceFileEventHandler diffHandler = new DifferenceFileEventHandler(diffFile, hash, timestamp);
        
        try {
            comparer.compare(desired, existing, diffHandler);
            if (diffHandler.getDifferenceCount() > 0) {
                diffFile.renameTo(targetName);
                return targetName;
            }
            diffFile.delete();
            return null;
        } finally {
            //don't close existing because we didn't create it
            desired.close();
        }
    }
    
    /**
     * Calculate statistics of available differences - which files are available, as
     * well as how many and the amount of time that they span.
     * @param inventoryLocation the location where inventory and differences are saved
     * @return the statistics for the given files
     */
    public static Stats availableDifferences(ContentSet contentset) {
        File inventoryLocation = path.getOpenInventory(contentset);
        File[] differences = inventoryLocation.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(DIFF_EXTENSION);
            }
        });
        return new Stats(differences);
    }
    
    /**
     * Applies a difference file or series of difference files to an inventory,
     * rewriting the inventory file itself.  This method will take the hash
     * function specified in the most recent difference file.
     * @param inventoryLocation Path to open inventory directory
     * @param differenceFiles One or more differences to apply to this inventory
     * @return the time elapsed during processing, or -1 if applying differences failed
     */
    public static long applyDifferences(ContentSet contentset, File... differenceFiles) throws IOException {
        Inventory existing = null;
        try {
            //don't use filter when applying differences
            existing = getInventory(contentset, false);
            if (existing == null) throw new IOException("No inventory found, but attempt to apply differences was made. inv= " + contentset.getName());
            return applyDifferences(existing, contentset, differenceFiles);
        } finally {
            if (existing != null) existing.close();
        }
    }
    
    public static long applyDifferences(Inventory existing, ContentSet contentset, File... differenceFiles) throws IOException {
        List<DifferenceInventory> differences = Collections.emptyList();
        try {
            differences = new ArrayList<DifferenceInventory>(differenceFiles.length);
            for (File f : differenceFiles) {
                differences.add(new DifferenceFileReader(f));
            }
            Collections.sort(differences);
    
            DifferenceInventory mostRecentDifference = differences.get(differences.size() - 1);
            long timestamp = (Long) mostRecentDifference.getProperty(TimeProperties.TIMESTAMP);
            String hash = (String) mostRecentDifference.getProperty(HashProperties.ALGORITHM);
            
            File inventoryLocation = path.getOpenInventory(contentset);
            File targetName = new File(inventoryLocation, "" + timestamp + INDEX_EXTENSION);
            File newInventory = new File(inventoryLocation, targetName.getName() + TEMP_EXTENSION);
            
            if (targetName.exists()) {
                throw new IOException("Inventory for the LKG " + timestamp + " already exists.");
            }
        
            long start = System.currentTimeMillis();

            Writer w = WriterFactory.newInstance(new MergedNode(existing.root(), differences), hash, timestamp);
            w.write(newInventory);
            
            if (!newInventory.renameTo(targetName)) {
                throw new IOException("Rename " + newInventory.getName() + " to " + targetName.getName() + " in " + inventoryLocation + " failed");
            }
            
            long stop = System.currentTimeMillis();
            return stop - start;
        } finally {
            //don't close existing because we didn't open it
            for (Inventory i : differences) {
                i.close();
            }
        }
    }
    
    public static class Stats {
        private final long duration;
        private final File[] files;
        public Stats(File[] files) {
            this.files = Arrays.copyOf(files, files.length);
            Arrays.sort(this.files);
            this.duration = calcTimespan();
        }
        
        private long calcTimespan() {
            if (files.length == 0) return 0;
            File last = files[files.length - 1];
            File first = files[0];
            long end = Long.parseLong(last.getName().substring(0, last.getName().indexOf('.')));
            long begin = Long.parseLong(first.getName().substring(0, first.getName().indexOf('.')));
            return end - begin;
        }
        
        public File[] listFiles() {
            return Arrays.copyOf(files, files.length);
        }
        
        public int count() {
            return files.length;
        }
        
        public long duration() {
            return duration;
        }
    }
    
    public static boolean isFileSystemInventoryLoaded() {
        final ProviderInfo expected = new FileSystemInventoryProvider().getProviderInfo();
        final Collection<ProviderInfo> availableProviders = InventoryPlugin.getProviderInfo();
        return availableProviders.contains(expected);
    }
}
