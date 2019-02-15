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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;

import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.rsa.jsafe.crypto.CryptoJ;
import com.rsa.jsafe.provider.JsafeJCE;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class EncryptionUtilsTestCase extends TestCase {
    
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
    public void testCrypt() {
        String plaintext = "hello world";
        char [] password = "my password".toCharArray();
        
        byte [] ciphertext = EncryptionUtils.encrypt(password, plaintext.getBytes());
        byte [] decryptedPlaintext = EncryptionUtils.decrypt(password, ciphertext);
        
        Assert.assertEquals(plaintext, new String(decryptedPlaintext));
    }
    
    @Test
    public void testInvalidPassword() {
        String plaintext = "hello world";
        char [] password = "my password".toCharArray();
        char [] wrongPassword = "wrong password".toCharArray();
        
        byte [] ciphertext = EncryptionUtils.encrypt(password, plaintext.getBytes());
        try {
            EncryptionUtils.decrypt(wrongPassword, ciphertext);
            Assert.fail();
        } catch (Exception ignore) {
            
        }
    }
    
    @Test
    public void testHex() {
        String text = "hello world";
        Assert.assertEquals(text, new String(EncryptionUtils.fromHex(EncryptionUtils.toHex(text.getBytes()))));
    }

    @Test
    public void testSealUnseal() throws IllegalBlockSizeException, IOException, GeneralSecurityException, ClassNotFoundException{
        char[] password = "foobar".toCharArray();
        TestSerializable orig = new TestSerializable(1234, "foo");
        SealedObject sealed = EncryptionUtils.seal(orig, password);
        TestSerializable unsealed = EncryptionUtils.unseal(sealed, password);

        Assert.assertTrue(orig.equals(unsealed));
    }
    
    @Test
    public void testMac() throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        SecretKey key = EncryptionUtils.getMacKey();
        Mac mac = EncryptionUtils.getMac(key);
        
        byte[] bytes = EncryptionUtils.getObjectArray("foobar");
        
        byte[] origMac = mac.doFinal(bytes);
        
        byte[] newBytes = EncryptionUtils.getObjectArray("foobar");
        byte[] newMac = mac.doFinal(newBytes);

        //assert mac generates the same value for the same string value
        assertTrue(compareBytes(origMac, newMac));
        
        byte[] diffBytes = EncryptionUtils.getObjectArray("batz");
        byte[] diffMac = mac.doFinal(diffBytes);
        
        //mac always generate hashes of the same length
        assertTrue(origMac.length == diffMac.length);
        //hashes should be different for different input
        assertFalse(compareBytes(origMac, diffMac));
    }

    /**
     * Returns true if the arrays are identical.  Using this instead
     * of assertArrayEquals because there is no corresponding 
     * assertArrayNotEquals
     * 
     * @param a - orignal array
     * @param b - to be compared array
     * @return true if arrays are the same, false otherwise
     */
    private boolean compareBytes(byte[] a, byte[] b) {
        boolean result = (a.length == b.length);
        
        if (result) {
            for(int i = 0; i < a.length; i++) {
                result = result && (a[i] == b[i]);
            }
        }
        
        return result;
    }
}
