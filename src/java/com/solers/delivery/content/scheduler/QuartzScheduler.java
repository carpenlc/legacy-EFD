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

import org.apache.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerListener;
import org.quartz.UnableToInterruptJobException;
import org.springframework.beans.factory.annotation.Configurable;
import com.solers.delivery.domain.ContentSet;


@Configurable
public class QuartzScheduler extends AbstractContentSetScheduler  {
	private static final Logger log = Logger.getLogger(QuartzScheduler.class);
    private static final boolean DEBUG_ENABLED = log.isDebugEnabled();
	
    private final Scheduler scheduler;
    private final JobDetail jobDetail;
    private final List<CronTrigger> triggers;
	public QuartzScheduler(ContentSet config, 
	                       Scheduler scheduler, 
	                       JobDetail jobDetail,
	                       List<CronTrigger> triggers) {    
		super(config);
		this.scheduler = scheduler;
		this.jobDetail = jobDetail;
		this.triggers = triggers;
	}

	public synchronized void start() {	     
        ContentSet config = getConfig();
        if (DEBUG_ENABLED)
            log.debug("Starting ContentSet: " + config.getName());             
        try {
            if (!scheduler.isStarted()) {
                addTriggerListener();             
                scheduler.start();
                
                boolean jobAdded = false;
		        for (CronTrigger trigger : triggers) {		
                    if (!jobAdded) {
	 	                scheduler.scheduleJob(jobDetail, trigger);
	 	                jobAdded = true;
                    } else {
                        trigger.setJobName(jobDetail.getName());
                        trigger.setJobGroup(jobDetail.getGroup());
                        scheduler.scheduleJob(trigger);
                    }
	 	            
		        }
            }
		} catch (SchedulerException e) {
		    log.error("Failed to schedule the job for content set: " + config.getName(), e);		    
		}		
		config.setEnabled(true);
	} 
	
	public synchronized void stop() {
        ContentSet config = getConfig();
        if (DEBUG_ENABLED)
            log.debug("Stopping ContentSet: " + config.getName());
        try {
            if (scheduler != null) {
                scheduler.shutdown();
            }
        } catch (UnableToInterruptJobException e) {
            log.error("Failed to stop job of content set: " + config.getName(),e);            
        } catch(SchedulerException e) {
            log.error("Failed to shutdown scheduler of content set: " + config.getName(),e);
        }
        config.setEnabled(false);
	}	
	
	private void addTriggerListener() throws SchedulerException {
        addTriggerListener(new ScheduledJobMonitor());
	}
	
	void addTriggerListener(TriggerListener listener) throws SchedulerException {
        scheduler.addGlobalTriggerListener(listener); 
	}
	
    TriggerListener getTriggerListener(String name) throws SchedulerException {
        return scheduler.getGlobalTriggerListener(name);
    }
}