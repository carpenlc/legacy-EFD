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
package com.solers.delivery.inventory.index;

import junit.framework.TestCase;

import com.solers.util.HashFunction;

public class HashUtilTest extends TestCase {
    public void testIndex() {
        HashFunction hash = HashFunction.FNV;
        int buckets = 73;
        String[] values = new String[] {
          "aroecTohuhCG208",
          "gregWasHere",
          "Solers",
          "filefilefile.txt.txt.txt"
        };
        long[] expectedHashes = new long[] {
            -5163787755614146838l,
            1691262119302761536l,
            -8966218941912169208l,
            -7689249515207204272l
        };
        int[] indexes = new int[] {
            0,
            56,
            19,
            62
        };
        
        assertTrue((values.length == expectedHashes.length) && (expectedHashes.length == indexes.length));
        for (int i = 0; i < values.length; ++i) {
            assertTrue(hash.hash(values[i]) == expectedHashes[i]);
            assertTrue(HashUtil.index(values[i], hash, buckets) == indexes[i]);
        }
    }
    
    public void testSet() {
        int[] target = new int[5];
        int[] values = new int[]    { 1, 2, 3, 4, 5 };
        int[] positions = new int[] { 1, 2, 2, 0, 3 };
        int[] result =              { 4, 1, 2, 3, 5 };

        try {
            HashUtil.set(target, 0, 0);
            fail("Cannot accept zero values, since they are the 'special' value that demarcates an empty slot.");
        } catch (IllegalArgumentException iae) {
            //zero not allowed as value
        }
        
        assertTrue(target.length == values.length && values.length == positions.length && positions.length == result.length);
        for (int i : target) {
            assertTrue(i == 0);
        }
        for (int i : values) {
            assertFalse(i == 0);
        }
        for (int i = 0; i < target.length; ++i) {
            assertTrue("Entering a value must succeed here.", HashUtil.set(target, positions[i], values[i]));
        }
        for (int i = 0; i < target.length; ++i) {
            assertTrue(target[i] == result[i]);
        }
        assertFalse("Cannot enter values once array is full.", HashUtil.set(target, 0, 7));
    }
}
