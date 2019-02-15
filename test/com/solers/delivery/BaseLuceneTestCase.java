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
package com.solers.delivery;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;

import com.solers.delivery.event.ContentEvent;
import com.solers.delivery.event.SynchronizationEvent;
import com.solers.delivery.lucene.LuceneHelper;
import com.solers.delivery.reports.history.LuceneSynchronizationHistory;
import com.solers.delivery.reports.history.ReportDetail;
import com.solers.delivery.reports.history.Synchronization;
import com.solers.delivery.reports.history.SynchronizationHistory;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public abstract class BaseLuceneTestCase extends TestCase {
    
    protected File siteDirectory;
    protected LuceneHelper helper;
    protected SynchronizationHistory history;
    
    public void setUp() {
        siteDirectory = new File(getClass().getSimpleName());
        helper = new LuceneHelper(siteDirectory);
        history = new LuceneSynchronizationHistory(helper);
    }
    
    public void tearDown() {
        try {
            FileUtils.deleteDirectory(siteDirectory);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    protected void assertEquals(SynchronizationEvent event, Synchronization sync) {
        assertNotNull(event);
        assertNotNull(sync);
        assertEquals(event.getId(), sync.getId());
        assertEquals(event.getBytesAdded(), sync.getBytesAdded());
        assertEquals(event.getBytesUpdated(), sync.getBytesUpdated());
        assertEquals(event.getBytesDeleted(), sync.getBytesDeleted());
        assertEquals(event.getFilesAdded(), sync.getAdds());
        assertEquals(event.getFilesUpdated(), sync.getUpdates());
        assertEquals(event.getFilesDeleted(), sync.getDeletes());
        assertEquals(event.getElapsedTime(), sync.getElapsedTime());
    }
    
    protected void assertEquals(ContentEvent event, ReportDetail detail) {
        assertNotNull(event);
        assertNotNull(detail);
        
        assertEquals(event.getBytesRequested(), detail.getSize());
        assertEquals(event.getBytesManipulated(), detail.getTransferred());
        assertEquals(event.getBytesRequested() - event.getBytesManipulated(), event.getBytesFailed());
        assertEquals(event.getAction(), detail.getAction());
        assertEquals(event.getPath(), detail.getPath());
        assertEquals(event.getResult(), detail.getStatus());
    }
    
    /**
     * Since the listeners generally handle things with a thread, we need
     * to wait a bit for them to finish processing before asserting results
     */
    protected void waitForListener() {
        try {
            Thread.sleep(500);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
