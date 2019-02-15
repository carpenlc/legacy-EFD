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
package com.solers.delivery.inventory.plugin.provider;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import com.solers.delivery.inventory.plugin.PluginException;

public class InventoryParameterTest extends TestCase {
    public void testPrimitiveToWrapperKnownClasses() {
        InventoryParameter p = new InventoryParameter("default", String.class, false, false, "");
        assertEquals(Character.class, p.primitiveToWrapper(char.class));
        assertEquals(Short.class, p.primitiveToWrapper(short.class));
        assertEquals(Long.class, p.primitiveToWrapper(long.class));
        assertEquals(Boolean.class, p.primitiveToWrapper(boolean.class));
        assertEquals(Integer.class, p.primitiveToWrapper(int.class));
        assertEquals(Float.class, p.primitiveToWrapper(float.class));
        assertEquals(Double.class, p.primitiveToWrapper(double.class));
        assertEquals(Byte.class, p.primitiveToWrapper(byte.class));
    }
    
    public void testPrimitiveToWrapperUnknownClasses() {
        InventoryParameter p = new InventoryParameter("default", String.class, false, false, "");
        assertNull(p.primitiveToWrapper(String.class));
        assertNull(p.primitiveToWrapper(Object.class));
        assertNull(p.primitiveToWrapper(TestCase.class));
    }
    
    public void testParseDouble() {
        InventoryParameter p = new InventoryParameter("default", String.class, false, false, "");
        double d = 1.2387d;
        Object o_wrapper = p.attemptParse(Double.class, Double.toString(d));
        Object o_primitv = p.attemptParse(double.class, Double.toString(d));
        assertEquals(d, o_wrapper);
        assertEquals(d, o_primitv);
    }
    
    public void testParseInteger() {
        InventoryParameter p = new InventoryParameter("default", String.class, false, false, "");
        int d = 2347;
        Object o_wrapper = p.attemptParse(Integer.class, Integer.toString(d));
        Object o_primitv = p.attemptParse(int.class, Integer.toString(d));
        assertEquals(d, o_wrapper);
        assertEquals(d, o_primitv);
    }
    
    public void testParseCharacter() {
        InventoryParameter p = new InventoryParameter("default", String.class, false, false, "");
        char d = 'e';
        Object o_wrapper = p.attemptParse(Character.class, Character.toString(d));
        Object o_primitv = p.attemptParse(char.class, Character.toString(d));
        assertEquals(d, o_wrapper);
        assertEquals(d, o_primitv);
    }
    
    public void testParseShort() {
        InventoryParameter p = new InventoryParameter("default", String.class, false, false, "");
        short d = 187;
        Object o_wrapper = p.attemptParse(Short.class, Short.toString(d));
        Object o_primitv = p.attemptParse(short.class, Short.toString(d));
        assertEquals(d, o_wrapper);
        assertEquals(d, o_primitv);
    }
    
    public void testParseFloat() {
        InventoryParameter p = new InventoryParameter("default", String.class, false, false, "");
        float d = 1.2387f;
        Object o_wrapper = p.attemptParse(Float.class, Float.toString(d));
        Object o_primitv = p.attemptParse(float.class, Float.toString(d));
        assertEquals(d, o_wrapper);
        assertEquals(d, o_primitv);
    }
    
    public void testParseLong() {
        InventoryParameter p = new InventoryParameter("default", String.class, false, false, "");
        long d = 123873222l;
        Object o_wrapper = p.attemptParse(Long.class, Long.toString(d));
        Object o_primitv = p.attemptParse(long.class, Long.toString(d));
        assertEquals(d, o_wrapper);
        assertEquals(d, o_primitv);
    }
    
    public void testParseByte() {
        InventoryParameter p = new InventoryParameter("default", String.class, false, false, "");
        byte d = 55;
        Object o_wrapper = p.attemptParse(Byte.class, Byte.toString(d));
        Object o_primitv = p.attemptParse(byte.class, Byte.toString(d));
        assertEquals(d, o_wrapper);
        assertEquals(d, o_primitv);
    }
    
    public void testParseBoolean() {
        InventoryParameter p = new InventoryParameter("default", String.class, false, false, "");
        boolean d = true;
        Object o_wrapper = p.attemptParse(Boolean.class, Boolean.toString(d));
        Object o_primitv = p.attemptParse(boolean.class, Boolean.toString(d));
        assertEquals(d, o_wrapper);
        assertEquals(d, o_primitv);
    }
    
    public void testMandatoryParameter() {
        InventoryParameter mandatory = new InventoryParameter("param", String.class, false, true, "");
        InventoryParameter notMandatory = new InventoryParameter("param", String.class, false, false, "");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("notParam", "test");
        params.put("another", 1.2f);
        try {
            assertNull(notMandatory.verifyAndRetrieve(params));
        } catch (PluginException pe) {
            fail();
        }
        
        try {
            mandatory.verifyAndRetrieve(params);
            fail("should fail on mandatory fields");
        } catch (PluginException pe) {
            
        }
    }
    
    public void testVerifyAndRetrieve() {
        String param = "p";
        InventoryParameter p = new InventoryParameter(param, Integer.class, false, false, "");
        Map<String, Object> params = new HashMap<String, Object>();
        int source = 12345;
        params.put(param, source);
        try {
            assertEquals(source, p.verifyAndRetrieve(params));
        } catch (PluginException pe) {
            fail();
        }
    }
    
    //When the target is type String, we use a toString on the parameter object to
    //get its value instead of rejecting it.
    public void testToString() {
        InventoryParameter allow = new InventoryParameter("p", String.class, false, false, "");
        InventoryParameter deny = new InventoryParameter("p", String.class, false, false, false, "");
        Object o = new Object();
        String name = o.toString();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("p", o);
        try {
            assertEquals(name, allow.verifyAndRetrieve(params));
        } catch (PluginException pe) {
            fail();
        }
        
        try {
            assertNull(deny.verifyAndRetrieve(params));
            fail();
        } catch (PluginException pe) {
            
        }
    }
}
