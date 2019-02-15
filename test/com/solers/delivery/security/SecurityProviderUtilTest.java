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
package com.solers.delivery.security;


import java.security.Provider;
import java.security.Security;

import junit.framework.Assert;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

//import com.rsa.jsafe.crypto.FIPS140Context;
//import com.rsa.jsafe.crypto.CryptoJ;
import com.rsa.jsafe.provider.JsafeJCE;
import com.solers.delivery.security.SecurityProviderUtil;

public class SecurityProviderUtilTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        //Security.removeProvider("BC");
        SecurityProviderUtil.init();
    }
    
    @Test
    public void checkDefaultProvider() {
        Provider[] providers = Security.getProviders();
        Assert.assertEquals(JsafeJCE.class.getName(), providers[0].getClass().getName());
        Assert.assertTrue(SecurityProviderUtil.inFIPSMode());
    }
    
    @Test
    public void testForFipsPrng() {
        Assert.assertEquals("FIPS186PRNG", SecurityProviderUtil.getPRNG().getAlgorithm());
    }
    
    @Test
    public void disableFipsAndReInit() {
        
        //CryptoJ.disableLibrary((FIPS140Context)SecurityProviderUtil.getProvider());
        SecurityProviderUtil.removeFipsProvider();
        SecurityProviderUtil.init();
        Assert.assertFalse(SecurityProviderUtil.inFIPSMode());
        Assert.assertEquals(BouncyCastleProvider.class.getName(), SecurityProviderUtil.getProvider().getClass().getName());
    }
    
    @Test
    public void removeFIPSProvider() {
        SecurityProviderUtil.removeFipsProvider();
        Assert.assertNull(Security.getProvider("JsafeJCE"));
        Assert.assertFalse(SecurityProviderUtil.inFIPSMode());
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        SecurityProviderUtil.removeFipsProvider();
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {}

}
