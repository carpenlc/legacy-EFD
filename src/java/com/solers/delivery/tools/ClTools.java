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
package com.solers.delivery.tools;

import java.io.File;
import java.security.Provider;
import java.security.Security;
import java.util.HashSet;
import java.util.Set;

import com.rsa.jsafe.crypto.CryptoJ;
import com.rsa.jsafe.crypto.JSAFE_InvalidUseException;
import com.rsa.jsafe.provider.JsafeJCE;
import com.solers.delivery.security.PasswordType;
import com.solers.delivery.security.SecurityProviderUtil;
import com.solers.security.password.PasswordProvider;
import com.solers.security.ssl.SSLContextCreator;
import com.solers.util.unit.TimeIntervalUnit;

public final class ClTools {
    private ClTools() {}
    
    public static void initializeJsafeProvider() {
        try {
            CryptoJ.setMode(CryptoJ.FIPS140_MODE);
        } catch (JSAFE_InvalidUseException ex) {
            throw new RuntimeException(ex);
        }
        Provider fips = new JsafeJCE();
        Security.insertProviderAt(fips, 1);
    }
    
    public static void initializeSSL(PasswordProvider provider, File securityDirectory) {
        File keystore = new File(securityDirectory, "portal_keystore.p12");
        File truststore = new File(securityDirectory, "portal_truststore.jks");
        Set<String> ciphers = new HashSet<String>();
        ciphers.add("TLS_RSA_WITH_AES_128_CBC_SHA");
        ciphers.add("TLS_RSA_WITH_AES_256_CBC_SHA");
        new SSLContextCreator(provider.getPassword(PasswordType.PKI_KEY.key()),
            provider.getPassword(PasswordType.PKI_KEYSTORE.key()),
            provider.getPassword(PasswordType.PKI_TRUSTSTORE.key()),
            keystore.getAbsolutePath(), 
            truststore.getAbsolutePath(), 
            ciphers, 
            false,
            (int)TimeIntervalUnit.HOURS.convert(1, TimeIntervalUnit.SECONDS),
            SecurityProviderUtil.getPRNG()).init();
    }
    
    public static File extractFile(boolean mustExist, String... args) {
        return extractFile(mustExist, 0, args);
    }
    
    public static File extractFile(boolean mustExist, int expectedLocation, String... args) {
        if (expectedLocation >= 0 && args != null && args.length > expectedLocation) {
            String name = args[expectedLocation];
            if (isEmpty(name)) return null;
            File f = new File(name);
            if (mustExist) {
                if (f.exists()) return f;
                else return null;
            }
            return f;
        }
        return null;
    }
    
    public static File extractExistingFile(int expectedLocation, String... args) {
        return extractFile(true, expectedLocation, args);
    }
    
    public static File extractExistingFile(String... args) {
        return extractFile(true, 0, args);
    }

    public static boolean containsEmptyArguments(String... args) {
        for (String arg : args) {
            if (isEmpty(arg)) return true;
        }
        return false;
    }
    
    protected static boolean isEmpty(String s) {
        return s.trim().length() == 0;
    }
}
