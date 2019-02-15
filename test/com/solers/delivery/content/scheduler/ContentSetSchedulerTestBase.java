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

import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;

import com.solers.delivery.domain.ContentSet;
import com.solers.delivery.inventory.InventoryFactory;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public abstract class ContentSetSchedulerTestBase extends TestCase {
    ContentSetSchedulerFactory factory;
    ContentSetScheduler scheduler;
    ContentSet config;
    MockRunnable task;

    public void setUp() {
        config = getContentSet();
        task = new MockRunnable();

        try {
            InventoryFactory.setSiteDirectory("testRuntime");
        } catch (IllegalStateException ise) {
            //ok
        }
                
        factory = new ContentSetSchedulerFactory();
        scheduler = factory.newScheduler(task, config);
    }
    
    protected abstract ContentSet getContentSet();
    
    public void tearDown() {
        try {
            FileUtils.deleteDirectory(new File("testRuntime"));
        } catch (Exception ex) {
            
        }
    }

    public void testStart() {
        assertFalse(config.isEnabled());
        scheduler.start();
        assertTrue(config.isEnabled());
    }

    public void testStop() {
        assertFalse(config.isEnabled());
        scheduler.start();
        assertTrue(config.isEnabled());

        scheduler.stop();
        assertFalse(config.isEnabled());
    }

    public void testEnsuresTaskStartsOnlyOnce() throws Exception {
        scheduler.start();
        scheduler.start();

        Thread.sleep(100);

        assertEquals(1, task.count);
    }

    public void testStopActuallyStopsThread() throws Exception {
        RunUntilInterrupted newTask = new RunUntilInterrupted();
        scheduler = factory.newScheduler(newTask, config);
        scheduler.start();
        Thread.sleep(100);

        assertFalse(newTask.stopped);

        scheduler.stop();

        Thread.sleep(300);

        assertTrue(newTask.stopped);
    }

    public class RunUntilInterrupted implements Runnable {

        volatile boolean stopped = false;
        volatile boolean running = false;
        
        BlockingQueue<Object> queue = new ArrayBlockingQueue<Object>(5);

        public void run() {
            try {
                running = true;
                queue.take();
            } catch (InterruptedException ex) {
                stopped = true;
            }
        }
    }

    public class MockRunnable implements Runnable {

        volatile int count = 0;

        public void run() {
            count++;
        }
    }
}
