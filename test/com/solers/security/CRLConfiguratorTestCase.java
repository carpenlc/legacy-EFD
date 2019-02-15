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
package com.solers.security;


import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.PKIXParameters;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CRLConfiguratorTestCase {
    private String truststoreFile = "dev/security/portal_truststore.jks";
    private String certStorePath = "modules/security/src/test/resources/crl";
    
    
    @Before
    public void setUp() throws Exception {      
        System.setProperty("certificateRevocation.crlStores", "");
        System.setProperty("certificateRevocation.enable", Boolean.toString(true));
        System.setProperty("certificateRevocation.dynamic", Boolean.toString(false));
    }
    
    @After
    public void tearDown() {      
        System.setProperty("certificateRevocation.crlStores", "");
        System.setProperty("certificateRevocation.enable", "");
        System.setProperty("certificateRevocation.dynamic", "");  
    }
    
    @Test
    public void testLoadCertPathParameters() throws Exception {        
        PKIXParameters parameters = CRLConfigurator.loadCertPathParameters(getTrustStore());
        Assert.assertEquals(parameters.getCertStores().size(), 0);
    }
    
    @Test
    public void testLoadCertPathParametersWithCertStore() throws Exception {
        PKIXParameters parameters = CRLConfigurator.loadCertPathParameters(getTrustStore());
        Assert.assertEquals(parameters.getCertStores().size(), 0);

        System.setProperty("certificateRevocation.crlStores", certStorePath);
        
        Assert.assertEquals(parameters.getCertStores().size(), 1);
        Assert.assertEquals(parameters.getCertStores().size(), 1);
        
        System.setProperty("certificateRevocation.crlStores", ""); 
        Assert.assertEquals(parameters.getCertStores().size(), 0);
    }
    
    private KeyStore getTrustStore() throws Exception {
        InputStream input = new FileInputStream(truststoreFile);
        
        KeyStore truststore = KeyStore.getInstance("JKS");
        truststore.load(input, "password".toCharArray());
        input.close();
        return truststore;
    }
}
