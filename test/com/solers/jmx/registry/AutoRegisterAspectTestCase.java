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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class AutoRegisterAspectTestCase {
    
    MockRegistrar registrar;
    AutoRegisterAspect aspect;
    
    @Before
    public void setUp() {
        registrar = new MockRegistrar();
        aspect = new AutoRegisterAspect();
        aspect.setRegistrar(registrar);
    }
    
    @Test
    public void testMockRegister() {
        TestObj t = new TestObj();
        aspect.register(t);
        
        Assert.assertSame(t, registrar.obj);
        Assert.assertEquals("test", registrar.type);
        Assert.assertEquals(new Long(1l), registrar.name);
    }
    
    @Test
    public void testRegisterObjectWithoutAnnotation() {
        try {
            aspect.register("foo");
            Assert.fail();
        } catch (IllegalArgumentException expected) {
            
        }
    }
    
    @Test
    public void testRegisterObjectWithoutObjectName() {
        Object o = new TestObjWithoutName();
        aspect.register(o);
        Assert.assertSame(o, registrar.obj);
        Assert.assertEquals("test", registrar.type);
        Assert.assertEquals("TestObjWithoutName", registrar.name); 
    }
    
    @Test
    public void testRegisterInJmx() {
        MBeanRegistrar mbeanRegistrar = new MBeanRegistrar(ManagementFactory.getPlatformMBeanServer());
        aspect.setRegistrar(mbeanRegistrar);
        
        FooImpl obj = new FooImpl();
        
        aspect.register(obj);
        
        Foo foo = mbeanRegistrar.getManagedBean(Foo.class, "testRegisterInJmx");
        
        Assert.assertNotNull(foo);
        Assert.assertEquals("foo", foo.foo());
    }
    
    @AutoRegister(category="test")
    class FooImpl implements Foo {
        
        public String foo() {
            return "foo";
        }
        
        @AutoRegisterObjectName
        public String key() {
            return "testRegisterInJmx";
        }
    }
    
    @MXBean
    public interface Foo {
        String foo();
    }
    
    @AutoRegister(category="test")
    class TestObjWithoutName {
        
        public Long getId() {
            return 1l;
        }
    }
    
    @AutoRegister(category="test")
    class TestObj {
        
        @AutoRegisterObjectName
        public Long getId() {
            return 1l;
        }
    }
    
    class MockRegistrar implements Registrar {

        Object obj;
        String type;
        Object name;
        
        @Override
        public <M> M getManagedBean(Class<? extends M> type, Object key) {
            return null;
        }

        @Override
        public void registerManagedBean(Object object, String typeCategory, Object key) {
            obj = object;
            type = typeCategory;
            name = key;
        }
        
    }
}
