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
package com.solers.delivery.domain;

import com.solers.util.unit.TimeIntervalUnit;

public class ScheduleExpressionTestCase extends BaseValidationTestCase {
    
    public void testEquals() {
        assertEquals(new ScheduleExpression(), new ScheduleExpression());
        assertEquals(new ScheduleExpression("anything"), new ScheduleExpression("anything"));
        assertFalse(new ScheduleExpression("anything").equals(new ScheduleExpression("abc")));
        
        ScheduleExpression expression1 = new ScheduleExpression("anything");
        ScheduleExpression expression2 = new ScheduleExpression("anything");
        expression2.setDuration(20);
        expression2.setDurationUnit(TimeIntervalUnit.SECONDS);
        assertTrue(expression1.equals(expression2));
    }
    
    public void testHashCode() {
        assertEquals(0, new ScheduleExpression().hashCode());
    }
   
    public void testValidExpression() {
        ScheduleExpression expression = new ScheduleExpression("anything");
        assertInvalid(expression);
        
        expression.setCronExpression("0 * * * * ?");
        assertValid(expression);
        
        expression.setCronExpression(" 0 * * * Mar Sun");
        assertInvalid(expression);
    }
    
    public void testValidDuration() {           
        ScheduleExpression expression = new ScheduleExpression("0 0/2 * * * ?");
        expression.setDuration(20);
        expression.setDurationUnit(null);
        assertInvalid(expression);
        
        expression.setDuration(20);
        expression.setDurationUnit(TimeIntervalUnit.MINUTES);
        assertValid(expression);            
    }
}
