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
package com.solers.delivery.web.util;

import java.util.Iterator;
import java.util.Map;

import junit.framework.TestCase;

import com.solers.util.unit.TimeIntervalUnit;

public class TimeToLiveMapTestCase extends TestCase {
    
    public void testExpireKey() throws Exception {
        
        TimeToLiveMap<String,String> map = new TimeToLiveMap<String,String>(Long.MAX_VALUE);
        map.put("test", "value");
        
        assertEquals( "value", map.get("test") );
        
        map.expireKey("test", TimeIntervalUnit.MILLISECONDS, 500);
        
        Thread.sleep(700);
        
        assertNull( map.get("test") );
        assertEquals(0, map.size());
    }
    
    public void testGet() throws Exception {
        Map<String,String> map = new TimeToLiveMap<String,String>(new Long(500));
        
        map.put("test", "value");
        
        assertEquals( "value", map.get("test") );
        
        Thread.sleep(200);
        
        assertEquals( "value", map.get("test") );
        
        Thread.sleep(600);
        
        assertNull( map.get("test") );
        assertEquals(0, map.size());
    }
    
    public void testEntrySet() throws Exception {
        Map<String,String> map = new TimeToLiveMap<String,String>(new Long(500));
        
        map.put("test", "value");
        
        for (Iterator<Map.Entry<String,String>> i = map.entrySet().iterator(); i.hasNext();) {
            Map.Entry<String,String> e = i.next();
            assertEquals("test", e.getKey());
            assertEquals("value", e.getValue());
            assertFalse(i.hasNext());
        }
        
        Thread.sleep(600);
        
        for (Iterator<Map.Entry<String,String>> i = map.entrySet().iterator(); i.hasNext();) {
            fail("The key should have expired");
        }
    }
    
    public void testValues() throws Exception {
        Map<String,String> map = new TimeToLiveMap<String,String>(new Long(500));
        
        map.put("test", "value");
        
        for (Iterator<String> i = map.values().iterator(); i.hasNext();) {
            assertEquals("value", i.next());
            assertFalse(i.hasNext());
        }
        
        Thread.sleep(600);
        
        assertFalse (map.keySet().iterator().hasNext());
    }
    
    public void testContainsKey() throws Exception {
        Map<String,String> map = new TimeToLiveMap<String,String>(new Long(500));
        
        map.put("test", "value");
        assertTrue( map.containsKey("test") );
        Thread.sleep(600);
        assertFalse( map.containsKey("test") );
    }
    
    public void testContainsValue() throws Exception {
        Map<String,String> map = new TimeToLiveMap<String,String>(new Long(500));
        
        map.put("test", "value");
        assertTrue( map.containsValue("value") );
        Thread.sleep(600);
        assertFalse( map.containsValue("value") );
    }
    
    public void testKeySet() throws Exception {
        Map<String,String> map = new TimeToLiveMap<String,String>(new Long(500));
        
        map.put("test", "value");
        
        for (Iterator<String> i = map.keySet().iterator(); i.hasNext();) {
            assertEquals("test", i.next());
            assertFalse(i.hasNext());
        }
        
        Thread.sleep(600);
        
        assertFalse (map.keySet().iterator().hasNext());
    }
    
}
