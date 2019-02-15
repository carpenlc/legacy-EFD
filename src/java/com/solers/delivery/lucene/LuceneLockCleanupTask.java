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
package com.solers.delivery.lucene;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;

/**
 * Cleanup any stale locks left behind by lucene.
 * 
 * This should be run only once and before the system starts synchronizations
 * 
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class LuceneLockCleanupTask implements Runnable {
    
    private static final Logger log = Logger.getLogger(LuceneLockCleanupTask.class);
    
    private final LuceneHelper helper;
    
    public LuceneLockCleanupTask(LuceneHelper helper) {
        this.helper = helper;
    }
    
    /**
     * Unlock any index files that were not properly cleaned up.  Index files
     * might not get cleaned up if they were in use and the JVM crashed.
     * 
     * This should be run before the application begins using lucene
     */
    public void run() {
        log.info("Cleaning up stale lucene locks...");
        for (File contentSetFile : list(helper.getEventDirectory())) {
            for (File indexFile : list(contentSetFile)) {
                Directory index = LuceneHelper.getIndex(indexFile);
                try {
                    if (IndexWriter.isLocked(index)) {
                        log.info("Unlocking: "+index);
                        IndexWriter.unlock(index);
                    }
                } catch (IOException ex) {
                    log.error("Error unlocking index: "+index);
                }
            }
        }
    }
    
    private File [] list(File dir) {
        File [] files = dir.listFiles();
        if (files == null) {
            return new File [] {};
        }
        return files;
    }
}
