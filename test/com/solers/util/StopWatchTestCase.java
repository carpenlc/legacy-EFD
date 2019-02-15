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
package com.solers.util;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class StopWatchTestCase extends TestCase {
    
    public void testChainedMethods() {
        StopWatch watch = new StopWatch();
        
        assertSame(watch, watch.start());
        assertSame(watch, watch.stop());
    }

    public void testElapsedTimeStaysTheSameAfterStop() throws Exception {
        StopWatch watch = new StopWatch();
        watch.start();

        Thread.sleep(25);

        watch.stop();

        long time = watch.getElapsedTime();

        Thread.sleep(25);

        assertEquals(time, watch.getElapsedTime());
    }

    public void testStartTimeState() {
        StopWatch watch = new StopWatch();
        try {
            watch.getStartTime();
            fail();
        } catch (IllegalStateException expected) {

        }
    }

    public void testIsStarted() {
        StopWatch watch = new StopWatch();
        assertFalse(watch.isStarted());
        watch.start();
        assertTrue(watch.isStarted());
    }
}
