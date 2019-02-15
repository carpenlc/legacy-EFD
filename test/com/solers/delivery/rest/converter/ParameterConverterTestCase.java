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
import org.restlet.resource.Representation;

import com.solers.delivery.XPathAssert;
import com.solers.delivery.inventory.plugin.provider.InventoryParameter;
import com.solers.delivery.inventory.plugin.provider.Parameter;

public class ParameterConverterTestCase extends TestCase {
    
    Converter c = new ParameterConverter();
    
    public void assertParameter(String xml, Parameter p) throws Exception {
        XPathAssert.assertXPathNodeExists(xml, "//parameter");
        XPathAssert.assertXPathNodeExists(xml, "//parameter/name");
        XPathAssert.assertXPathNodeExists(xml, "//parameter/type");
        XPathAssert.assertXPathNodeExists(xml, "//parameter/mandatory");
        XPathAssert.assertXPathNodeExists(xml, "//parameter/encrypted");
        XPathAssert.assertXPathNodeExists(xml, "//parameter/description");
        
        XPathAssert.assertXPathNodeValue(xml, "//parameter/name", p.name());
        XPathAssert.assertXPathNodeValue(xml, "//parameter/type", p.type().getName());
        XPathAssert.assertXPathNodeValue(xml, "//parameter/mandatory", Boolean.toString(p.mandatory()));
        XPathAssert.assertXPathNodeValue(xml, "//parameter/encrypted", Boolean.toString(p.encrypted()));
        XPathAssert.assertXPathNodeValue(xml, "//parameter/description", p.description());
    }
    
    public void assertParameter(Parameter p1, Parameter p2) {
        assertEquals(p1.name(), p2.name());
        assertEquals(p1.description(), p2.description());
        assertEquals(p1.encrypted(), p2.encrypted());
        assertEquals(p1.mandatory(), p2.mandatory());
        assertEquals(p1.type(), p2.type());
    }
    
    public void testConvert() throws Exception {
        Parameter p = new InventoryParameter("test", String.class, true, true, "description");
        Representation r = c.to(MediaType.TEXT_XML, p);
        assertParameter(r.getText(), p);
        
        Parameter p2 = (Parameter) c.convert(r);
        assertParameter(p, p2);
    }
}
