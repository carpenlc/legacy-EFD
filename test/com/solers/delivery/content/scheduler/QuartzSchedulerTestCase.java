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


import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.TriggerListener;
import org.quartz.listeners.TriggerListenerSupport;

import com.solers.delivery.domain.ContentSet;
import com.solers.delivery.domain.ScheduleExpression;

public class QuartzSchedulerTestCase extends  ContentSetSchedulerTestBase {
    private boolean triggerFired;
    private TriggerListener listener;
    private Object lock = new Object();    
    
    public ContentSet getContentSet() {
        config = new ContentSet();
        config.setName("ContentSet1");
        ScheduleExpression expression = new ScheduleExpression("*/3 * * * * ?");
        expression.setId(1L);
        config.addScheduleExpression(expression);
        return config; 
    }

    private ContentSetScheduler getScheduler(Runnable newTask) {
        ContentSet newConfig = new ContentSet();
        newConfig.setName("ContentSetNew");
        ScheduleExpression expression = new ScheduleExpression("*/3 * * * * ?");
        expression.setId(2L);
        newConfig.addScheduleExpression(expression);
        
        return getScheduler(newConfig, newTask);
    }

    private ContentSetScheduler getScheduler(ContentSet config, Runnable task) {
        ContentSetScheduler scheduler = factory.newScheduler(task, config);
        return scheduler; 
    }
    
    @Override
    public void testEnsuresTaskStartsOnlyOnce() throws Exception {
        MockRunnable newTask = new MockRunnable();
        ContentSetScheduler scheduler = getScheduler(newTask);
 
        listener = new TestTriggerListener();
        ((QuartzScheduler)scheduler).addTriggerListener(listener);
        scheduler.start();        
        scheduler.start();
        
        while(true) {
            synchronized(listener) {
               try {  
                   listener.wait();
               } catch (InterruptedException e) {
                   fail("Interruted."); 
               }
            }

            if (triggerFired) {
                break;
            }
        }
       
        Thread.sleep(300);
        assertEquals(1, newTask.count);
        scheduler.stop();
        triggerFired = false;
    }
    
    @Override
    public void testStopActuallyStopsThread() throws Exception {
        RunUntilInterrupted newTask = new RunUntilInterrupted();
        ContentSetScheduler scheduler = factory.newScheduler(newTask, config);
        
        scheduler.start();
        Thread.sleep(100);
        assertFalse(newTask.stopped);
        
        while (!newTask.running) {
            Thread.sleep(100);
        }
        
        scheduler.stop();
        Thread.sleep(300);
        assertTrue(newTask.stopped);
    }
    
    public void testAddTriggerListener() throws Exception {
        QuartzScheduler scheduler = (QuartzScheduler)getScheduler(new MockRunnable());
        scheduler.addTriggerListener(new TestTriggerListener());
        assertTrue(scheduler.getTriggerListener("testtrigger") != null);
        assertEquals(scheduler.getTriggerListener("testtrigger").getName(), "testtrigger");
    }
    
    public void testMultipleTriggers() throws Exception {
        MockRunnable newTask = new MockRunnable();
        ScheduleExpression expression = new ScheduleExpression("*/1 * * * * ?");
        expression.setId(2L);
        config.addScheduleExpression(expression);
        ContentSetScheduler scheduler = getScheduler(config, newTask);
 
        MultiTriggerListener listener = new MultiTriggerListener();
        ((QuartzScheduler)scheduler).addTriggerListener(listener);
        scheduler.start();        
        
        while(true) {
            synchronized(lock) {
               try {  
                   lock.wait();
               } catch (InterruptedException e) {
                   fail("Interruted."); 
               }
            }

            if (listener.trigger1Fired && listener.trigger2Fired) {
                break;
            }
        }
       
        Thread.sleep(200);
        assertTrue(newTask.count >= 2);
    }
    
    class TestTriggerListener extends TriggerListenerSupport {        
        public String getName() {
            return "testtrigger";
        }
        
        public void triggerFired(Trigger trigger, JobExecutionContext context) {
            synchronized(this) {
                triggerFired = true;     
                notify();
            }
        }
    }
    
    class MultiTriggerListener extends TriggerListenerSupport {
        boolean trigger1Fired;
        boolean trigger2Fired;
        
        public String getName() {
            return "multitrigger";
        }
        
        public void triggerFired(Trigger trigger, JobExecutionContext context) {
            synchronized(lock) {
                if (trigger.getName().equals("ContentSet1.1")) {
                    trigger1Fired = true;
                }
                if (trigger.getName().equals("ContentSet1.2")) {
                    trigger2Fired = true;
                }             
                lock.notify();;
            }
        }
    }
}