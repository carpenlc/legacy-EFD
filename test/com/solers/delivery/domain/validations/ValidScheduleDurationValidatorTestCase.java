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
package com.solers.delivery.domain.validations;

import org.junit.Assert;
import org.junit.Test;

import com.solers.delivery.domain.ScheduleExpression;
import com.solers.delivery.domain.validations.validators.ValidScheduleDurationValidator;
import com.solers.util.unit.TimeIntervalUnit;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class ValidScheduleDurationValidatorTestCase {
    
    @Test
    public void testValid() {
        ValidScheduleDurationValidator v = new ValidScheduleDurationValidator();
        
        ScheduleExpression expr = new ScheduleExpression();
        
        Assert.assertTrue(v.isValid(expr));
        
        expr.setDuration(1);
        
        Assert.assertFalse(v.isValid(expr));
        
        expr.setDurationUnit(TimeIntervalUnit.MINUTES);
        
        Assert.assertTrue(v.isValid(expr));
    }
    
}
