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
package com.solers.delivery.rest;

import java.util.Arrays;

import junit.framework.TestCase;

import org.restlet.Filter;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class IpFilterTestCase extends TestCase {
    
    public void testFilter() {
        
        IpFilter filter = new IpFilter(Arrays.asList("127.0.0.1"));
        
        Request request = new Request();
        Response response = new Response(request);
        request.getClientInfo().setAddress("127.0.0.1");
        
        assertEquals(Filter.CONTINUE, filter.beforeHandle(request, response));
        
        request.getClientInfo().setAddress("192.168.1.1");
        
        assertEquals(Filter.STOP, filter.beforeHandle(request, response));
        assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
    }
    
}
