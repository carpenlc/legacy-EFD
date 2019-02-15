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

import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

import com.solers.delivery.content.consumer.difference.ContentDifference;
import com.solers.delivery.content.consumer.difference.ContentDifferenceActions;
import com.solers.delivery.content.consumer.difference.ContentDifferenceHandler;
import com.solers.delivery.content.status.ConsumerProgress;

/**
 * Processes items in a given Queue until the Thread is interrupted
 * 
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class ContentDiffereceTask implements Runnable {

    private static final Logger log = Logger.getLogger(ContentDiffereceTask.class);
    
    private final BlockingQueue<ContentDifference> queue;
    private final ContentDifferenceHandler handler;
    private final ConsumerProgress progress;

    /**
     * Constructor
     * 
     * @param queue
     * @param handler
     * @param progress
     */
    public ContentDiffereceTask(BlockingQueue<ContentDifference> queue, ContentDifferenceHandler handler, ConsumerProgress progress) {
        this.queue = queue;
        this.handler = handler;
        this.progress = progress;
    }

    /**
     * Run through the elements in the Queue until the poison pill
     * is seen or the thread is interrupted
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                ContentDifference difference = queue.take();
                if (difference.getAction() == ContentDifferenceActions.DONE) {
                    handler.cleanup(progress);
                    Thread.currentThread().interrupt();
                } else {
                    handler.handle(difference, progress);
                }
            } catch (InterruptedException ex) {
                log.info("Interrupted");
                Thread.currentThread().interrupt();
            }
        }
    }

}
