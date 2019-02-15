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

import java.text.ParseException;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.CronTriggerBean;

public class CronTrigger extends CronTriggerBean {
    
    public static final String DEFAULT_EXPRESSION = "0 0 0 ? * SAT";
    
    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(CronTrigger.class);

    private boolean executionCompleted;
    
    @Override
    public void setCronExpression(String expression) {
        try {
            super.setCronExpression(expression);
        } catch (ParseException e) {
            log.error(String.format("Invalid cron expression supplied [%s]", expression), e);

            try {
                super.setCronExpression(DEFAULT_EXPRESSION);
                log.info(String.format("Default value: every SAT at 12 AM is used : [%s]", DEFAULT_EXPRESSION));
            } catch (ParseException ex) {
                log.error("Error using default expression", ex);
            }
        }
    }
    
    @Override
    public int executionComplete(JobExecutionContext context, JobExecutionException result) {
        this.executionCompleted = true;           
        return super.executionComplete(context, result);
    }
    
    public boolean isExecutionCompleted() {
        return this.executionCompleted;
    }
}
