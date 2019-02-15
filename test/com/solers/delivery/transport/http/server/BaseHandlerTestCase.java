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
package com.solers.delivery.transport.http.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class BaseHandlerTestCase extends TestCase {
    
    MockHttpServletRequest request;
    BaseHandler handler;
    
    public void setUp() {
        request = new MockHttpServletRequest();
        handler = new BaseHandler() {
            public void handle(String arg0, HttpServletRequest arg1, HttpServletResponse arg2, int arg3) throws IOException, ServletException {
            }
        };
    }
    
    public void testGetPathInfo() {
        request.setPathInfo("a/b/c");
        assertEquals("a/b/c", handler.getPathInfo(request));
        
        request.setPathInfo("/a/b/c");
        assertEquals("a/b/c", handler.getPathInfo(request));
        
        request.setPathInfo("/");
        assertEquals("/", handler.getPathInfo(request));
    }
    
}
