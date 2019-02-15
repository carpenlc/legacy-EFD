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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import com.solers.delivery.domain.ConsumerContentSet;
import com.solers.delivery.domain.ContentSet;
import com.solers.delivery.domain.ScheduleExpression;

public class ContentSetSchedulerFactory {
    private static final Logger log = Logger.getLogger(ContentSetSchedulerFactory.class);
    
    public ContentSetScheduler newScheduler(Runnable task, ContentSet config) {
        if (useScheduleExpression(config)) {    
            String name = config.getName();
            String group = (config instanceof ConsumerContentSet)? "JOBGROUP.CONSUMER" : "JOBGROUP.SUPPLIER";
            
            return new QuartzScheduler(config, 
                                       getScheduler(name),
                                       getJobDetail(task,name, group),
                                       getTriggers(config, group));
        } else {
            return new SimpleScheduler(task, config);
        }
    }       
    
    Scheduler getScheduler(String name) {
        SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean(); 
        TaskExecutor executor = new ThreadPoolTaskExecutor();
        schedulerFactory.setTaskExecutor(executor);
        schedulerFactory.setWaitForJobsToCompleteOnShutdown(false);
        schedulerFactory.setAutoStartup(false);
        schedulerFactory.setSchedulerName(name);
        
        try {
            schedulerFactory.afterPropertiesSet();
            Scheduler scheduler = (Scheduler)schedulerFactory.getObject();
            scheduler.addSchedulerListener((SchedulerListener)executor);            
            return scheduler;
        } catch (Exception e) {
            log.error("Error setting up job qaurtz scheduler", e);
            throw new IllegalStateException("Error creating quartz scheduler");
        }  
    }
    
    JobDetail getJobDetail(Runnable task, String name, String group) {
        MethodInvokingJobDetailFactoryBean jobDetail = new MethodInvokingJobDetailFactoryBean();
        jobDetail.setName(name);
        jobDetail.setGroup(group);
        jobDetail.setTargetObject(task);
        jobDetail.setTargetMethod("run");
        
        try {
            jobDetail.afterPropertiesSet();
            return(JobDetail) jobDetail.getObject();
        } catch (NoSuchMethodException e) {
            log.error("Error setting up job detail", e);
            throw new IllegalStateException("Error creating job detail");
        } catch (ClassNotFoundException e) {
            log.error("Error setting up job detail class", e);
            throw new IllegalStateException("Error creating job detail class");
        }      
    }
    
    List<CronTrigger> getTriggers(ContentSet config, String group) {
        Set<ScheduleExpression> expressions = config.getScheduleExpressions();
        List<CronTrigger> triggers = new ArrayList<CronTrigger>();
        
        for (ScheduleExpression expression : expressions) {
           CronTrigger trigger = getTrigger(expression, config.getName(), group);
           triggers.add(trigger);
           
           log.info("Trigger for Content Set: " +config.getName()+". <"+expression.getCronExpression()+"> will fire on "+ScheduleUtil.getNextFireDate(expression));
        }
        return triggers;
    }   
    
    CronTrigger getTrigger(ScheduleExpression expression, String name, String group) {
        CronTrigger trigger = new CronTrigger();
        trigger.setCronExpression(expression.getCronExpression());
        trigger.setName(getTriggerName(name, expression.getId()));
        
        if (expression.getDuration() > 0 && expression.getDurationUnit() != null) {             
            trigger.getJobDataMap().put(ScheduleUtil.getDurationKey(trigger.getName()), 
                                   Long.valueOf(getDuration(expression)));
        }           
        trigger.setGroup(group);
        return trigger;
    }
    
    private String getTriggerName(String contentSetName, Long exprId) {
        return new StringBuilder(contentSetName).append(".").append(exprId).toString();    
    }
    
    private long getDuration(ScheduleExpression expression) {
        return expression.getDurationUnit().toMillis(expression.getDuration());  
    }
    
    private boolean useScheduleExpression(ContentSet config) {
        return (config.getScheduleExpressions() != null &&
                config.getScheduleExpressions().size() > 0);
    }
}