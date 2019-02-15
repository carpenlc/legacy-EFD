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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import com.solers.delivery.content.consumer.difference.ContentDifference;
import com.solers.delivery.content.consumer.difference.ContentDifferenceActions;
import com.solers.delivery.content.consumer.difference.ContentDifferenceHandler;
import com.solers.delivery.content.status.ConsumerProgress;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class ContentDifferenceTaskTestCase extends TestCase {

    private static final int QUEUE_SIZE = 10;
    
    BlockingQueue<ContentDifference> queue;
    MockContentDifferenceHandler handler;
    ContentDiffereceTask task;
    
    public void setUp() {
        queue = new ArrayBlockingQueue<ContentDifference>(QUEUE_SIZE);
        handler = new MockContentDifferenceHandler();
        task = new ContentDiffereceTask(queue, handler, null); 
    }
    
    public void testHandle() throws InterruptedException {
        ContentDifference difference = new ContentDifference(ContentDifferenceActions.ADD);
        
        queue.put(difference);
        queue.put(new ContentDifference(ContentDifferenceActions.DONE));
        
        ExecutorService exec = Executors.newSingleThreadExecutor();
        exec.execute(task);
        
        try {
            exec.shutdown();
            exec.awaitTermination(2, TimeUnit.SECONDS);
        } finally {
            exec.shutdownNow();
        }
        
        assertSame(difference, handler.mostRecent);
    }
    
    public void testCleanup() throws InterruptedException {
        ContentDifference difference = new ContentDifference(ContentDifferenceActions.ADD);
        
        queue.put(difference);
        queue.put(new ContentDifference(ContentDifferenceActions.DONE));
        
        assertFalse(handler.cleanupCalled);
        
        ExecutorService exec = Executors.newSingleThreadExecutor();
        exec.execute(task);
        
        try {
            exec.shutdown();
            exec.awaitTermination(2, TimeUnit.SECONDS);
        } finally {
            exec.shutdownNow();
        }
        
        assertTrue(handler.cleanupCalled);
    }
    
    class MockContentDifferenceHandler implements ContentDifferenceHandler {
        
        ContentDifference mostRecent = null;
        boolean cleanupCalled = false;
        
        public void handle(ContentDifference difference, ConsumerProgress progress) {
            mostRecent = difference;
        }
        
        public void cleanup(ConsumerProgress progress) {
            cleanupCalled = true;
        }
        
    }
}
