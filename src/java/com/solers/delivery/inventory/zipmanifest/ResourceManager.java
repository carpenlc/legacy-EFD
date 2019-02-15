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
package com.solers.delivery.inventory.zipmanifest;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

import com.solers.util.unit.TimeIntervalUnit;

/**
 * This class is responsible for handling the extraction and cleanup of zip manifest
 * data.  It uses java.util.concurrent.Future objects so that multiple requests against
 * the same zip archive won't create multiple temporary extractions.  When a zip
 * manifest is closed, it signals the resource manager.  A creation of a new zip manifest
 * inventory representation will halt any cleanup count down that is underway. 
 */
public class ResourceManager {
    protected static final Logger log = Logger.getLogger(ResourceManager.class);
    
    private static final ExecutorService resourceService = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        }        
    });
    protected static final ConcurrentMap<ZipFileCacheEntry, Future<File>> createCache =
        new ConcurrentHashMap<ZipFileCacheEntry, Future<File>>();
    protected static final ConcurrentMap<ZipFileCacheEntry, CleanupThread> deleteCache =
        new ConcurrentHashMap<ZipFileCacheEntry, CleanupThread>();
    
    public static final String DELETE_DELAY_PROP = "zipmanifest.deleteDelayMillis";
    private static final long defaultDeleteDelay = 1000 * 60 * 2; //2 minutes
    private static final long deleteDelay;
    
    static {
        long l = defaultDeleteDelay;
        try {
            l = Long.parseLong(System.getProperty(DELETE_DELAY_PROP));
        } catch (Exception e) {}
        finally {
            deleteDelay = l;
        }
        log.debug("Delete delay set to " + deleteDelay);
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
               log.debug("Shutdown detected, cleaning up zip manifest data...");
               for (CleanupThread ct : deleteCache.values()) {
                   ct.cleanup();
               }
               for (Future<File> f : createCache.values()) {
                   if (f.isDone()) {
                       try {
                           File temp = f.get();
                           log.debug("Cleaning unclosed inventory " + temp);
                           FileUtils.forceDelete(temp);
                       } catch (Exception e) {
                           log.debug("Received exception during cleanup", e);
                       }
                   }
               }
            } 
        });
    }
    
    /**
     * Submit an archive file with a given root name for extraction.
     * @param zip the archive
     * @param root name of the root to use
     * @return the temporary directory where the file was extracted
     */
    public static File submit(File zip, String root) {
        return submit(new ZipFileCacheEntry(zip), zip, root);
    }
    
    /**
     * Submit an archive file with a given root name for extraction, using a pre-
     * instantiated key for correlation.
     * @param key the key that represents the given file
     * @param zip the archive
     * @param root name of the root to use
     * @return the temporary directory where the file was extracted
     */
    public static File submit(ZipFileCacheEntry key, File zip, String root) {
        Thread t = deleteCache.get(key);
        if (t != null) t.interrupt();
        
        ExpandTask task = new ExpandTask(zip, root);
        Future<File> current = createCache.get(key);
        try {
            if (current != null) return current.get();
            
            Future<File> future = resourceService.submit(task);
            current = createCache.putIfAbsent(key, future);
            
            if (current == null) {
                task.signal();
                return future.get();
            }
            
            future.cancel(true);
            return current.get();
        } catch (ExecutionException ee) {
            throw new RuntimeException(ee);
        } catch (InterruptedException ie) {
            //don't worry
        }
        
        return null;
    }
    
    /**
     * Signal that an archive is no longer needed.
     * @param zip the archive that is not needed
     */
    public static void close(File zip) {
        close(new ZipFileCacheEntry(zip));
    }
    
    /**
     * Signal that an entry is no longer needed.
     * @param key the key representing the archive
     */
    public static void close(ZipFileCacheEntry key) {
        CleanupThread future = new CleanupThread(deleteDelay, key);
        CleanupThread current = deleteCache.putIfAbsent(key, future);
        if (current != null) current.interrupt();
        future.start();        
    }
    
    protected static File getTempDir() throws IOException {
        File tempFile = File.createTempFile("zptmp", "");
        tempFile.delete();
        tempFile.mkdirs();
        return tempFile;
    }
    
    @SuppressWarnings("unchecked")
    protected static void expand(ZipFile zf, File tempDir, String rootName) throws IOException {
        File root = new File(tempDir, rootName);
        root.mkdirs();
        
        Enumeration<? extends ZipEntry> entries = zf.getEntries();
        while (entries.hasMoreElements()) {
            ZipEntry e = entries.nextElement();
            File f = new File(root, e.getName());
            if (!f.exists()) {
                if (e.isDirectory()) f.mkdirs();
                else {
                    File fp = f.getParentFile();
                    if (!fp.exists()) fp.mkdirs();
                    f.createNewFile();
                }
            }
        }
    }
    
    /**
     * Responsible for expanding an archive to a temporary location. 
     */
    protected static class ExpandTask implements Callable<File> {
        private static final Logger log = Logger.getLogger(ExpandTask.class);
        private final File zip;
        private final String rootName;
        private final CountDownLatch signal = new CountDownLatch(1);
        
        public ExpandTask(File zip, String rootName) {
            this.zip = zip;
            this.rootName = rootName;
        }
        
        public void signal() {
            signal.countDown();
        }
        
        @Override
        public File call() throws Exception {
            if (log.isDebugEnabled()) log.debug("Awaiting task release on " + zip);
            try {
                signal.await();
            } catch (InterruptedException ie) {
                log.debug("Expansion task not needed, cancelling.");
                return null;
            }
            
            File temp = getTempDir();
            if (log.isDebugEnabled()) log.debug("Expanding " + new ZipFileCacheEntry(zip) + " to " + temp);
            long start = System.currentTimeMillis();
            expand(new ZipFile(zip), temp, rootName);
            long end = System.currentTimeMillis();
            if (log.isDebugEnabled()) log.debug("Extraction of " + new ZipFileCacheEntry(zip) + " took " + TimeIntervalUnit.format(end - start));
            return temp;
        }
    }
    
    /**
     * Waits for a configurable amount of time.  If interrupted, cleanup is aborted.
     * Otherwise, when the duration has elapsed, the specified cache entry will have
     * its temporary data erased.
     */
    protected static class CleanupThread extends Thread {
        private static final Logger log = Logger.getLogger(CleanupThread.class);
        private final long timeout;
        private final ZipFileCacheEntry key;
        
        CleanupThread(long timeout, ZipFileCacheEntry key) {
            this.timeout = timeout;
            this.key = key;
            setDaemon(true);
        }
        
        @Override
        public void run() {
            log.debug("Waiting to delete temporary data for " + key);
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException ie) {
                log.debug(key + " interrupted by new inventory request.");
                return;
            }
            if (isInterrupted()) {
                log.debug(key + " interrupted by new inventory request.");
                return;
            }
            
            cleanup();
        }
        
        public void cleanup() {
            File tempDir = null;
            
            deleteCache.remove(key);
            try {
                tempDir = createCache.remove(key).get();
            } catch (Exception e) {
                log.warn("There was an error trying to clean up temp data for " + key);
                if (tempDir == null) return;
            }
            
            try {
                log.debug("Deleting temporary data for " + key + " in " + tempDir);
                long start = System.currentTimeMillis();
                FileUtils.deleteDirectory(tempDir);
                long end = System.currentTimeMillis();
                log.debug("Deletion for " + key + " took " + TimeIntervalUnit.format(end - start));
            } catch (Exception ioe) {
                //don't care
            }
        }
    }
}
