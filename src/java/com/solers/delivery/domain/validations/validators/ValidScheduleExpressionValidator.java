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
package com.solers.delivery.domain.validations.validators;

import java.text.ParseException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.hibernate.validator.Validator;
import org.quartz.CronExpression;

import com.solers.delivery.domain.validations.ValidScheduleExpression;

public class ValidScheduleExpressionValidator implements Validator<ValidScheduleExpression> {
  
    private static final Logger log = Logger.getLogger(ValidScheduleExpressionValidator.class);
    
    @Override
    public void initialize(ValidScheduleExpression arg0) {        
    }

    @Override
    public boolean isValid(Object arg) {
        if (!(arg instanceof String)) {
            return false;
        }
        return isValid((String) arg);
    }
    
    private boolean isValid(String expression) {
        try {
            CronExpression cronExpr = new CronExpression(expression);
            cronExpr.getNextValidTimeAfter(new Date());
            return true;
        } catch (ParseException ex) {
            log.error("Invalid cron expression: "+expression);
        } catch (UnsupportedOperationException ex) {
            log.error("UnsupportedOperationException: "+expression, ex);
        }
        return false;
    }
}