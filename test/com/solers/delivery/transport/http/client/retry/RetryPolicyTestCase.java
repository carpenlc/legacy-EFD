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
package com.solers.delivery.transport.http.client.retry;

import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.junit.Test;


public class RetryPolicyTestCase {
    
    @Test
    public void testNegativeDelayTime() throws InterruptedException {
        int[] delayTimes = {-1};
        try {
            new RetryPolicyLinearTime(5, TimeUnit.MILLISECONDS, delayTimes, TimeUnit.MILLISECONDS);
            Assert.fail();
        } catch (IllegalArgumentException e) {
        }
    }
    
    @Test
    public void testZeroDelayTime() throws InterruptedException {
        int[] delayTimes = {0};
        try {
            new RetryPolicyLinearTime(5, TimeUnit.MILLISECONDS, delayTimes, TimeUnit.MILLISECONDS);
            Assert.fail();
        } catch (IllegalArgumentException e) {
        }
    }
    
    @Test
    public void testSingleDelayTime() throws InterruptedException {
        int[] delayTimes = {5};
        RetryPolicy rp = new RetryPolicyLinearTime(6, TimeUnit.MILLISECONDS, delayTimes, TimeUnit.MILLISECONDS);
        Assert.assertTrue(rp.isEnabled());
        rp.execute();
        Assert.assertTrue(rp.isEnabled());
    }
    
    @Test
    public void testSingleDelayTime2() throws InterruptedException {
        int[] delayTimes = {7};
        RetryPolicy rp = new RetryPolicyLinearTime(6, TimeUnit.MILLISECONDS, delayTimes, TimeUnit.MILLISECONDS);
        Assert.assertTrue(rp.isEnabled());
        rp.execute();
        Assert.assertFalse(rp.isEnabled());
    }
    
    @Test
    public void testDisable() throws InterruptedException {
        int[] delayTimes = {5};
        RetryPolicy rp = new RetryPolicyLinearTime(6, TimeUnit.MILLISECONDS, delayTimes, TimeUnit.MILLISECONDS);
        Assert.assertTrue(rp.isEnabled());
        rp.disable();
        Assert.assertFalse(rp.isEnabled());
    }
    
    @Test
    public void testDisableMixedTimeUnit() throws InterruptedException {
        int[] delayTimes = {500};
        RetryPolicy rp = new RetryPolicyLinearTime(1, TimeUnit.SECONDS, delayTimes, TimeUnit.MILLISECONDS);
        Assert.assertTrue(rp.isEnabled());
        rp.disable();
        Assert.assertFalse(rp.isEnabled());
    }
    
    @Test
    public void testMaxTimeLessThanSumOfDelayTimes() throws InterruptedException {
        // Will always execute at least once, even if first delay time is greater than max
        int totalDelaytime = 5;
        int[] delayTimes = {totalDelaytime};
        RetryPolicy rp = new RetryPolicyLinearTime(totalDelaytime-1, TimeUnit.MILLISECONDS, delayTimes, TimeUnit.MILLISECONDS);
        rp.execute();
        Assert.assertFalse(rp.isEnabled());
    }
    
    @Test
    public void testMaxTimeLessThanSumOfDelayTimesMixedTimeUnit() throws InterruptedException {
        // Will always execute at least once, even if first delay time is greater than max
        int[] delayTimes = {1100};
        RetryPolicy rp = new RetryPolicyLinearTime(1, TimeUnit.SECONDS, delayTimes, TimeUnit.MILLISECONDS);
        rp.execute();
        Assert.assertFalse(rp.isEnabled());
    }
    
    @Test
    public void testGoThroughAllDelayTimes() throws InterruptedException {
        
        int[] delayTimes = {5,10,15};
        RetryPolicy rp = new RetryPolicyLinearTime(30, TimeUnit.MILLISECONDS, delayTimes, TimeUnit.MILLISECONDS);
        
        for(int i=0;i<=delayTimes.length; i++) {
            rp.execute();
        }
        Assert.assertFalse(rp.isEnabled());  
   }
    
    @Test
    public void testGoThroughAllDelayTimesMixedTimeUnits() throws InterruptedException {
        
        int[] delayTimes = {100,200,300,400};
        RetryPolicy rp = new RetryPolicyLinearTime(1, TimeUnit.SECONDS, delayTimes, TimeUnit.MILLISECONDS);
        
        for(int i=0;i<=delayTimes.length; i++) {
            rp.execute();
        }
        Assert.assertFalse(rp.isEnabled());  
   }
    
    @Test
    public void testInterruptionPostExecute() throws InterruptedException {
        int[] delayTimes = {1};
        final RetryPolicy rp = new RetryPolicyLinearTime(1, TimeUnit.SECONDS, delayTimes, TimeUnit.SECONDS);
        
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    rp.execute();
                    Assert.fail();
                } catch (InterruptedException e) {
                }
            }
        });
        t.start();
        Thread.sleep(500);
        t.interrupt();
        t.join();
        Assert.assertFalse(rp.isEnabled());
        Assert.assertEquals(1, rp.getRetryCount());
    }
    
    @Test
    public void testInterruptionPreExecute()  throws InterruptedException {
        int[] delayTimes = {1};
        final RetryPolicy rp = new RetryPolicyLinearTime(2, TimeUnit.SECONDS, delayTimes, TimeUnit.SECONDS);
        
        Thread t = new Thread(new Runnable() {
            public void run() {
                Thread.currentThread().interrupt();
                try {
                    rp.execute();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        t.start();
        t.join();
        Assert.assertFalse(rp.isEnabled());
        Assert.assertEquals(0, rp.getRetryCount());
    }
}
