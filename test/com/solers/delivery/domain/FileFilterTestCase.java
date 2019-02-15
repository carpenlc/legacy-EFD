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
package com.solers.delivery.domain;

import junit.framework.TestCase;

import com.solers.delivery.domain.FileFilter.Pattern;

public class FileFilterTestCase extends TestCase {
    
    public void testEquals() {
        assertEquals(new FileFilter(), new FileFilter());
        assertEquals(new FileFilter("abc", Pattern.BEGINS), new FileFilter("abc", Pattern.BEGINS));
        assertFalse(new FileFilter("abc", Pattern.BEGINS).equals(new FileFilter("abc", Pattern.ENDS)));
        assertFalse(new FileFilter("abc", Pattern.BEGINS).equals(new FileFilter("abcd", Pattern.BEGINS)));
    }
    
    public void testHashCode() {
        assertEquals(0, new FileFilter().hashCode());
    }
}
