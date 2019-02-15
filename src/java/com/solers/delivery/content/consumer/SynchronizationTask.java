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
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Qualifier;

import com.solers.delivery.content.consumer.difference.ContentDifference;
import com.solers.delivery.content.consumer.difference.DifferenceGenerator;
import com.solers.delivery.content.consumer.difference.DifferenceQueuer;
import com.solers.delivery.content.consumer.difference.Remover;
import com.solers.delivery.content.consumer.difference.Requester;
import com.solers.delivery.content.scheduler.ScheduleUtil;
import com.solers.delivery.content.status.ConsumerProgress;
import com.solers.delivery.content.status.SynchronizationResult;
import com.solers.delivery.domain.ConsumerContentSet;
import com.solers.delivery.event.SynchronizationEvent;
import com.solers.delivery.event.listener.EventListener;
import com.solers.delivery.inventory.Inventory;
import com.solers.delivery.inventory.InventoryFactory;
import com.solers.delivery.inventory.TimeProperties;
import com.solers.delivery.inventory.comparer.NodeListener;
import com.solers.delivery.management.ConsumerStatus;
import com.solers.delivery.transport.http.TransferStatus;
import com.solers.delivery.transport.http.client.TransferService;
import com.solers.delivery.transport.http.client.TransferServiceImpl;
import com.solers.delivery.transport.http.client.TransferService.TransportUnavailableException;
import com.solers.delivery.util.FileSystemUtil;
import com.solers.delivery.util.MathHelper;
import com.solers.delivery.util.FileSystemUtil.Stats;
import com.solers.jmx.registry.AutoRegister;
import com.solers.jmx.registry.AutoRegisterObjectName;
import com.solers.util.NamedThreadFactory;
import com.solers.util.StopWatch;
import com.solers.util.unit.TimeIntervalUnit;

/**
 * SynchronizationTask should be executed only once, as its run method repeats
 * in an infinite loop. The SynchronizationTask handles all steps in
 * synchronizing a ConsumerContentSet with a SupplierContentSet. The steps to
 * synch include fetching an inventory, extracting its contents, coparing the
 * inventory with the file system and finally handling the differences (by
 * requesting or removing the file).
 * 
 * @author JGimourginas
 */
@Configurable
@AutoRegister(category="Consumer Synchronizations")
public class SynchronizationTask implements Runnable, ConsumerStatus {

    private static final Logger log = Logger.getLogger(SynchronizationTask.class);
    private static final int QUEUE_SIZE = 5000;

    private final TransferService transferService;
    private final ConsumerContentSet config;
    private final Requester requester;
    private final Remover remover;
    private final StopWatch watch;
    
    private EventListener eventListener;
    private ConsumerProgress progress;
    private SynchronizationState state;
    protected Enum<?> substate;
    private long fileCount;
    private long sizeInBytes;
    private long lastRuntime;

    public SynchronizationTask(ConsumerContentSet config) {
        this(config, new TransferServiceImpl(config.getSupplierName(), config.getSupplierAddress(), config.getSupplierPort()));
    }
    
    public SynchronizationTask(ConsumerContentSet config, TransferService transferService) {
        this.transferService = transferService;
        this.requester = new Requester(config, transferService);
        this.remover = new Remover(config);
        this.config = config;
        this.state = SynchronizationState.IDLE;
        this.fileCount = -1;
        this.sizeInBytes = -1;
        this.lastRuntime = -1;
        this.watch = new StopWatch();
    }
    
    @Autowired
    public void setEventManager(@Qualifier("eventManager") EventListener eventManager) {
        this.eventListener = eventManager;
    }
    
    @AutoRegisterObjectName
    public Long getContentSetId() {
        return config.getId();
    }
    
    @Override
    public String toString() {
        return "SynchronizationTask for ContentSet: "+config;
    }

    /**
     * The run method controls the synchronization process. Synchronization
     * involves these main steps: Get an inventory, find the differences between
     * inventory and content set being managed, resolve the differences through
     * file requests and removals.
     * 
     * Must catch all exceptions or this task will never be run again by the scheduled executor.
     */
    public void run() {
        lastRuntime = System.currentTimeMillis();
        
        SynchronizationEvent event = new SynchronizationEvent(config.getId());
        progress = new ConsumerProgress();
        progress.setKey(event.getKey());
        event.setProgress(progress);
        
        log.info("Consumer synchronization started "+event.getKey()+" for content set: " + config.getName());
        
        try {
            watch.start();
            eventListener.consumerSynchronizationStarted(event);
            transferService.start(event);
            InventoryResult inventoryResult = retrieveInventory(event);
            if (inventoryResult.successful()) {
                processInventory(event);
            } else {
                event.setResult(inventoryResult.getSynchronizationResult());
                log.warn("Inventory retrieved was invalid and cannot be used.");
            }
        } catch (TransportUnavailableException ex) {
            log.error("Transport was unavailable for synchronization: "+event.getKey(), ex);
            event.setResult(SynchronizationResult.FAILED_NO_CONNECTION);
        } catch (Exception ex) {
            log.error("Exception during execution of task", ex);
        } finally {
            watch.stop();
            state = SynchronizationState.IDLE;
            lastRuntime = System.currentTimeMillis();
            event.completed(watch.getElapsedTime());
            transferService.stop(event);
            eventListener.consumerSynchronizationCompleted(event);
        }
        
        log.info("Consumer synchronization "+event.getKey()+" completed in " + TimeIntervalUnit.format(event.getElapsedTime()) + " for content set: " + config.getName());
    }
    
    private InventoryResult retrieveInventory(SynchronizationEvent event) {
        state = SynchronizationState.DOWNLOADING_INVENTORY;
        InventoryRetriever inventoryRetriever = new InventoryRetriever();
        inventoryRetriever.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                if (arg instanceof Enum) {
                    substate = (Enum<?>) arg;
                }
            }
        });
        
        try {
            return inventoryRetriever.getNewInventory(config, getInventoryTimestamp(), transferService, event.getKey());
        } finally {
            //inventoryRetriever is an observable that will update our substate object, so
            //set it back to null now that it is done.
            substate = null;
        }
    }
    
    /**
     * Retrieve the current inventory timestamp, or 0 if the inventory cannot be
     * found or loaded.
     * @return 0 or the timestamp from the latest inventory file.
     */
    protected long getInventoryTimestamp() {
        Inventory inventory = null;
        long timestamp = 0;
        
        try {
            inventory = InventoryFactory.getInventory(config);
            if (inventory != null) {
                timestamp = (Long) inventory.getProperty(TimeProperties.TIMESTAMP);
            }
        } finally {
            if (inventory != null) inventory.close();
        }
        
        return timestamp;
    }

    /**
     * Process the inventory
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private void processInventory(SynchronizationEvent event) {
        state = SynchronizationState.PROCESSING_INVENTORY;
        BlockingQueue<ContentDifference> refreshes = new ArrayBlockingQueue<ContentDifference>(QUEUE_SIZE);
        BlockingQueue<ContentDifference> deletes = new ArrayBlockingQueue<ContentDifference>(QUEUE_SIZE);
        DifferenceGenerator differenceGenerator = initializeDifferenceGenerator(refreshes, deletes);
        
        if (runTasks(event, differenceGenerator, refreshes, deletes)) {
            generateResults();
        }
    }
    
    private void generateResults() {
        state = SynchronizationState.INSPECTING;
        Inventory inventory = null;
        String root = null;
        try {
            inventory = InventoryFactory.getInventory(config);
            if (inventory == null) {
                return;
            }
            root = inventory.getRootName();
        } finally {
            if (inventory != null) inventory.close();
        }
        // inspect the content set directory to calculate the results
        File csDirectory = new File(config.getPath(), root);
        Stats s = FileSystemUtil.calculateDirectoryStatistics(csDirectory);
        if (s != null) {
            fileCount = s.getItems();
            sizeInBytes = s.getSize();
        }
    }
    
    private boolean runTasks(SynchronizationEvent event, DifferenceGenerator differenceGenerator, BlockingQueue<ContentDifference> refreshes, BlockingQueue<ContentDifference> deletes) {
        state = SynchronizationState.RUNNING;
        Runnable[] tasks = new Runnable[] { differenceGenerator,
            new ContentDiffereceTask(deletes, remover, progress) };
        
        Runnable[] requesters = new Runnable[config.getConcurrentTransfers()];
        for (int i=0; i < config.getConcurrentTransfers(); i++) {
            requesters[i] = new ContentDiffereceTask(refreshes, requester, progress);
        }
        
        ExecutorService executor = Executors.newFixedThreadPool(tasks.length + requesters.length, new NamedThreadFactory());
        for (Runnable r : tasks) { 
            executor.execute(r); 
        }
        for (Runnable r : requesters) {
            executor.execute(r);
        }
        
        executor.shutdown();
        
        boolean result = true;
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            event.setResult(SynchronizationResult.INTERRUPTED);
            log.info("Synchronization interrupted for content set: " + config.getName());
            executor.shutdownNow();
            result = false;
        }
        
        return result;
    }

    /**
     * Initialize the difference generator
     * 
     * @param refreshes
     * @param deletes
     */
    private DifferenceGenerator initializeDifferenceGenerator(BlockingQueue<ContentDifference> refreshes, BlockingQueue<ContentDifference> deletes) {
        NodeListener handler = new DifferenceQueuer(refreshes, deletes, config.getId(), config.getConcurrentTransfers());
        DifferenceGenerator differenceGenerator = new DifferenceGenerator(config, handler);
        differenceGenerator.prescan(progress);
        transferService.sendMetrics(progress.getTotalNewFiles(), progress.getTotalNewBytes());
        return differenceGenerator;
    }

    /**
     * @return Overall progress of the synch event
     */
    @Override
    public double getPercentCompleted() {
        if (progress == null) {
            return 0.0d;
        }
        
        switch(state) {
            case RUNNING:
            case INSPECTING:
                return progress.getPercentComplete(getCurrentTransfers());
            default:
                return 0.0d;
        }
    }

    @Override
    public long getBytesAdded() {
        if (progress == null)
            return 0;
        return progress.getBytesAdded(getCurrentTransfers());
    }

    @Override
    public long getBytesDeleted() {
        if (progress == null)
            return 0;
        return progress.getBytesDeleted();
    }

    @Override
    public long getBytesUpdated() {
        if (progress == null)
            return 0;
        return progress.getBytesUpdated(getCurrentTransfers());
    }

    @Override
    public long getItemsAdded() {
        if (progress == null)
            return 0;
        return progress.getFilesAdded();
    }

    @Override
    public long getItemsDeleted() {
        if (progress == null)
            return 0;
        return progress.getFilesDeleted();
    }

    @Override
    public long getItemsUpdated() {
        if (progress == null)
            return 0;
        return progress.getFilesUpdated();
    }

    @Override
    public String getState() {
        return (substate == null) ? state.name() : state.name() + "_" + substate.name();
    }

    @Override
    public long getItemCount() {
        if (state != SynchronizationState.IDLE) return -1;
        return this.fileCount;
    }

    @Override
    public long getSize() {
        if (state != SynchronizationState.IDLE) return -1;
        return this.sizeInBytes;
    }
    
    @Override
    public long getLastRuntime() {
        return lastRuntime;
    }

    /**
     * @return the list of in-progress tranfer status which includes files and
     * percentage completed
     */
     @Override
     public List<TransferStatus> getCurrentTransfers() {
          return transferService.getCurrentTransfers();
     }
    
    @Override
    public boolean isEnabled() {
        return config.isEnabled();
    }
    
    @Override
    public long getElapsedTime() {
        if (!watch.isStarted()) { 
            return -1;
        }
        return watch.getElapsedTime();
    }

    @Override
    public long getRemainingTime() {
        if (state != SynchronizationState.RUNNING) { 
            return -1;
        }
        
        return MathHelper.remainingTime(getElapsedTime(), getBytesAdded() + getBytesUpdated(), getBytesAddedRemaining() + getBytesUpdatedRemaining());
    }

    @Override
    public long getNextEstimatedRuntime() {
        if (state != SynchronizationState.IDLE) {
            return -1;
        }
        
        if (isEnabled()) {
            long remaining = TimeIntervalUnit.MILLISECONDS.convert(config.getInventoryInterval(), config.getInventoryIntervalUnit());
            return ScheduleUtil.getTimeToNextFire(lastRuntime, remaining,
                                        config.getScheduleExpressions());
        } else {
            return -1;
        }
    }
    
    @Override
    public long getBytesAddedRemaining() {
        if (progress == null)
            return 0;
        return progress.getBytesAddedRemaining(getCurrentTransfers());
    }

    @Override
    public long getBytesDeletedRemaining() {
        if (progress == null)
            return 0;
        return progress.getBytesDeletedRemaining();
    }

    @Override
    public long getBytesUpdatedRemaining() {
        if (progress == null)
            return 0;
        return progress.getBytesUpdatedRemaining(getCurrentTransfers());
    }

    @Override
    public long getItemsAddedRemaining() {
        if (progress == null)
            return 0;
        return progress.getFilesAddedRemaining();
    }

    @Override
    public long getItemsDeletedRemaining() {
        if (progress == null)
            return 0;
        return progress.getFilesDeletedRemaining();
    }

    @Override
    public long getItemsUpdatedRemaining() {
        if (progress == null)
            return 0;
        return progress.getFilesUpdatedRemaining();
    }
    
    @Override
    public long getFailures() {
        if (progress == null)
            return 0;
        return progress.getFailures();
    }
    
    @Override
    public long getBytesFailed() {
        if (progress == null)
            return 0;
        return progress.getFailedBytes();
    }
}
