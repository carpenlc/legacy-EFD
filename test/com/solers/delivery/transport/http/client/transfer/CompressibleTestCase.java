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
package com.solers.delivery.transport.http.client.transfer;

import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.solers.delivery.transport.http.client.util.CompressionRules;

public class CompressibleTestCase {

    private CompressionRules compress;

    @Before
    public void setup() {
        this.compress = new CompressionRules();
        this.compress.setCompressibleExtensions("txt,log,html,xml");
        this.compress.setMinimumFileSizeBytes(5000);
    }

    @Test
    public void privateMethod() {
        
        String ext;
        ext = getExtension("blah");
        Assert.assertEquals(null, ext);
        ext = getExtension("blah.txt");
        Assert.assertEquals("txt", ext);
        ext = getExtension("blah.");
        Assert.assertEquals(null, ext);
        ext = getExtension(".txt");
        Assert.assertEquals("txt", ext);
        ext = getExtension(".txt.");
        Assert.assertEquals(null, ext);
    }
    
    private String getExtension(String path) {
        try {
            Method m = this.compress.getClass().getDeclaredMethod("getExtension", new Class[] {String.class});
            m.setAccessible(true);
            return (String)m.invoke(compress, path);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } 
    }

    @Test
    public void testSimpleGood() {

        Assert.assertTrue(this.compress.shouldBeCompressed("blah.txt",5000));
        Assert.assertTrue(this.compress.shouldBeCompressed("blah.log",5000));
        Assert.assertTrue(this.compress.shouldBeCompressed("blah.html",5000));
        Assert.assertTrue(this.compress.shouldBeCompressed("blah.xml",5000));
        
        Assert.assertTrue(this.compress.shouldBeCompressed("blah.txt",5001));
        Assert.assertTrue(this.compress.shouldBeCompressed("blah.log",5001));
        Assert.assertTrue(this.compress.shouldBeCompressed("blah.html",5001));
        Assert.assertTrue(this.compress.shouldBeCompressed("blah.xml",5001));
    }

    @Test
    public void testSimpleBad() {

        Assert.assertFalse(this.compress.shouldBeCompressed("blah.txt.",4999));
        Assert.assertFalse(this.compress.shouldBeCompressed("blah.log.",4999));
        Assert.assertFalse(this.compress.shouldBeCompressed("blah.html.",4999));
        Assert.assertFalse(this.compress.shouldBeCompressed("blah.xml.",4999));

        Assert.assertFalse(this.compress.shouldBeCompressed("blah",4999));
        Assert.assertFalse(this.compress.shouldBeCompressed("blah.zzz",4999));

    }

}
