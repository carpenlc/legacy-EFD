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

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import com.solers.delivery.transport.http.TransferStatus;
import com.solers.delivery.transport.http.TransferType;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class ConsumerProgressTestCase extends TestCase {

    @Test
    public void testSimpleProgress() {
        ConsumerProgress progress = new ConsumerProgress();
        progress.initialize(4, 15, 4, 3, 2, 12);
        
        assertEquals(0.0d, progress.getPercentComplete());

        progress.added(2, true);
        progress.added(2, true);
        progress.added(2, true);
        progress.added(2, true);
        progress.added(2, true);
        progress.added(2, true);
        progress.added(2, true);
        progress.added(1, true);

        assertEquals(50.0d, progress.getPercentComplete());
        assertEquals(15, progress.getCompletedBytes());

        progress.updated(3, true);

        assertEquals(60.0d, progress.getPercentComplete());
        assertEquals(18, progress.getCompletedBytes());

        progress.removed(4, true);
        progress.removed(2, true);
        progress.removed(2, true);
        progress.removed(2, true);
        progress.removed(2, true);

        assertEquals(30, progress.getCompletedBytes());
        assertEquals(100.0d, progress.getPercentComplete());
    }
    
    public void testPercentCompleteWithTransfers() {
        ConsumerProgress progress = new ConsumerProgress();
        progress.initialize(2, 10);
        
        progress.added(5, true);
        
        assertEquals(50.0d, progress.getPercentComplete());
        
        TransferStatus x = new TransferStatus() {
            @Override
            public long getBytesTransferred() {
                return 4;
            }

            @Override
            public String getFileName() {
                return null;
            }

            @Override
            public double getPercentComplete() {
                return 0;
            }

            @Override
            public long getBytesRequested() {
                return 0;
            }

            @Override
            public TransferType getTransferType() {
                // TODO Auto-generated method stub
                return null;
            }
        };
        
        List<TransferStatus> list = Arrays.asList(x);
        
        assertEquals(90.0d, progress.getPercentComplete(list));
    }

    public void testDivideByZeroBugs() {
        ConsumerProgress progress = new ConsumerProgress();
        progress.initialize(0, 0);

        progress.getBytesAdded();
        progress.getBytesDeleted();
        progress.getBytesUpdated();
        progress.getCompletedBytes();
        progress.getFilesAdded();
        progress.getFilesDeleted();
        progress.getFilesUpdated();

        // if we made it here, no divide by zero errors
        assertTrue(true);
        assertEquals(100.00d, progress.getPercentComplete(), 0.0d);
    }

    public void testAdded() {
        ConsumerProgress progress = new ConsumerProgress();
        progress.initialize(4, 0, 4, 0, 2, 0);

        assertEquals(0, progress.getFilesAdded());
        assertEquals(0, progress.getBytesAdded());
        progress.added(2, true);
        assertEquals(1, progress.getFilesAdded());
        assertEquals(2, progress.getBytesAdded());

    }

    public void testUpdated() {
        ConsumerProgress progress = new ConsumerProgress();
        progress.initialize(4, 0, 4, 0, 2, 0);

        assertEquals(0, progress.getFilesUpdated());
        assertEquals(0, progress.getBytesUpdated());
        progress.updated(2, true);
        progress.updated(2, true);
        assertEquals(2, progress.getFilesUpdated());
        assertEquals(4, progress.getBytesUpdated());

    }

    public void testRemoved() {
        ConsumerProgress progress = new ConsumerProgress();
        progress.initialize(4, 0, 4, 0, 2, 0);

        assertEquals(0, progress.getFilesDeleted());
        assertEquals(0, progress.getBytesDeleted());
        progress.removed(2, true);
        progress.removed(6, true);
        progress.removed(2, true);
        assertEquals(3, progress.getFilesDeleted());
        assertEquals(10, progress.getBytesDeleted());
        
    }

    public void testArithmetic() {
        try {
            ConsumerProgress progress = new ConsumerProgress();
            progress.initialize(3, 0);
            progress.added(21, true);
        } catch (ArithmeticException ae) {
            fail();
        }
        
        assertTrue(true);
    }
    
    public void testFailurePercent() {
        ConsumerProgress progress = new ConsumerProgress();
        progress.initialize(2, 2);
        progress.added(1, true);
        assertEquals(50.0d, progress.getPercentComplete());
        progress.added(1, false);
        assertEquals(1, progress.getFailures());
        assertEquals(1, progress.getFailedBytes());
        progress.added(1, true);
        assertEquals(100.0d, progress.getPercentComplete());
    }
    
    public void testFailureTypes() {
        ConsumerProgress progress = new ConsumerProgress();
        progress.initialize(2, 2, 2, 2, 2,  2);
        
        progress.added(1, true);
        progress.updated(1, true);
        progress.removed(1, true);
        assertEquals(50.0d, progress.getPercentComplete());
        assertEquals(3, progress.getFilesAdded() + progress.getFilesDeleted() + progress.getFilesUpdated());
        assertEquals(1, progress.getFilesAddedRemaining());
        assertEquals(1, progress.getBytesAddedRemaining());
        
        progress.added(1, false);
        assertEquals(0, progress.getFilesAddedRemaining());
        assertEquals(0, progress.getBytesAddedRemaining());
        assertEquals(1, progress.getFailures());
        assertEquals(1, progress.getFailedBytes());
        assertEquals(1, progress.getFilesUpdatedRemaining());
        assertEquals(1, progress.getBytesUpdatedRemaining());
        
        progress.updated(1, false);
        assertEquals(0, progress.getFilesUpdatedRemaining());
        assertEquals(0, progress.getBytesUpdatedRemaining());
        assertEquals(2, progress.getFailures());
        assertEquals(2, progress.getFailedBytes());
        assertEquals(1, progress.getFilesDeletedRemaining());
        assertEquals(1, progress.getBytesDeletedRemaining());
        
        progress.removed(1, false);
        assertEquals(0, progress.getFilesDeletedRemaining());
        assertEquals(0, progress.getBytesDeletedRemaining());
        assertEquals(3, progress.getFailures());
        assertEquals(3, progress.getFailedBytes());
    }
}
