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
package com.solers.delivery.web.filters;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;

import com.solers.delivery.web.XSLTFilter;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class XSLTFilterTestCase {
    
    private TestMockServletContext ctx;
    private MockFilterConfig config;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private Filter filter;
    
    @Before
    public void setUp() throws Exception {
        String sheet = "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">" +
                       "<xsl:output omit-xml-declaration=\"yes\" indent=\"no\"/>" +
                       "<xsl:template match=\"/\">" +
                       "<output><xsl:value-of select=\"foo\"/></output>" +
                       "</xsl:template></xsl:stylesheet>";
        
        ctx = new TestMockServletContext();
        ctx.setStyleSheet(sheet);
        config = new MockFilterConfig(ctx);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        config.addInitParameter("styleSheet", "/style.xsl");
        request.setRequestURI("/file.xml");
        filter = new XSLTFilter();
    }
    
    @Test
    public void testFilterWithoutDebug() throws Exception { 
        config.addInitParameter("debug", "false");
        
        filter.init(config);
        filter.doFilter(request, response, null);
        
        Assert.assertEquals("text/html", response.getContentType());
        Assert.assertEquals("<output>data</output>", response.getContentAsString());
        
        writeNewSheet();
        
        response = new MockHttpServletResponse();
        filter.doFilter(request, response, null);
        
        Assert.assertEquals("text/html", response.getContentType());
        Assert.assertEquals("<output>data</output>", response.getContentAsString());
    }
    
    @Test
    public void testFilterWithDebug() throws Exception {
        config.addInitParameter("debug", "true");
        
        filter.init(config);
        filter.doFilter(request, response, null);
        
        Assert.assertEquals("text/html", response.getContentType());
        Assert.assertEquals("<output>data</output>", response.getContentAsString());
        
        writeNewSheet();
        
        response = new MockHttpServletResponse();
        filter.doFilter(request, response, null);
        
        Assert.assertEquals("text/html", response.getContentType());
        Assert.assertEquals("<newOutput>data</newOutput>", response.getContentAsString());
    }
    
    private void writeNewSheet() throws Exception {
        String newSheet = "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">" +
        "<xsl:output omit-xml-declaration=\"yes\" indent=\"no\"/>" +
        "<xsl:template match=\"/\">" +
        "<newOutput><xsl:value-of select=\"foo\"/></newOutput>" +
        "</xsl:template></xsl:stylesheet>";
        
        ctx.setStyleSheet(newSheet);
    }
    
    private class TestMockServletContext extends MockServletContext {
        
        private String styleSheet;

        @Override
        public RequestDispatcher getRequestDispatcher(String path) {
            return new MockRequestDispatcher();
        }
        
        public void setStyleSheet(String sheet) {
            styleSheet = sheet;
        }

        @Override
        public InputStream getResourceAsStream(String arg0) {
            return new ByteArrayInputStream(styleSheet.getBytes());
        }
    }
    
    private class MockRequestDispatcher implements RequestDispatcher {

        @Override
        public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException {
  
            PrintWriter writer = response.getWriter();
            writer.write("<foo>data</foo>");
            writer.flush();
        }
        
        @Override
        public void forward(ServletRequest arg0, ServletResponse arg1) throws ServletException, IOException {
            throw new UnsupportedOperationException();
        }
        
    }
}
