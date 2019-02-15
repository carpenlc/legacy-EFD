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
package com.solers.security.password;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class PasswordFactoryBeanTestCase {
    
    private PasswordProvider provider;
    private PasswordFactoryBean factory;
    
    
    @Before
    public void setUp() {
        provider = new MapPasswordProvider();
        factory = new PasswordFactoryBean();
        factory.setProvider(provider);
    }
    
    @Test
    public void testSetKey() throws Exception {
        String key = "testSetKey";
        String value = "testSetKeyValue";
        
        provider.setPassword(key, value);
        factory.setKey(key);
        
        Assert.assertEquals(value, factory.getObject());
        Assert.assertEquals(String.class, factory.getObjectType());
    }
    
    @Test
    public void testSetEnumKey() throws Exception {
        String value = "testSetEnumKey";
        factory.setEnumKey(TestEnum.class.getName()+".VALUE");
        
        provider.setPassword(TestEnum.VALUE.toString(), value);
        
        Assert.assertEquals(value, factory.getObject());
        Assert.assertEquals(String.class, factory.getObjectType());
    }
    
    @Test
    public void testSetFieldKey() throws Exception {
        String value = "testSetEnumKey";
        factory.setFieldKey(TestClass.class.getName()+".FIELD");
        
        provider.setPassword(TestClass.FIELD.toString(), value);
        
        Assert.assertEquals(value, factory.getObject());
        Assert.assertEquals(String.class, factory.getObjectType());
    }
    
    private static class TestClass {
        public static String FIELD = "foo";
    }
    
    private enum TestEnum {
        VALUE;
        
        @Override
        public String toString() {
            return "enumValue";
        }
    }
}
