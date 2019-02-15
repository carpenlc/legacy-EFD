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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import junit.framework.TestCase;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.listeners.TriggerListenerSupport;

import com.solers.delivery.domain.ConsumerContentSet;
import com.solers.delivery.domain.ScheduleExpression;
import com.solers.util.unit.TimeIntervalUnit;

public class ScheduledJobMonitorTestCase extends TestCase {
    private ConsumerContentSet config;
    private ContentSetSchedulerFactory factory;
    private Scheduler scheduler;
    private LongRunningTask task;
    private JobDetail jobDetail;
    private CronTrigger trigger;
    private JobExecutionContext jobContext;
    private MockListener listener;
    private String triggerName;
    
    private static final String GROUP_NAME = "GROUP.CONSUMER";
    
    public void setUp() {
        config = new ConsumerContentSet();
        config.setName("Consumer");
        ScheduleExpression expression = new ScheduleExpression("*/5 * * * * ?");
        expression.setId(1L);
        expression.setDuration(1);
        expression.setDurationUnit(TimeIntervalUnit.SECONDS);        
        config.addScheduleExpression(expression);
        
        task = new LongRunningTask();
                
        factory = new ContentSetSchedulerFactory();
        scheduler = factory.getScheduler(config.getName());         
        jobDetail = factory.getJobDetail(task, config.getName(), GROUP_NAME);
        trigger = factory.getTrigger(expression, config.getName(), GROUP_NAME);
        triggerName = trigger.getName();
        
        listener = new MockListener();
    }          
    
    public void testTriggerFired() throws Exception {
        try {                
            scheduler.addGlobalTriggerListener(listener);
            scheduler.start();         
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            fail("Faild to start scheduler");
        }  
        
        ScheduledJobMonitor monitor = new ScheduledJobMonitor();
        while(true) {
            synchronized(listener) {
               try {                   
                  listener.wait();
               } catch (InterruptedException e) {
                  fail("Interruted."); 
               }
            }
            if (jobContext != null) {
                break;
            }
        }

        monitor.triggerFired(trigger, jobContext);
       
        assertFalse(task.stopped);
        Thread.sleep(1100);
        assertTrue(task.stopped);   
        
        CronTrigger newTrigger = (CronTrigger)scheduler.getTrigger(triggerName, "DEFAULT");
        assertTrue(newTrigger != null);
        assertTrue(newTrigger.getNextFireTime().after(new java.util.Date()));
        scheduler.shutdown();
    }
    
    private class MockListener extends TriggerListenerSupport {
        public String getName() {
            return "MockListener";
        }
        
        public void triggerFired(Trigger trigger, JobExecutionContext context) {
            synchronized(listener) {
               jobContext = context;              
               notify();
            }
        }
    }
    
    public class LongRunningTask implements Runnable {
        volatile boolean stopped = false;
        
        BlockingQueue<Object> queue = new ArrayBlockingQueue<Object>(5);

        public void run() {
            try {
                queue.take();
            } catch (InterruptedException ex) {
                stopped = true;
            }
        }
    }
}