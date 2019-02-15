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
package com.solers.delivery.content.scheduler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.Date;

import org.apache.log4j.Logger;

import com.solers.delivery.domain.ContentSet;
import com.solers.util.NamedThreadFactory;
import com.solers.delivery.inventory.InventoryFactory;

/**
 * Schedules a task to run for a content set at a fixed interval. The interval
 * is defined by the content set
 * 
 * @author DBailey
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class SimpleScheduler extends AbstractContentSetScheduler {
    private static final Logger log = Logger.getLogger(SimpleScheduler.class);
    private static final boolean IS_DEBUG_ENABLED = log.isDebugEnabled();

    private final Runnable task;
    private final ScheduledExecutorService scheduler;
    
    private ScheduledFuture<?> taskFuture;

    /**
     * Constructor.
     * 
     * @param task
     *            task that should be used to synchronize the ConsumerContentSet
     *            with a SupplierContentSet. The task will be run periodically
     *            according to the configuration for this specific
     *            ConsumerContentSet.
     */
    public SimpleScheduler(Runnable task, ContentSet config) {
    	super(config);
        this.task = task;
        this.scheduler = Executors.newScheduledThreadPool(1, new NamedThreadFactory(this.getClass().getSimpleName()));
    }

    /**
     * Stop the service
     */
    public synchronized void stop() {
        ContentSet config = getConfig();
        if (IS_DEBUG_ENABLED)
            log.debug("Stopping ContentSet: " + config.getName());
        cancel();
        // This is a hack, not sure this is being updated correctly from the UI, we force it here.
        config.setEnabled(false);
    }

    /**
     * Start the service
     */
    public synchronized void start() {
        ContentSet config = getConfig();
        if (IS_DEBUG_ENABLED)
            log.debug("Starting ContentSet: " + config.getName());
        long intervalInMillis =  config.getInventoryIntervalUnit().toMillis(config.getInventoryInterval());
        
        Date timestamp = InventoryFactory.getInventoryFileTimestamp(config);    
        long delay = getDelay(intervalInMillis, config.isSupplier(), timestamp);
    
        if (IS_DEBUG_ENABLED)
            log.debug("Will synch" + config.getName() + " at most once every " + config.getInventoryInterval() + " " + config.getInventoryIntervalUnit().getDisplayName()
                       + ", with initial delay of " + delay/1000 + " second(s).");
        
        start(delay, intervalInMillis); 
        
        // This is a hack, not sure this is being updated correctly from the UI,
        // we force it here.
        config.setEnabled(true);
    }

    private long getDelay(long intervalInMillis, boolean isSupplier, Date timestamp) {        
        long delay = 0;
            
        if (!isSupplier || timestamp == null) {
            return delay;
        }
        
        Date currentTime = new Date();
        Date nextStartTime = new Date(timestamp.getTime() + intervalInMillis);
        
        if (nextStartTime.after(currentTime)) {                               
            delay = nextStartTime.getTime() - currentTime.getTime();            
        }
        
        return delay;
    }
    
    /**
     * Cancel the currently running task
     */
    private void cancel() {
        if (taskFuture != null) {
            taskFuture.cancel(true);
            taskFuture = null;
        }
    }

    /**
     * Start the task to run every <code>intervalInMillis</code> milliseconds
     * 
     * @param intervalInMillis
     * @return True if the task was started
     */
    private boolean start(long delay, long intervalInMillis) {
        if (taskFuture == null) { // only 1 scheduled task will run at a time
            taskFuture = scheduler.scheduleWithFixedDelay(task, delay, intervalInMillis, TimeUnit.MILLISECONDS);
            return true;
        } else {
            return false;
        }
    }
}