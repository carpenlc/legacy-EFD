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
package com.solers.security.password;

import java.io.File;
import java.security.Provider;
import java.security.Security;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.rsa.jsafe.crypto.CryptoJ;
import com.rsa.jsafe.provider.JsafeJCE;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class DefaultPasswordProviderTestCase {
    
    @Before
    public void setUp() throws Exception {
        CryptoJ.setMode(CryptoJ.FIPS140_MODE);
        Provider fips = new JsafeJCE(CryptoJ.getFIPS140Context());
        Security.insertProviderAt(fips, 1);
    }
    
    @After
    public void tearDown() throws Exception {
        CryptoJ.setMode(CryptoJ.NOT_INITIALIZED);
        Security.removeProvider(new JsafeJCE(CryptoJ.getFIPS140Context()).getName());
    }
    
    @Test
    public void testLoadFileNotFound() {
        File file = new File(getClass().getSimpleName());
        try {
            new DefaultPasswordProvider(file);
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }
    }
    
    @Test
    public void testSaveOnSetProperty() {
        File file = new File("testSaveOnSetProperty");
        file.deleteOnExit();
        
        DefaultPasswordProvider p = new DefaultPasswordProvider(file);
        p.initialize("password".toCharArray());
        
        p.setPassword("foo", "bar");
        
        DefaultPasswordProvider p2 = new DefaultPasswordProvider(file);
        p2.initialize("password".toCharArray());
        Assert.assertEquals("bar", p2.getPassword("foo"));
    }
    
    @Test
    public void testInvalidMasterPassword() {
        File file = new File("testInvalidMasterPassword");
        file.deleteOnExit();
        
        DefaultPasswordProvider p = new DefaultPasswordProvider(file);
        p.initialize("password".toCharArray());
        
        p.setPassword("foo", "bar");
        
        DefaultPasswordProvider p2 = new DefaultPasswordProvider(file);
        p2.initialize("password1".toCharArray());
        Assert.assertNull(p2.getPassword("foo"));
    }
    
    @Test
    public void testInitialize() {
        File file = new File("testInitialize");
        file.deleteOnExit();
        
        DefaultPasswordProvider p = new DefaultPasswordProvider(file);
        p.initialize("password".toCharArray());
        p.setPassword("key1", "xxx");
        p.setPassword("key2", "yyy");
        
        Assert.assertEquals("xxx", p.getPassword("key1"));
        Assert.assertEquals("yyy", p.getPassword("key2"));
        
        p.initialize("newpassword".toCharArray());
        Assert.assertEquals("xxx", p.getPassword("key1"));
        Assert.assertEquals("yyy", p.getPassword("key2"));
        
        p.initialize("newpassword-again".toCharArray());
        Assert.assertEquals("xxx", p.getPassword("key1"));
        Assert.assertEquals("yyy", p.getPassword("key2"));
    }
}
