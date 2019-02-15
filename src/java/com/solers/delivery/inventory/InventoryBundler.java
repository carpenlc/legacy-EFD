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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Observable;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarOutputStream;
import org.springframework.beans.factory.annotation.Configurable;

import com.solers.delivery.domain.ContentSet;

/**
 * This class is responsible for packaging the files that make up an Inventory and placing the packaged files in a specific location. The packaging uses BZip2
 * Compression. The location of the open Inventory and packaged Inventory root directories is provided through spring. Then, the specific sub-directory is
 * passed in to the bundle method. The sub directory is appended to the root directory to support separate storage for each content set.
 * 
 * @author JGimourginas
 */
@Configurable("inventoryBundler")
public class InventoryBundler extends Observable {
    
    public static final String EXTENSION = ".tar.gz";
    public static final String BACKUP_EXTENSION = ".bak";
    public static final String TEMP_EXTENSION = ".tmp";
    
    private static final Logger log = Logger.getLogger(InventoryBundler.class);
    private static final boolean IS_DEBUG_ENABLED = log.isDebugEnabled();
    private static final int BUFFER_SIZE = 8 * 1024;

    private static class ExtensionMatcher implements FileFilter {
        private final String extension;
        protected ExtensionMatcher(String extension) {
            this.extension = extension;
        }
        @Override
        public boolean accept(File pathname) {
            return pathname.isFile() && pathname.getName().endsWith(extension);
        }
    }
    
    private static final FileFilter currentFiles = new ExtensionMatcher(EXTENSION);
    private static final FileFilter backupFiles = new ExtensionMatcher(BACKUP_EXTENSION);
    private static final FileFilter indexFiles = new ExtensionMatcher(InventoryFactory.INDEX_EXTENSION);
    private static final FileFilter diffFiles = new ExtensionMatcher(InventoryFactory.DIFF_EXTENSION);
    private static final FileFilter tempFiles = new ExtensionMatcher(TEMP_EXTENSION);
    
    private File openInventoryDirectory = null;
    private File packagedInventoryDir = null;
    private String contentSetName;
    private int numInventoryBackups;
    
    public InventoryBundler(ContentSet config) {
        this.openInventoryDirectory = InventoryFactory.getOpenLocation(config);
        this.packagedInventoryDir = InventoryFactory.getPackageLocation(config);
        this.contentSetName = config.getName();
    }
    
    public void setNumInventoryBackups(int numBackups) {
        this.numInventoryBackups = numBackups;
    }

    /**
     * Test to see if bundles are available.
     */
    public boolean bundlesAvailable() {
        if (packagedInventoryDir.exists()) {
            File[] files = packagedInventoryDir.listFiles();
            if (files != null && files.length > 0) {
                for (File f : files) {
                    if (f.getName().endsWith(EXTENSION)) return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Packages an open inventory and creates a packaged inventory. Uses bzip2 compression when bundling the files that comprise an inventory. If a packaged
     * inventory already exists, it is backed up to a file that includes the timestamp of the backup.
     */
    public void bundleInventory() {
        if (IS_DEBUG_ENABLED)
            log.debug("Bundling Inventory for: " + contentSetName);
        if (!packagedInventoryDir.exists()) {
            packagedInventoryDir.mkdirs();
        }
        
        //Determine if we should proceed with bundling.
        File[] indices = openInventoryDirectory.listFiles(indexFiles);
        Arrays.sort(indices);
        if (indices.length == 0) {
            log.debug("No inventory to bundle, aborting.");
            return;
        }
        File latestIndex = indices[indices.length - 1];
        File[] bundles = packagedInventoryDir.listFiles(currentFiles);
        Arrays.sort(bundles);
        if (bundles.length > 0) {
            File latestBundle = bundles[bundles.length - 1];
            String indx = getNamePart(latestIndex);
            String bndl = getNamePart(latestBundle);
            if (bndl.equals(contentSetName + "_" + indx)) {
                log.debug("Inventory has not changed, will not rebundle.");
                return;
            }
        }
        
        notifyObservers(State.BACKUP);
        //Back up the existing packaged set.
        backup();
        
        notifyObservers(State.PACKAGING);
        boolean success = true;
        //package inventory file
        try {
            createTarGzip(
                new File(packagedInventoryDir, contentSetName + "_" + getNamePart(latestIndex) + EXTENSION + TEMP_EXTENSION),
                latestIndex
            );
        } catch (IOException ioe) {
            log.error("Failed to bundle inventory " + latestIndex, ioe);
            success = false;
        }
        
        //package difference files
        File[] diffs = openInventoryDirectory.listFiles(diffFiles);
        Arrays.sort(diffs);
        for (int i = 0; i < diffs.length; ++i) {
            if (!success) break;
            int count = diffs.length - i;
            String name = getNamePart(diffs[i]);
            File nextFile = new File(packagedInventoryDir, contentSetName + "_" + name + EXTENSION + TEMP_EXTENSION);
            File[] targets = (i == 0) ? diffs : new File[count];
            if (i > 0) {
                System.arraycopy(diffs, i, targets, 0, count);
            }
                        
            try {
                createTarGzip(nextFile, targets);
            } catch (IOException ioe) {
                log.error("Failed to bundle difference " + nextFile, ioe);
                success = false;
                break;
            }
        }
        
        notifyObservers(State.CLEANUP);
        //Delete the old data we do not need anymore if successful.
        if (success) {
            for (File f : bundles) {
                f.delete();
            }
        }
        
        //if not successful, delete the detritus.  Otherwise rename temp files to inventory files.
        File[] temps = packagedInventoryDir.listFiles(tempFiles);
        for (File f : temps) {
            if (!success) {
                f.delete();
            } else {
                //chop off temporary extension
                File newName = new File(f.getAbsolutePath().substring(0, f.getAbsolutePath().indexOf(TEMP_EXTENSION)));
                f.renameTo(newName);
            }
        }
    }

    private String getNamePart(File f) {
        return f.getName().substring(0, f.getName().indexOf('.'));
    }
    
    /**
     * Method that updates the packaged inventory using the files of the current inventory for a particular sub directory. The openInventoryDirectory is
     * appended with the subDir parameter. The files under that subDir are assumed to be the files needed for the inventory. The files are bundled and compressed
     * into a single file, which is place in the packagedInventoryFile under the subDir specified.
     */
    private void createTarGzip(File archive, File... contents) throws IOException {
        TarOutputStream tOut = null;
        if (IS_DEBUG_ENABLED)
            log.debug("Creating archive: " + archive);
        FileOutputStream fOut = null;
        BufferedOutputStream bOut = null;
        OutputStream gzOut = null;
        try {
            fOut = new FileOutputStream(archive);
            bOut = new BufferedOutputStream(fOut);
            gzOut = createGZIPOutputStream(bOut);
            tOut = new TarOutputStream(gzOut);
            tOut.setLongFileMode(TarOutputStream.LONGFILE_GNU);
            for (File file : contents) {
                TarEntry te = new TarEntry(file.getName());
                te.setModTime(file.lastModified());
                if (!file.isDirectory()) {
                    te.setSize(file.length());
                    te.setMode(TarEntry.DEFAULT_FILE_MODE);
                } else {
                    te.setMode(TarEntry.DEFAULT_DIR_MODE);
                }
                tOut.putNextEntry(te);
                if (!file.isDirectory()) {
                    // KRJ 2016-08-26: Converted fIn creation to try with resources,
                    // based on an HP Fortify recommendation
                    
                    try (FileInputStream fIn = new FileInputStream(file)) {
                        byte[] buffer = new byte[BUFFER_SIZE];
                        int count = 0;
                        do {
                            tOut.write(buffer, 0, count);
                            count = fIn.read(buffer, 0, buffer.length);
                        } while (count != -1);
                        IOUtils.closeQuietly(fIn);
                    }
                    catch (IOException i) {
                        log.error("Could not create FileInputStream object", i);
                    }
                }
                tOut.closeEntry();
            }
            if (IS_DEBUG_ENABLED)
                log.debug("Building tar " + archive + " complete.");
        } finally {
            IOUtils.closeQuietly(tOut);
            IOUtils.closeQuietly(gzOut);
            IOUtils.closeQuietly(bOut);
            IOUtils.closeQuietly(fOut);
        }
    }

    private OutputStream createGZIPOutputStream(final OutputStream outputStream) throws IOException {
        return new GZIPOutputStream(outputStream);
    }

    /**
     * Backs up an existing inventory and appends a timestamp to the old version.
     */
    private void backup() {
        if (IS_DEBUG_ENABLED)
            log.debug("Backing up inventory: " + packagedInventoryDir.getPath());
        // first figure out what to back up.
        File backup = new File(packagedInventoryDir, contentSetName + "_" + System.currentTimeMillis() + BACKUP_EXTENSION);
        File[] currentSet = packagedInventoryDir.listFiles(currentFiles);
        if (currentSet.length == 0) {
            log.debug("No files to back up, aborting.");
            return;
        }
        
        //tar/gzip the set of files that we need to back up
        try {
            createTarGzip(backup, currentSet);
        } catch (IOException ioe) {
            log.error("Error building backup in " + packagedInventoryDir, ioe);
        }
        
        //remove backups exceeding the allowed count
        File[] existingFiles = packagedInventoryDir.listFiles(backupFiles);
        Arrays.sort(existingFiles);
        if (existingFiles.length > numInventoryBackups) {
            int numToDelete = existingFiles.length - numInventoryBackups;
            for (int i = 0; i < numToDelete; i++) {
                // listing will show the oldest backup first, so remove starting with the oldest
                existingFiles[i].delete();
            }
        }
    }
    
    private enum State {
        BACKUP,
        PACKAGING,
        CLEANUP
    }
}
