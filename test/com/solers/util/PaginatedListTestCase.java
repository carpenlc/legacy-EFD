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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class PaginatedListTestCase {
    
    @Test
    public void testIsEmpty() {
        PaginatedList<Integer> list = new MockPaginatedList(2, 6);
        
        try {
            list.isEmpty();
            Assert.fail();
        } catch (IllegalStateException expected) {
            
        }
        
        list.initialize();
        Assert.assertFalse(list.isEmpty());
        
        list = new MockPaginatedList(2, 0);
        list.initialize();
        Assert.assertTrue(list.isEmpty());
    }
    
    @Test
    public void testIterator() {
        
        PaginatedList<Integer> list = new MockPaginatedList(2, 6);
        list.initialize();
        
        List<Integer> expected = Arrays.asList(0,1,0,1,0,1);
        
        assertEquals(expected.iterator(), list.iterator());
    }
    
    @Test
    public void testEmptySubList() {
        PaginatedList<String> empty = PaginatedList.emptyList();
        
        Assert.assertEquals(0, empty.subList(0).size());
    }
    
    @Test
    public void testSubList() {
        PaginatedList<Integer> list = new MockPaginatedList(3, 6);
        list.initialize();
        
        List<Integer> actual = list.subList(2);
        List<Integer> expected = Arrays.asList(0,1,2);
        
        assertEquals(expected.iterator(), actual.iterator());
    }
    
    protected void assertEquals(Iterator<?> expected, Iterator<?> actual) {
        while(expected.hasNext()) {
            Assert.assertEquals(expected.next(), actual.next());
        }
        Assert.assertFalse(actual.hasNext());
    }
    
    class MockPaginatedList extends PaginatedList<Integer> {
        
        int totalSize;
        MockPaginatedList(int pageSize, int totalSize) {
            super(pageSize);
            this.totalSize = totalSize;
        }
        
        /**
         * @see com.solers.util.PaginatedList#initialize()
         */
        @Override
        public void initialize() {
            initialized(totalSize);
        }

        /**
         * @see com.solers.util.PaginatedList#getSubList(int, int)
         */
        @Override
        public List<Integer> getSubList(int fromIndex, int toIndex) {
            List<Integer> result = new ArrayList<Integer>(getPageSize());
            
            for (int i=0; i < getPageSize(); i++) {
                result.add(i);
            }
            return result;
        }

        
    }
    
}
