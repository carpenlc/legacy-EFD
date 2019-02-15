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

import org.quartz.JobExecutionException;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class CronTriggerTestCase extends TestCase {
    
    public void testSetInvalidCronExpression() {
        CronTrigger trigger = new CronTrigger();
        
        try {
            trigger.setCronExpression("invalid");
            assertEquals(CronTrigger.DEFAULT_EXPRESSION, trigger.getCronExpression());
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }
    
    public void testValidCronExpression() {
        String expression = "0 * * * * ?";
        CronTrigger trigger = new CronTrigger();
        
        trigger.setCronExpression(expression);
        
        assertEquals(expression, trigger.getCronExpression());
    }
    
    public void testExecutionComplete() {
        JobExecutionException result = new JobExecutionException();
        CronTrigger trigger = new CronTrigger();
        trigger.setCronExpression("0 * * * * ?");
        trigger.executionComplete(null, result);
        assertEquals(true, trigger.isExecutionCompleted());
    }
}
