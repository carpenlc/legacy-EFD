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

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.solers.delivery.domain.ScheduleExpression;

/** 
 * fmt: SEC MIN HOUR DAYOFMONTH MONTH DAYSWEEK YEAR
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class ScheduleUtilTestCase {
    
    @Test
    public void testGetNextFireDate() {
        ScheduleExpression expr = new ScheduleExpression(String.format("* * * * %s ? *", month(1)));
        Date date = ScheduleUtil.getNextFireDate(expr);
        
        Assert.assertEquals(month(1), month(date));
        
        Set<ScheduleExpression> exprs = new HashSet<ScheduleExpression>();
        exprs.add(new ScheduleExpression(String.format("* * * * %s ? *", month(5))));
        exprs.add(new ScheduleExpression(String.format("* * * * %s ? *", month(3))));
        exprs.add(new ScheduleExpression(String.format("* * * * %s ? *", month(7))));
        
        date = ScheduleUtil.getNextFireDate(exprs);
        
        Assert.assertEquals(month(3), month(date));
    }
    
    private int month(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.MONTH)+1;
    }
    
    private int month(int num) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.MONTH, c.get(Calendar.MONTH)+num);
        return c.get(Calendar.MONTH)+1;
    }
}
