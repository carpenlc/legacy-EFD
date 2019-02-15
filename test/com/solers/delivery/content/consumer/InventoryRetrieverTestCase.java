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
import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;

import com.solers.delivery.domain.ConsumerContentSet;
import com.solers.delivery.inventory.InventoryFactory;
import com.solers.delivery.transport.http.TransferStatus;
import com.solers.delivery.transport.http.client.Transfer;
import com.solers.delivery.transport.http.client.TransferInventory;
import com.solers.delivery.transport.http.client.TransferService;
import com.solers.delivery.transport.http.client.Transfer.Status;
import com.solers.delivery.util.transport.TransferServiceAdapter;

public class InventoryRetrieverTestCase extends TestCase {
    private TransferService failedTransferService = new TransferServiceAdapter() {
        @Override
        public Transfer process(Transfer t) {
            t.setStatusCleanup(Status.FAILED_TRANSFER);
            assertTrue(t.failed());
            return t;
        }
        @Override
        public List<TransferStatus> getCurrentTransfers() {
           return null;
        }
    };
    
    private TransferService notModifiedTransferService = new TransferServiceAdapter() {
        @Override
        public Transfer process(Transfer t) {
            t.setStatusCleanup(Status.COMPLETE_NOT_MODIFIED);
            assertTrue(((TransferInventory)t).isNotModified());
            return t;
        }
        @Override
        public List<TransferStatus> getCurrentTransfers() {
           return null;
        }
    };
    
    private File tempdir;
    private InventoryRetriever retriever;
    
    public void setUp() {
        tempdir = new File("temp");
        tempdir.mkdirs();
        retriever = new InventoryRetriever();
        retriever.setNumBackups(1);
        try {
            InventoryFactory.setSiteDirectory(tempdir.getAbsolutePath());
        } catch (IllegalStateException ise) {
            //ok
        }
    }
    
    public void tearDown() {
        try {
            FileUtils.deleteDirectory(tempdir);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            fail();
        }
    }
    
    public void testFailedTransfer() {
        ConsumerContentSet contentSet = new ConsumerContentSet();
        contentSet.setName("notImportant");
        contentSet.setSupplierName("notImportant");
        assertFalse(
            retriever.getNewInventory(contentSet, 0l, failedTransferService, "").successful()
        );
    }
    
    public void testNotModifiedTransfer() {
        ConsumerContentSet contentSet = new ConsumerContentSet();
        contentSet.setName("notImportant");
        contentSet.setSupplierName("notImportant");
        assertTrue(
            retriever.getNewInventory(contentSet, 0l, notModifiedTransferService, "").successful()
        );
    }
}
