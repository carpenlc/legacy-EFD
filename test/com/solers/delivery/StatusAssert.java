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
package com.solers.delivery;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.codehaus.jettison.json.JSONObject;

import com.solers.delivery.content.status.CurrentSynchronization;
import com.solers.delivery.transport.http.TransferStatus;
import com.solers.delivery.transport.http.TransferType;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class StatusAssert {
    
    public static <T> T proxy(Class<T> clazz) {
        return clazz.cast(Proxy.newProxyInstance(StatusAssert.class.getClassLoader(), new Class<?>[] {clazz}, new MockInvocationHandler()));
    }
    
    public static void assertSupplierJSON(String json) throws Exception {
        JSONObject obj = new JSONObject(json);
        JSONObject status = obj.getJSONObject("status");
        
        Assert.assertEquals("true", status.getString("enabled"));
        Assert.assertEquals("5", status.getString("nextEstimatedRuntime"));
        Assert.assertEquals("5", status.getString("itemCount"));
        Assert.assertEquals("5", status.getString("lastRuntime"));
        Assert.assertEquals("5", status.getString("size"));
    }
    
    public static void assertSupplierXML(String xml) throws Exception {
        XPathAssert.assertXPathNodeExists(xml, "//status");
        XPathAssert.assertXPathNodeValue(xml, "//status/enabled/text()", "true");
        XPathAssert.assertXPathNodeValue(xml, "//status/nextEstimatedRuntime/text()", "5");
        XPathAssert.assertXPathNodeValue(xml, "//status/itemCount/text()", "5");
        XPathAssert.assertXPathNodeValue(xml, "//status/lastRuntime/text()", "5");
        XPathAssert.assertXPathNodeValue(xml, "//status/size/text()", "5");
    }
    
    public static void assertConsumerJSON(String json) throws Exception {
        JSONObject obj = new JSONObject(json);
        JSONObject status = obj.getJSONObject("status");
        
        Assert.assertNotNull(status);
        Assert.assertEquals("5", status.getString("bytesAdded"));
        Assert.assertEquals("5", status.getString("bytesAddedRemaining"));
        Assert.assertEquals("5", status.getString("bytesDeleted"));
        Assert.assertEquals("5", status.getString("bytesDeletedRemaining"));
        Assert.assertEquals("5", status.getString("bytesUpdated"));
        Assert.assertEquals("5", status.getString("bytesUpdatedRemaining"));
        Assert.assertEquals("5", status.getString("elapsedTime"));
        Assert.assertEquals("true", status.getString("enabled"));
        Assert.assertEquals("5", status.getString("nextEstimatedRuntime"));
        Assert.assertEquals("5", status.getString("itemCount"));
        Assert.assertEquals("5", status.getString("itemsAdded"));
        Assert.assertEquals("5", status.getString("itemsAddedRemaining"));
        Assert.assertEquals("5", status.getString("itemsDeleted"));
        Assert.assertEquals("5", status.getString("itemsDeletedRemaining"));
        Assert.assertEquals("5", status.getString("itemsUpdated"));
        Assert.assertEquals("5", status.getString("itemsUpdatedRemaining"));
        Assert.assertEquals("5", status.getString("failures"));
        Assert.assertEquals("5", status.getString("bytesFailed"));
        Assert.assertEquals("5", status.getString("lastRuntime"));
        Assert.assertEquals("2.5", status.getString("percentCompleted"));
        Assert.assertEquals("5", status.getString("size"));
        Assert.assertEquals("test", status.getString("state"));
    }
    
    public static void assertConsumerXML(String xml) throws Exception {
        XPathAssert.assertXPathNodeExists(xml, "//status");
        XPathAssert.assertXPathNodeValue(xml, "//status/bytesAdded/text()", "5");
        XPathAssert.assertXPathNodeValue(xml, "//status/bytesAddedRemaining/text()", "5");
        XPathAssert.assertXPathNodeValue(xml, "//status/bytesDeleted/text()", "5");
        XPathAssert.assertXPathNodeValue(xml, "//status/bytesDeletedRemaining/text()", "5");
        XPathAssert.assertXPathNodeValue(xml, "//status/bytesUpdated/text()", "5");
        XPathAssert.assertXPathNodeValue(xml, "//status/bytesUpdatedRemaining/text()", "5");
        XPathAssert.assertXPathNodeValue(xml, "//status/elapsedTime/text()", "5");
        XPathAssert.assertXPathNodeValue(xml, "//status/enabled/text()", "true");
        XPathAssert.assertXPathNodeValue(xml, "//status/nextEstimatedRuntime/text()", "5");
        XPathAssert.assertXPathNodeValue(xml, "//status/itemCount/text()", "5");
        XPathAssert.assertXPathNodeValue(xml, "//status/itemsAdded/text()", "5");
        XPathAssert.assertXPathNodeValue(xml, "//status/itemsAddedRemaining/text()", "5");
        XPathAssert.assertXPathNodeValue(xml, "//status/itemsDeleted/text()", "5");
        XPathAssert.assertXPathNodeValue(xml, "//status/itemsDeletedRemaining/text()", "5");
        XPathAssert.assertXPathNodeValue(xml, "//status/itemsUpdated/text()", "5");
        XPathAssert.assertXPathNodeValue(xml, "//status/itemsUpdatedRemaining/text()", "5");
        XPathAssert.assertXPathNodeValue(xml, "//status/failures/text()", "5");
        XPathAssert.assertXPathNodeValue(xml, "//status/bytesFailed/text()", "5");
        XPathAssert.assertXPathNodeValue(xml, "//status/lastRuntime/text()", "5");
        XPathAssert.assertXPathNodeValue(xml, "//status/percentCompleted/text()", "2.5");
        XPathAssert.assertXPathNodeValue(xml, "//status/size/text()", "5");
        XPathAssert.assertXPathNodeValue(xml, "//status/state/text()", "test");
    }
    
    /**
     * InvocationHandler to return known values for a
     * defined set of return types
     */
    private static class MockInvocationHandler implements InvocationHandler {
        
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getReturnType().equals(String.class)) {
                return "test";
            }
            if (method.getReturnType().equals(Long.class)) {
                return new Long(5);
            }
            if (method.getReturnType().equals(long.class)) {
                return new Long(5);
            }
            if (method.getReturnType().equals(boolean.class)) {
                return Boolean.TRUE;
            }
            if (method.getReturnType().equals(Boolean.class)) {
                return Boolean.TRUE;
            }
            if (method.getReturnType().equals(Double.class)) {
                return new Double(2.5);
            }
            if (method.getReturnType().equals(double.class)) {
                return new Double(2.5);
            }
            if (method.getReturnType().equals(List.class) && isParameterizedGenericType(method, TransferStatus.class)) {
                List<TransferStatus> list = new ArrayList<TransferStatus>();
                list.add(new MockTransferStatus());
                list.add(new MockTransferStatus(70, 200000, "/mock/filename2"));
                return list;
            }
            if (method.getReturnType().equals(List.class) && isParameterizedGenericType(method, CurrentSynchronization.class)) {
                List<CurrentSynchronization> list = new ArrayList<CurrentSynchronization>();
                List<TransferStatus> x = new ArrayList<TransferStatus>();
                x.add(new MockTransferStatus(70, 200000, "/mock/filename2"));
                CurrentSynchronization s = new CurrentSynchronization();
                s.setId("mock-id");
                s.setContentSetName("mock-name");
                s.setCurrentTransfers(x);
                list.add(s);
                return list;
            }

            throw new RuntimeException("Unable to handle the following return type: "+method.getReturnType());
        }
        /**
         * Check if the generic type matching the method's the generic type
         */
        protected boolean isParameterizedGenericType(Method method, Class<?> type)
        {
            Type gType = method.getGenericReturnType() ;
            if(gType != null && gType instanceof ParameterizedType)
            {
                 Type[] genericArguments = ((ParameterizedType) gType).getActualTypeArguments();
                 if( genericArguments != null &&
                     genericArguments.length == 1 &&
                     genericArguments[0] != null &&
                     genericArguments[0].equals(type)
                   )
                 {
                     return true;
                 }
            }
            return false;
        }

        public class MockTransferStatus implements TransferStatus, Serializable
        {
            private static final long serialVersionUID = 1L;
            
            private double percentComplete;
            private long bytesTransferred;
            private String fileName;
            public MockTransferStatus()
            {
                this(50, 100000, "/mock/filename1");
            }
            public MockTransferStatus(double percentComplete, long  bytesTransferred, String fileName )
            {
                 this.percentComplete = percentComplete;
                 this.bytesTransferred = bytesTransferred;
                 this.fileName = fileName;
            }

            public double getPercentComplete() {
                return percentComplete;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public long getBytesRequested() {
                // TODO Auto-generated method stub
                return 0;
            }
            public long getBytesTransferred() {
                return bytesTransferred;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public String getFileName() {
                return fileName;  //To change body of implemented methods use File | Settings | File Templates.
            }
            @Override
            public TransferType getTransferType() {
                // TODO Auto-generated method stub
                return null;
            }
        }
    }
}
