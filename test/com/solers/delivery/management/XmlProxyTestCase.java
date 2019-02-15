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
package com.solers.delivery.management;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.solers.delivery.transport.http.TransferStatus;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class XmlProxyTestCase {

    @Test
    public void testGetName() {
        XmlProxy p = new XmlProxy("<empty/>");
        
        Assert.assertEquals("enabled", p.getName("isEnabled"));
        Assert.assertEquals("statusService", p.getName("getStatusService"));
    }
    
    @Test
    public void testIsReadable() {
        XmlProxy p = new XmlProxy("<empty/>");
        
        Assert.assertTrue(p.isReadable("isEnabled"));
        Assert.assertTrue(p.isReadable("getStatusService"));
        Assert.assertFalse(p.isReadable("readable"));
    }
    
    @Test
    public void testProxyBean() throws Exception {
        XmlProxy p = new XmlProxy("<root><property>4</property></root>");
        
        Foo foo = (Foo) p.proxyBean(FooImpl.class);
        
        Assert.assertEquals(4, foo.getProperty());
    }
    
    @Test
    public void testProxy() {
        String xml = 
        "<status>" +
            "<enumProperty>WARNING</enumProperty>" +
            "<stringProperty>200</stringProperty>" +
            "<boolean>true</boolean>" +
            "<double>2.5</double>" +
            "<int>100</int>" +
            "<long>1000</long>" +
            "<transferStatus>" +
            "    <status>" +
            "        <bytesTransferred>100000</bytesTransferred>" +
            "        <fileName>/path1/testfile1</fileName>" +
            "        <percentComplete>20.0</percentComplete>" +
            "    </status>" +
            "    <status>" +
            "        <bytesTransferred>500000</bytesTransferred>" +
            "        <fileName>/path5/testfile5</fileName>" +
            "        <percentComplete>50.0</percentComplete>" +
            "    </status>" +
            "</transferStatus>             " +
            "<foo><property>55555</property></foo>" +
            "<concreteObject>" +
            "<level>INFO</level>" +
            "<fieldOne>hello world</fieldOne><fieldTwo>444555</fieldTwo>" +
            "<nested><f3>33</f3></nested>" +
            "</concreteObject>" +
        "</status>";
        
        InvocationHandler p = new XmlProxy(xml);
        
        Status status = (Status) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {Status.class}, p);
        
        Assert.assertEquals("200", status.getStringProperty());
        Assert.assertTrue(status.isBoolean());
        Assert.assertEquals(2.5d, status.getDouble(), 0.0d);
        Assert.assertEquals(100, status.getInt());
        Assert.assertEquals(1000l, status.getLong());
        Assert.assertNull(status.unReadable());
        Assert.assertNotNull(status.getTransferStatus());
        Assert.assertEquals(2,status.getTransferStatus().size());
        Assert.assertEquals(100000,status.getTransferStatus().get(0).getBytesTransferred());
        Assert.assertEquals("/path1/testfile1",status.getTransferStatus().get(0).getFileName());
        Assert.assertEquals((float) 20.0, (float) status.getTransferStatus().get(0).getPercentComplete(), (float)0.0);
        Assert.assertEquals(500000,status.getTransferStatus().get(1).getBytesTransferred());
        Assert.assertEquals("/path5/testfile5",status.getTransferStatus().get(1).getFileName());
        Assert.assertEquals((float) 50.0, (float) status.getTransferStatus().get(1).getPercentComplete(), (float)0.0);
        Assert.assertEquals(55555, status.getFoo().getProperty());
        Assert.assertEquals("hello world", status.getConcreteObject().getFieldOne());
        Assert.assertEquals(444555, status.getConcreteObject().getFieldTwo());
        Assert.assertEquals(33, status.getConcreteObject().getNested().getF3());
        Assert.assertEquals(Level.WARNING, status.getEnumProperty());
        Assert.assertEquals(Level.INFO, status.getConcreteObject().getLevel());
    }

    interface Status {
        
        String getStringProperty();
        
        boolean isBoolean();
        
        long getLong();
        
        double getDouble();
        
        int getInt();
        
        String unReadable();

        List<TransferStatus> getTransferStatus();
        
        Foo getFoo();
        
        MyObject getConcreteObject();
        
        Level getEnumProperty();
        
    }
    
    enum Level {
        INFO, WARNING, ERROR
    }
    
    interface Foo {
        
        int getProperty();
    }
    
    static class FooImpl implements Foo {
        int property;

        @Override
        public int getProperty() {
            return property;
        }

        public void setProperty(int property) {
            this.property = property;
        }
    }
    
    static class MyObject {
        private String fieldOne;
        private long fieldTwo;
        private NestedObject nested;
        private Level level;
        
        public Level getLevel() {
            return level;
        }
        
        public void setLevel(Level arg) {
            level = arg;
        }
        
        public NestedObject getNested() {
            return nested;
        }
        public void setNested(NestedObject nested) {
            this.nested = nested;
        }
        public String getFieldOne() {
            return fieldOne;
        }
        public void setFieldOne(String fieldOne) {
            this.fieldOne = fieldOne;
        }
        public long getFieldTwo() {
            return fieldTwo;
        }
        public void setFieldTwo(long fieldTwo) {
            this.fieldTwo = fieldTwo;
        }
    }
    
    static class NestedObject {
        
        private int f3;

        public int getF3() {
            return f3;
        }

        public void setF3(int f3) {
            this.f3 = f3;
        }
    }
    
}
