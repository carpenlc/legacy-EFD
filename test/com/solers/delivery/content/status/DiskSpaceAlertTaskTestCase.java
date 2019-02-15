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
package com.solers.delivery.content.status;

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.solers.delivery.alerts.AlertService;
import com.solers.delivery.alerts.AlertServiceImpl;
import com.solers.delivery.content.ContentService;
import com.solers.delivery.content.ContentServiceImpl;
import com.solers.delivery.content.consumer.MockConsumerContentSetManager;
import com.solers.delivery.content.supplier.MockSupplierContentSetManager;
import com.solers.delivery.daos.MockDAOFactory;
import com.solers.delivery.domain.ContentSet;
import com.solers.delivery.domain.Alert.AlertType;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class DiskSpaceAlertTaskTestCase {
    
    private MockDAOFactory factory;
    private ContentService contentService;
    private AlertService alertService;
    
    @Before
    public void setUp() {
        factory = new MockDAOFactory();
        contentService = new ContentServiceImpl(factory, new MockSupplierContentSetManager(), new MockConsumerContentSetManager());
        alertService = new AlertServiceImpl(factory);
    }
    
    @Test
    public void testBelowThreshold() {
        factory.getContentSetDAO().makePersistent(newSupplier());
        DiskSpaceAlertTask task = new TestDiskSpaceAlertTask(contentService, alertService, 50.0D, 200, 300, ".");
        
        Assert.assertEquals(0, alertService.list(AlertType.USER, 0, 100).getCount());
        task.run();
        Assert.assertEquals(0, alertService.list(AlertType.USER, 0, 100).getCount());
    }
    
    @Test
    public void testAtThreshold() {
        factory.getContentSetDAO().makePersistent(newSupplier());
        DiskSpaceAlertTask task = new TestDiskSpaceAlertTask(contentService, alertService, 50.0D, 100, 200, ".");
        
        Assert.assertEquals(0, alertService.list(AlertType.USER, 0, 100).getCount());
        task.run();
        Assert.assertEquals(2, alertService.list(AlertType.USER, 0, 100).getCount());
    }
    
    @Test
    public void testAboveThreshold() {
        factory.getContentSetDAO().makePersistent(newSupplier());
        DiskSpaceAlertTask task = new TestDiskSpaceAlertTask(contentService, alertService, 82.0D, 25, 200, ".");
        
        Assert.assertEquals(0, alertService.list(AlertType.USER, 0, 100).getCount());
        task.run();
        Assert.assertEquals(2, alertService.list(AlertType.USER, 0, 100).getCount());
    }
    
    @Test
    public void testRealWorld() {
        factory.getContentSetDAO().makePersistent(newSupplier());
        DiskSpaceAlertTask task = new TestDiskSpaceAlertTask(contentService, alertService, 95.0D, 8479744L, 1252867072L, ".");
        
        Assert.assertEquals(0, alertService.list(AlertType.USER, 0, 100).getCount());
        task.run();
        Assert.assertEquals(2, alertService.list(AlertType.USER, 0, 100).getCount());
    }
    
    private ContentSet newSupplier() {
        ContentSet contentSet = new ContentSet();
        contentSet.setSupplier(true);
        contentSet.setName("supplier");
        contentSet.setPath(".");
        return contentSet;
    }
    
    private static class TestDiskSpaceAlertTask extends DiskSpaceAlertTask {

        private final long freeSpace;
        private final long totalSpace;
        
        public TestDiskSpaceAlertTask(ContentService cs, AlertService as, double warning_threshold, long freeSpace, long totalSpace, String efd_home) {
            super(cs, as, warning_threshold, efd_home);
            this.freeSpace = freeSpace;
            this.totalSpace = totalSpace;
        }

        @Override
        protected long getFreeSpace(File path) {
            return freeSpace;
        }

        @Override
        protected long getTotalSpace(File path) {
            return totalSpace;
        }
        
    }
}
