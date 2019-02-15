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
package com.solers.util.spring;

import org.junit.Assert;
import org.junit.Test;

import com.solers.util.unit.TimeIntervalUnit;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class TimeIntervalUnitFactoryBeanTestCase {
    
    @Test
    public void testGetObjectWithDefaultSourceUnit() throws Exception {
        TimeIntervalUnitFactoryBean factory = new TimeIntervalUnitFactoryBean();
        factory.setSourceUnit(TimeIntervalUnit.HOURS);
        factory.setValue(3);
        
        Assert.assertEquals(TimeIntervalUnit.HOURS.toMillis(3), factory.getObject());
        Assert.assertEquals(long.class, factory.getObjectType());
        Assert.assertFalse(factory.isSingleton());
    }
    
    @Test
    public void testGetObjectWithSourceUnit() throws Exception {
        TimeIntervalUnitFactoryBean factory = new TimeIntervalUnitFactoryBean();
        factory.setSourceUnit(TimeIntervalUnit.HOURS);
        factory.setDestUnit(TimeIntervalUnit.DAYS);
        factory.setValue(48);
        
        Assert.assertEquals(2L, factory.getObject());
        Assert.assertEquals(long.class, factory.getObjectType());
        Assert.assertFalse(factory.isSingleton());
    }
}
