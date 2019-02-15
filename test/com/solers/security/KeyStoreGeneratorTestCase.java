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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.CertificateExpiredException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class KeyStoreGeneratorTestCase {
    
    @Test
    public void testExpiredCertificates() throws Exception {
        File certChain = file("/security/expiredCerts.pem");
        File destination = new File("truststore.jks");
        
        KeyStoreGenerator generator = new KeyStoreGenerator(null, certChain);
        try {
            generator.saveTrustStore(destination, password());
            Assert.fail();
        } catch (CertificateExpiredException expected) {
            
        } finally {
            destination.delete();
        }
    }
    
    @Test
    public void testSaveKeyStore() throws Exception {
        File privateKey = key();
        File certChain = certs();
        testKeyStore(privateKey, certChain);
    }
    
    @Test
    public void testSaveTrustStore() throws Exception {
        File certChain = certs();
        File destination = new File("truststore.jks");
        
        KeyStoreGenerator generator = new KeyStoreGenerator(null, certChain);
        generator.saveTrustStore(destination, password());
        
        KeyStore trustStore = KeyStore.getInstance("JKS");
        InputStream is = new FileInputStream(destination);
        trustStore.load(is, password());
        is.close();
        
        Enumeration<String> e = trustStore.aliases();
        List<String> result = new ArrayList<String>();
        while (e.hasMoreElements()) {
            result.add(e.nextElement());
        }
        
        Assert.assertEquals(1, result.size());
        destination.delete();
    }
    
    @Test
    public void testEncryptedKey() throws Exception {
        File privateKey = file("/security/encryptedKey.pem");;
        File certChain = certs();
        testKeyStore(privateKey, certChain);
    }
    
    private void testKeyStore(File keyFile, File chain) throws Exception {
        File destination = new File("keystore.p12");
        
        KeyStoreGenerator generator = new KeyStoreGenerator(keyFile, chain);
        generator.saveKeyStore(destination, password(), password());
        
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        InputStream is = new FileInputStream(destination);
        keyStore.load(is, password());
        is.close();
        
        Key key = keyStore.getKey("key", password());
        Assert.assertNotNull(key);
        destination.delete();
    }
    
    private File key() throws Exception {
        return file("/security/key.pem");
    }
    
    private File certs() throws Exception {
        return file("/security/certs.pem");
    }
    
    private File file(String name) throws Exception {
        return new File((getClass().getResource(name)).toURI());
    }
    
    private char [] password() {
        return "password".toCharArray();
    }
}
