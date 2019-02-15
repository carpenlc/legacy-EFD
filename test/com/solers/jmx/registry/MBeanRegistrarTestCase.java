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
package com.solers.jmx.registry;

import java.lang.management.ManagementFactory;

import javax.management.MXBean;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class MBeanRegistrarTestCase extends TestCase {
    
    MBeanRegistrar registrar;
    
    public void setUp() {
        registrar = new MBeanRegistrar(ManagementFactory.getPlatformMBeanServer());
    }
    
    public void testRegister() {
        registrar.registerManagedBean(new TestBeanImpl(), "test", "MBeanRegistrarTestCase.testRegister");
        TestBean bean = registrar.getManagedBean(TestBean.class, "MBeanRegistrarTestCase.testRegister");
        assertNotNull(bean);
        assertEquals("test", bean.getMessage());
    }
    
    public void testGetObjectThatDoesntExist() {
        TestBean bean = registrar.getManagedBean(TestBean.class, "MBeanRegistrarTestCase.testGetObjectThatDoesntExist");
        assertNull(bean);
    }
    
    @MXBean
    public interface TestBean {
        String getMessage();
    }
    
    public static class TestBeanImpl implements TestBean {
        public String getMessage() {
            return "test";
        }
    }
}
