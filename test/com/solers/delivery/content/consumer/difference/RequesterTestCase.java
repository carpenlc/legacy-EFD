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
package com.solers.delivery.content.consumer.difference;

import java.io.File;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.solers.delivery.content.status.ConsumerProgress;
import com.solers.delivery.domain.ConsumerContentSet;
import com.solers.delivery.event.ContentEvent;
import com.solers.delivery.event.MockEventManager;
import com.solers.delivery.event.SynchronizationEvent;
import com.solers.delivery.event.ContentEvent.ContentEventResult;
import com.solers.delivery.transport.http.TransferStatus;
import com.solers.delivery.transport.http.client.Transfer;
import com.solers.delivery.transport.http.client.TransferService;
import com.solers.delivery.transport.http.client.Transfer.Status;
import com.solers.util.unit.TimeIntervalUnit;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class RequesterTestCase extends TestCase {
    
    Requester requester;
    ConsumerProgress progress;
    MockEventManager eventManager;
    MockTransferService service;

    public void setUp() {
        eventManager = new MockEventManager();
        service = new MockTransferService();
        ConsumerContentSet config = new ConsumerContentSet();
        config.setName("RequesterTestCase");
        config.setFileDeleteDelay(0);
        config.setFileDeleteDelayUnit(TimeIntervalUnit.SECONDS);
        config.setId(new Long(1));

        requester = new Requester(config, service);
        requester.setEventManager(eventManager);
        progress = new ConsumerProgress();
        progress.initialize(1, 1, 0, 0, 0, 0);

    }
    
    public void testEventForAddDirectory() {
        File dir = new File("testEventForDirectory");
        dir.mkdir();
        ContentDifference difference = new ContentDifference();
        difference.setDirectory(true);
        difference.setPath(FilenameUtils.separatorsToUnix(dir.getAbsolutePath()));
        difference.setAction(ContentDifferenceActions.ADD);
        
        requester.handle(difference, progress);
        ContentEvent event = (ContentEvent) eventManager.getMostRecent();
        
        assertEquals(1, event.getContentSetId());
        assertEquals(ContentEventResult.SUCCESS.value(), event.getResult());
        assertEquals(difference.getPath(), event.getPath());
    }
    
    public void testEventForUpdateDirectory() {
        File dir = new File("testEventForDirectory");
        dir.mkdir();
        ContentDifference difference = new ContentDifference();
        difference.setDirectory(true);
        difference.setPath(FilenameUtils.separatorsToUnix(dir.getAbsolutePath()));
        difference.setAction(ContentDifferenceActions.REFRESH);
        
        requester.handle(difference, progress);
        ContentEvent event = (ContentEvent) eventManager.getMostRecent();
        
        assertEquals(1, event.getContentSetId());
        assertEquals(ContentEventResult.SUCCESS.value(), event.getResult());
        assertEquals(difference.getPath(), event.getPath());
    }
    
    public void testEventForAddFile() {
        ContentDifference difference = new ContentDifference();
        difference.setDirectory(false);
        difference.setPath("testEventForFile");
        difference.setSize(10);
        difference.setAction(ContentDifferenceActions.ADD);
        
        service.bytesAdded = 10;
        
        requester.handle(difference, progress);
        ContentEvent event = (ContentEvent) eventManager.getMostRecent();
        
        assertEquals(1, event.getContentSetId());
        assertEquals(ContentEventResult.SUCCESS.value(), event.getResult());
        assertEquals(difference.getPath(), event.getPath());
    }
    
    public void testEventForUpdateile() {
        ContentDifference difference = new ContentDifference();
        difference.setDirectory(false);
        difference.setPath("testEventForFile");
        difference.setAction(ContentDifferenceActions.REFRESH);
        
        requester.handle(difference, progress);
        ContentEvent event = (ContentEvent) eventManager.getMostRecent();
        
        assertEquals(1, event.getContentSetId());
        assertEquals(ContentEventResult.SUCCESS.value(), event.getResult());
        assertEquals(difference.getPath(), event.getPath());
    }
    
    public void testFailedFile() {
        ContentDifference difference = new ContentDifference();
        difference.setDirectory(false);
        difference.setPath("testEventForFile");
        difference.setSize(10);
        difference.setAction(ContentDifferenceActions.ADD);
        
        requester.handle(difference, progress);
        ContentEvent event = (ContentEvent) eventManager.getMostRecent();
        
        assertEquals(1, event.getContentSetId());
        assertEquals(ContentEventResult.TRANSFER_FAILED.value(), event.getResult());
        assertEquals(difference.getPath(), event.getPath());
    }
    
    public void tearDown() {
        try {
            FileUtils.deleteDirectory(new File("testEventForDirectory"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    class MockTransferService implements TransferService {
        
        public long bytesAdded = 0;
        
        /**
         * @see com.solers.delivery.transport.http.client.TransferService#process(com.solers.delivery.transport.http.client.Transfer)
         */
        @Override
        public Transfer process(Transfer transfer) {
            transfer.setStatusCleanup(Status.COMPLETED);
            transfer.addBytesTransferred(bytesAdded);
            return transfer;
        }

        @Override
        public void start(SynchronizationEvent event) {
        }

        @Override
        public void stop(SynchronizationEvent event) {
        }

        @Override
        public void sendMetrics(long totalRequests, long totalBytes) {
        }

        @Override
        public List<TransferStatus> getCurrentTransfers() {
           return null;
        }
    }
}
