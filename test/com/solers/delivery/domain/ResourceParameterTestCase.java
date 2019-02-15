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

public class ResourceParameterTestCase extends TestCase {
    public void testUnencrypted() {
        ResourceParameter p = new ResourceParameter();
        String test = "test";
        assertFalse(p.isEncrypted());
        p.setValue(test);
        assertEquals(test, p.getPersistedValue());
        assertEquals(p.getValue(), p.getPersistedValue());
    }
    
    public void testEncrypted() {
        ResourceParameter p = new ResourceParameter();
        String test = "test";
        p.setEncrypted(true);
        assertTrue(p.isEncrypted());
        p.setValue(test);
        assertFalse(test.equals(p.getPersistedValue()));
        assertFalse(p.getValue().equals(p.getPersistedValue()));
        assertEquals(test, p.getValue());
    }
    
    public void testToggle() {
        ResourceParameter p = new ResourceParameter();
        String test = "testValue";
        assertFalse(p.isEncrypted());
        p.setValue(test);
        assertEquals(test, p.getPersistedValue());
        assertEquals(p.getValue(), p.getPersistedValue());
        
        p.setEncrypted(true);
        assertTrue(p.isEncrypted());
        assertFalse(test.equals(p.getPersistedValue()));
        assertFalse(p.getValue().equals(p.getPersistedValue()));
        assertEquals(test, p.getValue());
        
        p.setEncrypted(false);
        assertEquals(test, p.getPersistedValue());
        assertEquals(p.getValue(), p.getPersistedValue());
    }
}
