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
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import com.solers.delivery.domain.AllowedHost;
import com.solers.delivery.domain.ConsumerContentSet;
import com.solers.delivery.domain.ContentSet;
import com.solers.delivery.domain.FileFilter;
import com.solers.delivery.event.ContentEvent;
import com.solers.delivery.event.GBSUpdateCompleteEvent;
import com.solers.delivery.event.PendingGBSUpdateEvent;
import com.solers.delivery.event.SynchronizationEvent;
import com.solers.delivery.event.listener.EventListener;
import com.solers.util.unit.TimeIntervalUnit;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class ReplicationTestCase extends BaseDeliveryTestCase {
    
    File supplierContent;
    File consumerContent;
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        supplierContent = new File(System.getProperty("java.io.tmpdir"), "intData/supplier");
        supplierContent.mkdirs();
        
        for (int i=0; i < 100; i++) {
            File f = new File(supplierContent, "content-"+i);
            Writer fileWriter = new FileWriter(f);
            IOUtils.write("hello world "+i, fileWriter);
            fileWriter.close();
        }
        
        consumerContent = new File(System.getProperty("java.io.tmpdir"), "intData/consumer");
        consumerContent.mkdirs();
        
        getAllowedHostService().save(getAllowedHost());
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        FileUtils.deleteDirectory(supplierContent);
        FileUtils.deleteDirectory(consumerContent);
    }

    public void testReplicate() throws Exception {
        CountDownLatch latch = new CountDownLatch(2);
        getEventManager().addListener(new ReplicationCompleteListener(latch));
        
        long id = getContentService().save(createSupplier());
        assertTrue( id > 0 );
        
        id = getContentService().save(createConsumer());
        assertTrue( id > 0 );
        
        latch.await(5, TimeUnit.MINUTES);
        
        File actual = new File(consumerContent, "supplier");
        assertEquals(supplierContent, actual);
    }
    
    private ContentSet createSupplier() {
        ContentSet supplier = new ContentSet();
        supplier.setName("Test-Supplier");
        supplier.setInventoryInterval(1);
        supplier.setInventoryIntervalUnit(TimeIntervalUnit.MINUTES);
        supplier.setSupplier(true);
        supplier.setPath(FilenameUtils.separatorsToUnix(supplierContent.getPath()));
        supplier.setEnabled(true);
        supplier.addAllowedHost(getAllowedHost());
        return supplier;
    }
    
    private ConsumerContentSet createConsumer() {
        ConsumerContentSet consumer = new ConsumerContentSet();
        consumer.setEnabled(true);
        consumer.setName("Test-Consumer");
        consumer.setSupplierAddress("localhost");
        consumer.setSupplierPort(getSupplierPort());
        consumer.setSupplierName("Test-Supplier");
        consumer.setPath(FilenameUtils.separatorsToUnix(consumerContent.getPath()));
        consumer.setInventoryInterval(1);
        consumer.setInventoryIntervalUnit(TimeIntervalUnit.MINUTES);
        
        consumer.setFileDeleteDelay(0);
        consumer.setFileDeleteDelayUnit(TimeIntervalUnit.MINUTES);
        consumer.setFileFilters(new ArrayList<FileFilter>());
        return consumer;
    }
    
    private int getSupplierPort() {
        return Integer.parseInt(System.getProperty("transport.port"));
    }
    
    private void assertEquals(File expected, File actual) throws Exception {
        assertEquals(actual+" does not exist", expected.exists(), actual.exists());
        assertEquals(actual+" is not a file", expected.isFile(), actual.isFile());
        assertEquals(actual+" is not a directory", expected.isDirectory(), actual.isDirectory());
        assertEquals(actual+" does not have the same name as "+expected, expected.getName(), actual.getName());
        assertEquals(expected.length(), actual.length());
        
        if (expected.isDirectory()) {
            File [] expectedChildren = expected.listFiles();
            File [] actualChildren = expected.listFiles();
            
            assertEquals(expectedChildren.length, actualChildren.length);
            
            for (int i=0; i < expectedChildren.length; i++) {
                assertEquals(expectedChildren[i], actualChildren[i]);
            }
        } else {
            assertEquals(expected.lastModified(), actual.lastModified());
            assertContentsEqual(expected, actual);
        }
    }
    
    private void assertContentsEqual(File expected, File actual) throws Exception {
        InputStream expectedStream = new FileInputStream(expected);
        InputStream actualStream = new FileInputStream(actual);
        try {
            assertTrue(IOUtils.contentEquals(expectedStream, actualStream));
        } finally {
            IOUtils.closeQuietly(expectedStream);
            IOUtils.closeQuietly(actualStream);
        }
    }
    
    private AllowedHost getAllowedHost() {
        return new AllowedHost("alias", "cs-test");
    }
    
    private class ReplicationCompleteListener implements EventListener {
        private final CountDownLatch latch;
        
        public ReplicationCompleteListener(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void consumerSynchronizationCompleted(SynchronizationEvent event) {
            latch.countDown();
        }
        
        @Override
        public void supplierSynchronizationCompleted(SynchronizationEvent event) {
            latch.countDown();
        }

        @Override
        public void consumerSynchronizationStarted(SynchronizationEvent event) {
            
        }

        @Override
        public void gbsUpdateComplete(GBSUpdateCompleteEvent event) {
            
        }

        @Override
        public void received(ContentEvent event) {
            
        }

        @Override
        public void received(PendingGBSUpdateEvent event) {
            
        }

        @Override
        public void supplied(ContentEvent event) {
            
        }

        @Override
        public void supplierSynchronizationStarted(SynchronizationEvent event) {
            
        }
    }
}
