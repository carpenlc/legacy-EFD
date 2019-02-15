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
package com.solers.delivery.util;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class UUIDTestCase extends TestCase {
    
    /**
     * This really isn't very good but its better than nothing
     */
    public void testNewSyncKey() {
        int times = 100000;
        Map<String, String> cache = new HashMap<String, String>(times);
        
        for (int i=0; i < times; i++) {
            String generated = UUID.newSyncKey();
            if (cache.containsKey(generated)) {
                fail(generated+" was duplicated");
            } else {
                cache.put(generated, generated);
            }
        }
    }
    
}
