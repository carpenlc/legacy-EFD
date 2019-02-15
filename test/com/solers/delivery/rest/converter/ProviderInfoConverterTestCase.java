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
import com.solers.delivery.inventory.fs.FileSystemInventoryProvider;
import com.solers.delivery.inventory.plugin.provider.ProviderInfo;

public class ProviderInfoConverterTestCase extends TestCase {
    
    Converter c = new ProviderInfoConverter();
    
    public void assertProviderInfo(String xml, ProviderInfo pi) throws Exception {
        XPathAssert.assertXPathNodeExists(xml, "//plugin");
        XPathAssert.assertXPathNodeExists(xml, "//plugin/name");
        XPathAssert.assertXPathNodeExists(xml, "//plugin/version");
        XPathAssert.assertXPathNodeExists(xml, "//plugin/vendor");
        XPathAssert.assertXPathNodeExists(xml, "//plugin/developer");
        
        XPathAssert.assertXPathNodeValue(xml, "//plugin/name", pi.getName());
        XPathAssert.assertXPathNodeValue(xml, "//plugin/version", pi.getVersion());
        XPathAssert.assertXPathNodeValue(xml, "//plugin/vendor", pi.getVendor());
        XPathAssert.assertXPathNodeValue(xml, "//plugin/developer", pi.getDeveloper());
    }
    
    public void assertProviderInfo(ProviderInfo p1, ProviderInfo p2) {
        assertEquals(p1.getName(), p2.getName());
        assertEquals(p1.getVersion(), p2.getVersion());
        assertEquals(p1.getDeveloper(), p2.getDeveloper());
        assertEquals(p1.getVendor(), p2.getVendor());
    }
    
    public void testConvert() throws Exception {
        ProviderInfo p = new FileSystemInventoryProvider().getProviderInfo();
        Representation r = c.to(MediaType.TEXT_XML, p);
        assertProviderInfo(r.getText(), p);
        
        ProviderInfo p2 = (ProviderInfo) c.convert(r);
        assertProviderInfo(p, p2);
    }

}
