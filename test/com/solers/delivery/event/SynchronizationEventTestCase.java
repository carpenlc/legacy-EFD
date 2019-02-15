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
package com.solers.delivery.event;

import org.junit.Assert;
import org.junit.Test;

import com.solers.delivery.content.status.ConsumerProgress;
import com.solers.delivery.content.status.SynchronizationResult;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class SynchronizationEventTestCase {
    
    @Test
    public void testComplete() {
        ConsumerProgress progress = new ConsumerProgress();
        
        SynchronizationEvent event = new SynchronizationEvent("xx", 1l);
        event.setProgress(progress);
        event.completed(2);
        
        Assert.assertEquals(2, event.getElapsedTime());
        Assert.assertEquals(SynchronizationResult.COMPLETE_NOT_MODIFIED, event.getResult());
        
        
        progress.initialize(2, 10);
        progress.added(5, true);
        progress.added(5, true);
        event.setResult(null);
        event.completed(2);
        
        Assert.assertEquals(2, event.getElapsedTime());
        Assert.assertEquals(SynchronizationResult.COMPLETE_MODIFIED, event.getResult());
    }
    
    @Test
    public void testCompleteWithErrors() {
        ConsumerProgress progress = new ConsumerProgress();
        
        SynchronizationEvent event = new SynchronizationEvent("xx", 1l);
        event.setProgress(progress);
       
        progress.initialize(2, 10);
        progress.added(5, true);
        progress.added(5, false);
        event.completed(2);
        
        Assert.assertEquals(2, event.getElapsedTime());
        Assert.assertEquals(SynchronizationResult.COMPLETE_WITH_ERRORS, event.getResult());
    }
    
    @Test
    public void testResultIsntReset() {
        ConsumerProgress progress = new ConsumerProgress();
        
        SynchronizationEvent event = new SynchronizationEvent("xx", 1l);
        event.setProgress(progress);
        event.setResult(SynchronizationResult.FAILED);
       
        progress.initialize(2, 10);
        progress.added(5, true);
        progress.added(5, false);
        event.completed(2);
        
        Assert.assertEquals(2, event.getElapsedTime());
        Assert.assertEquals(SynchronizationResult.FAILED, event.getResult());
    }
}
