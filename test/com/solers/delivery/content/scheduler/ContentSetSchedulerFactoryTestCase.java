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

import java.util.List;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;

import junit.framework.TestCase;

import com.solers.delivery.domain.ContentSet;
import com.solers.delivery.domain.ScheduleExpression;
import com.solers.util.unit.TimeIntervalUnit;

public class ContentSetSchedulerFactoryTestCase extends TestCase {
    private ContentSet config;
    private Runnable task;
    private ContentSetSchedulerFactory factory;
    private ContentSetScheduler scheduler;
    private static final String GROUP = "DEFAULT";
    
    public void setUp() {
        config = new ContentSet();
        config.setName("TestContentSet");
        ScheduleExpression expression = new ScheduleExpression("*/5 * * * * ?");
        expression.setId(1L);        
        config.addScheduleExpression(expression);
        
        task = new Runnable() {
            public void run() {                
            }
        };
                
        factory = new ContentSetSchedulerFactory();
        scheduler = factory.newScheduler(task, config);
    }   
    
    public void testCreate() {
       ContentSet config = scheduler.getConfig();
       assertNotNull(scheduler.getConfig());
       assertNotNull(factory.getJobDetail(task, config.getName(), GROUP));
       assertNotNull(factory.getTriggers(config, GROUP));
       assertNotNull(factory.getScheduler(config.getName()));
    }
    
    public void testGetScheduler() {
        Scheduler scheduler = factory.getScheduler(config.getName());   
        assertTrue(scheduler instanceof Scheduler);       
    }
    
    public void testGetJobDetail() {
        JobDetail detail = factory.getJobDetail(task, config.getName(), GROUP);        
        assertTrue(detail.getName().equals("TestContentSet"));
    }
    
    public void testGetTriggers() {
        List<CronTrigger> triggers = factory.getTriggers(config, GROUP);
        assertTrue(triggers.size() == 1);    
        
        ScheduleExpression expression = new ScheduleExpression("0 15 10 * * ?");
        expression.setId(2L);   
        config.addScheduleExpression(expression);
        assertTrue(factory.getTriggers(config, GROUP).size() == 2);
    }
    
    public void testJobDataMap() {
        CronTrigger trigger = factory.getTriggers(config, "DEGAULT").get(0);
        JobDataMap map = trigger.getJobDataMap();
        assertTrue(map.size() == 0);
        
        ContentSet newConfig = new ContentSet();
        newConfig.setName("NewTestContentSet");
        ScheduleExpression expression = new ScheduleExpression("*/5 * * * * ?");
        expression.setId(1L);
        expression.setDuration(200);
        expression.setDurationUnit(TimeIntervalUnit.SECONDS);
        newConfig.addScheduleExpression(expression);
        
        expression = new ScheduleExpression("0 15 10 * * ?");
        expression.setId(2L);
        newConfig.addScheduleExpression(expression);
        
        List<CronTrigger> triggers = factory.getTriggers(newConfig, GROUP);
        
        for (CronTrigger ctrigger : triggers) {
           if (ctrigger.getName().equals("NewTestContentSet.1")) {        
              map = ctrigger.getJobDataMap();
              assertTrue(map.size() == 1);       
              assertEquals(map.getLong(ScheduleUtil.getDurationKey(ctrigger.getName())), 200000);
           } else if (ctrigger.getName().equals("NewTestContentSet.2")) {
               map = ctrigger.getJobDataMap();
               assertTrue(map.size() == 0);  
           } else {
               fail();
           }
        }
    }
}
