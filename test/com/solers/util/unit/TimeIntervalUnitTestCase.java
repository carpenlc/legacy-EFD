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
package com.solers.util.unit;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class TimeIntervalUnitTestCase extends TestCase {
    
    public void testToMillis() {
        assertEquals(1, TimeIntervalUnit.MILLISECONDS.toMillis(1));
        assertEquals(1000, TimeIntervalUnit.SECONDS.toMillis(1));
        assertEquals(60000, TimeIntervalUnit.MINUTES.toMillis(1));
        assertEquals(3600000, TimeIntervalUnit.HOURS.toMillis(1));
        assertEquals(86400000, TimeIntervalUnit.DAYS.toMillis(1));
    }
    
    public void testFormatZero() {
        assertTrue(TimeIntervalUnit.format(0).length() > 0);
    }
    
    public void testToValue() {
        assertNull(TimeIntervalUnit.toValue(null));
        assertNull(TimeIntervalUnit.toValue(""));
    }
}
