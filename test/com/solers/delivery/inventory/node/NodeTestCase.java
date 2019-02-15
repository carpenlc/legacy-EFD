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
package com.solers.delivery.inventory.node;

import junit.framework.TestCase;

import com.solers.delivery.inventory.MockNode;


public class NodeTestCase extends TestCase {
    public void testFileSizeComparisons() {
        String path = "test";
        boolean directory = false;
        MockNode n1 = new MockNode(path, directory);
        MockNode n2 = new MockNode(path, directory);
        n2.setTimestamp(n1.getTimestamp());
        
        assertEquals(0, n1.getSize());
        assertEquals(0, n2.getSize());
        assertEquals(n1, n2);
        assertTrue(n1.sizeEquals(n2.getSize()));
        
        long size1 = (long) Math.random() * Long.MAX_VALUE;
        long size2 = (long) Math.random() * Long.MAX_VALUE;
        if (size1 == size2) size1--;
        assertFalse(size1 == size2);
        n1.setSize(size1);
        n2.setSize(size2);
        assertFalse(n1.equals(n2));
        assertFalse(n1.sizeEquals(n2.getSize()));

        size2 = size1;
        n1.setSize(size1);
        n2.setSize(size2);
        
        assertTrue(n1.sizeEquals(n2.getSize()));
        assertEquals(n1, n2);
        
        n1.setTimestamp(0);
        assertFalse(n1.equals(n2));
        assertTrue(n1.sizeEquals(n2.getSize()));
    }
    
    public void testFileTimestampComparisons() {
        int errorMargin = AbstractNode.DEFAULT_ACCURACY;
        String path = "mock";
        MockNode n1 = new MockNode(path, false);
        MockNode n2 = new MockNode(path, false);
        n2.setTimestamp(n1.getTimestamp());
        assertEquals(n2.getTimestamp(), n1.getTimestamp());
        assertTrue(n2.timestampEquals(n1.getTimestamp(), errorMargin));
        assertEquals(n1, n2);
        
        long fakeStamp = errorMargin * 20;
        long s1 = fakeStamp;
        long s2 = fakeStamp + (errorMargin / 2);
        assertFalse(s1 == s2);
        
        n1.setTimestamp(s1);
        n2.setTimestamp(s2);
        assertTrue(n1.timestampEquals(n2.getTimestamp(), errorMargin));
        assertFalse(n2.getTimestamp() == n1.getTimestamp());
        assertEquals(n1, n2);
        
        s2 += errorMargin;
        n1.setTimestamp(s1);
        n2.setTimestamp(s2);
        assertFalse(n1.timestampEquals(n2.getTimestamp(), errorMargin));
        assertFalse(n2.getTimestamp() == n1.getTimestamp());
        assertFalse(n1.equals(n2));
    }
    
    public void testDirectorySizeComparisons() {
        String path = "mock";
        MockNode n1 = new MockNode(path, true);
        MockNode n2 = new MockNode(path, true);
        assertTrue(n1.sizeEquals(n2.getSize()));
        assertEquals(n1, n2);
        n2.setSize(System.currentTimeMillis());
        assertTrue(n1.sizeEquals(n2.getSize()));
        assertEquals(n1, n2);
    }
    
    public void testNodeCanCustomizeAccuracy() {
        AbstractNode n4k = new MockNode(4000);
        AbstractNode n1k = new MockNode(1000);
        
        assertEquals(4000, n4k.getTimestampAccuracy());
        assertEquals(1000, n1k.getTimestampAccuracy());
        assertEquals(4000, n4k.getTimestampAccuracy(n1k));
        assertEquals(4000, n1k.getTimestampAccuracy(n4k));
    }
    
    public void testDefaultAccuracyValue() {
        AbstractNode n = new MockNode("root", true);
        assertEquals(AbstractNode.DEFAULT_ACCURACY, n.getTimestampAccuracy());
    }
    
    public void testVariableAccuracyComparison() {
        MockNode n1 = new MockNode(1000);
        MockNode n2 = new MockNode(1000);
        n1.setTimestamp(104000);
        n2.setTimestamp(104000);
        //nodes here are exactly the same
        assertEquals(n1, n2);
        
        //vary the timestamp by 1000 in one, nodes should not be equal
        n2.setTimestamp(105000);
        assertFalse(n1.equals(n2));
        
        //set n1's accuracy to 2000ms, nodes should be equal now
        n1.setTimestampAccuracy(2000);
        assertEquals(n1, n2);
        
        //prove that it doesn't matter which one has the accuracy set.
        n1.setTimestampAccuracy(1000);
        n2.setTimestampAccuracy(2000);
        assertEquals(n1, n2);
    }
    
    public void testDirectoryTimestampComparisons() {
        int errorMargin = AbstractNode.DEFAULT_ACCURACY;
        String path = "mock";
        MockNode n1 = new MockNode(path, true);
        MockNode n2 = new MockNode(path, true);
        n2.setTimestamp(n1.getTimestamp());
        assertEquals(n2.getTimestamp(), n1.getTimestamp());
        assertTrue(n2.timestampEquals(n1.getTimestamp(), errorMargin));
        assertEquals(n1, n2);
        
        long fakeStamp = errorMargin * 20;
        long s1 = fakeStamp;
        long s2 = fakeStamp + (errorMargin / 2);
        assertFalse(s1 == s2);
        
        n1.setTimestamp(s1);
        n2.setTimestamp(s2);
        assertTrue(n1.timestampEquals(n2.getTimestamp(), errorMargin));
        assertFalse(n2.getTimestamp() == n1.getTimestamp());
        assertEquals(n1, n2);
        
        s2 += errorMargin;
        n1.setTimestamp(s1);
        n2.setTimestamp(s2);
        assertFalse(n1.timestampEquals(n2.getTimestamp(), errorMargin));
        assertFalse(n2.getTimestamp() == n1.getTimestamp());
        assertEquals(n1, n2);       
    }
    
    public void testFileDirectoryMixComparisons() {
        String name = "mock";
        MockNode n1 = new MockNode(name, false);
        MockNode n2 = new MockNode(name, false);
        n2.setTimestamp(n1.getTimestamp());
        assertEquals(n1, n2);
        n1.setDirectory(true);
        assertFalse(n1.equals(n2));
        n2.setDirectory(true);
        assertEquals(n1, n2);
        n1.setDirectory(false);
        assertFalse(n1.equals(n2));
    }
    
    public void testNameComparisons() {
        String name1 = "mock";
        String name2 = "node";
        MockNode n1 = new MockNode(name1, false);
        MockNode n2 = new MockNode(name1, false);
        MockNode n3 = new MockNode(name2, false);
        n2.setTimestamp(n1.getTimestamp());
        n3.setTimestamp(n1.getTimestamp());
        assertTrue(n3.getTimestamp() == n2.getTimestamp());
        assertEquals(0, n1.getSize());
        assertEquals(0, n2.getSize());
        assertEquals(0, n3.getSize());
        
        assertEquals(n1, n2);
        assertFalse(n2.equals(n3));
        assertFalse(n1.equals(n3));
        n1.setDirectory(true);
        assertFalse(n1.equals(n2));
        assertFalse(n1.equals(n3));
        n2.setDirectory(true);
        assertEquals(n1, n2);
        assertFalse(n2.equals(n3));
        n3.setDirectory(true);
        assertFalse(n1.equals(n3));
        assertFalse(n2.equals(n3));
    }
    
    public void testDirectorySizeIsZero() {
        MockNode n = new MockNode("mock", false);
        assertEquals(0, n.getSize());
        n.setSize(System.currentTimeMillis());
        assertFalse(n.getSize() == 0);
        n.setDirectory(true);
        assertEquals(0, n.getSize());
    }
}
