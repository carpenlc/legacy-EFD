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
package com.solers.delivery.domain;

import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class ContentSetTestCase {

    @Test
    public void testAllows() {
        ContentSet c = new ContentSet();
        
        Assert.assertFalse(c.allows(null));
        Assert.assertFalse(c.allows(""));
        Assert.assertFalse(c.allows("commonName"));
        
        c.addAllowedHost(new AllowedHost("alias", "commonName"));
        c.addAllowedHost(new AllowedHost("test", "cc"));
        
        Assert.assertTrue(c.allows("commonName"));
        Assert.assertTrue(c.allows("cc"));
        Assert.assertFalse(c.allows("foo"));
    }
    
    @Test
    public void testMapConversion() {
        ContentSet c = new ContentSet();
        Assert.assertEquals(0, c.getResourceParameters().size());
        Assert.assertEquals(0, c.getResourceParametersAsMap().size());
        ResourceParameter p = new ResourceParameter();
        p.setName("1");
        p.setValue("test");
        Set<ResourceParameter> l = new HashSet<ResourceParameter>();
        l.add(p);
        c.setResourceParameters(l);

        Assert.assertEquals(1, c.getResourceParameters().size());
        Assert.assertEquals(1, c.getResourceParametersAsMap().size());
        Assert.assertEquals("test", c.getResourceParametersAsMap().get("1"));
        
        ResourceParameter p2 = new ResourceParameter();
        p2.setName("2");
        p2.setValue("another");
        l.add(p2);
        
        Assert.assertEquals(2, c.getResourceParameters().size());
        Assert.assertEquals(2, c.getResourceParametersAsMap().size());
        Assert.assertEquals("another", c.getResourceParametersAsMap().get("2"));
    }
}
