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
import java.util.Date;
import java.util.Set;

import org.apache.log4j.Logger;
import org.quartz.CronExpression;

import com.solers.delivery.domain.ScheduleExpression;

public final class ScheduleUtil {
    
    private static final Logger log = Logger.getLogger(ScheduleUtil.class);
    
    private ScheduleUtil() { 
        
    }
    
    /** 
     * @param expressions
     * @return The earliest date that one of the given expressions will fire
     */
    public static Date getNextFireDate(Set<ScheduleExpression> expressions) {
        Date result = null;
        
        for (ScheduleExpression expr : expressions) {
            Date date = getNextFireDate(expr);
            if (result == null || date.before(result)) {
                result = date;
            }
        }
        
        return result;
    }
    
    /**
     * @param expr
     * @return The earliest date that {@code expression} will fire
     */
    public static Date getNextFireDate(ScheduleExpression expr) {
        try {
            return new CronExpression(expr.getCronExpression()).getNextValidTimeAfter(new Date());
        } catch (ParseException ex) {
            log.error("Error parsing cron expression", ex);
        }
        return null;
    }
    
    /**
     * @param lastRuntime
     * @param remaining
     * @param expressions
     * @return The amount of time until the given interval or expressions will fire
     */
    public static long getTimeToNextFire(long lastRuntime, long remaining, Set<ScheduleExpression> expressions) {
        if (expressions.size() > 0) {
            return getTimeToNextFire(expressions);
        } else {
            return getIntervalNextRuntime(lastRuntime, remaining); 
        }
    }
    
    public static Date getStartDate(long duration) {
        Date date = new Date();
        Date startDate = new Date(date.getTime() + duration);
        return startDate;
    }
    
    public static String getDurationKey(String triggerName) {
        return new StringBuilder(triggerName).append(".").append("duration").toString();
    }
    
    private static long getIntervalNextRuntime(long lastRuntime, long remaining) {
        long elapsed = (lastRuntime >= 0) ? System.currentTimeMillis() - lastRuntime : 0;
        remaining -= elapsed;
        if (remaining < 0) { 
           return 0;
        }
        
        return remaining;
    }
    
    private static long getTimeToNextFire(Set<ScheduleExpression> expressions) {        
        return ScheduleUtil.getNextFireDate(expressions).getTime() - System.currentTimeMillis();
    }
    
    
}