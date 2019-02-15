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
package com.solers.delivery.rest.converter;

import junit.framework.TestCase;

import org.restlet.data.MediaType;
import org.restlet.resource.StringRepresentation;

import com.solers.delivery.XPathAssert;
import com.solers.util.dao.ValidationException;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class ValidationExceptionConverterTestCase extends TestCase {
    
    ValidationExceptionConverter converter = new ValidationExceptionConverter();
    
    public void testConvertToXML() throws Exception {
        ValidationException ex = new ValidationException();
        
        ex.addMessage("foo");
        ex.addMessage("bar");
        
        String xml = converter.to(MediaType.TEXT_XML, ex).getText();
        
        XPathAssert.assertXPathNodeExists(xml, "//validation-errors");
        XPathAssert.assertXPathNodeSetValues(xml, "//validation-errors", "foo", "bar");
    }
    
    public void testConvertFromXML() throws Exception {
        String xml = "" +
            "<validation-errors>" +
                "<string>foo</string>" +
                "<string>bar</string>" +
            "</validation-errors>";
        
        ValidationException ex = (ValidationException) converter.from(new StringRepresentation(xml, MediaType.TEXT_XML));
        
        assertNotNull(ex);
        assertEquals(2, ex.getMessages().size());
        assertTrue(ex.getMessages().contains("foo"));
        assertTrue(ex.getMessages().contains("bar"));
    }
}
