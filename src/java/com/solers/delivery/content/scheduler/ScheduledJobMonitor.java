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

import java.util.Date;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.listeners.TriggerListenerSupport;


public class ScheduledJobMonitor extends TriggerListenerSupport {
    protected static final Logger log = Logger.getLogger(ScheduledJobMonitor.class);
    
    private Map<String,JobDetail> jobMap = Collections.synchronizedMap(new HashMap<String, JobDetail>());
    
    public String getName() {
        return "JobMonitor";
    }
  
    public void triggerFired(Trigger trigger, JobExecutionContext context) { 
        log.info("Trigger Fired: " + trigger.getName());
        
        Object obj = trigger.getJobDataMap().get(ScheduleUtil.getDurationKey(trigger.getName()));
        
        if (obj != null) {
            Long duration = (Long)obj;
            Scheduler scheduler = context.getScheduler();            
            Date startDate = ScheduleUtil.getStartDate(duration);
            log.info("Current time: " + new Date() + ", going to stop job in: " +
                     duration/1000 + " seconds");
            try {
                Scheduler monitorScheduler = new StdSchedulerFactory().getScheduler();
                monitorScheduler.start();
                JobDetail jobDetail = jobMap.get(trigger.getName());
                if (jobDetail == null) {
                    jobDetail = new JobDetail("monitorjob." + trigger.getJobName(), "MONITOR", ScheduledJobStopper.class);
                    JobDataMap map = jobDetail.getJobDataMap();
                    map.put(ScheduledJobStopper.SCHEDULER, scheduler);
                    map.put(ScheduledJobStopper.TRIGGER, trigger);
                    jobMap.put(trigger.getName(), jobDetail);
                }
                
                Trigger simpleTrigger = new SimpleTrigger("monitorTrigger", "MONITOR", startDate);
                monitorScheduler.deleteJob(jobDetail.getName(), jobDetail.getGroup());
                monitorScheduler.scheduleJob(jobDetail, simpleTrigger);
            } catch (SchedulerException ex) {
                log.error("Failed to create scheduler for job monitor", ex);
            }        
        } else {
            log.info("There is no time limit for executing the job: " + trigger.getName());
        }
    }              
    
    public void triggerComplete(Trigger trigger, JobExecutionContext context,
        int triggerInstructionCode) {
        log.debug("Trigger Complete: " + trigger.getName()); 
    }
}