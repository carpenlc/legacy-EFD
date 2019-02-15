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

import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;

import org.apache.log4j.Logger;
import com.rsa.jsafe.crypto.CryptoJ;
import com.rsa.jsafe.provider.JsafeJCE;

public final class SecurityProviderUtil {

    private static final Logger log = Logger.getLogger(SecurityProviderUtil.class);
    private static final int RANDOM_SEED_BYTES_LENGTH = 64;

    private static boolean isFIPS = false;

    private SecurityProviderUtil() {}

    public static String init() {
        return loadProviders();
    }

    private static void logProviders() {

        Provider[] providers = Security.getProviders();
        for (int i = 0; i < providers.length; i++) {
            Provider provider = providers[i];
            log.info(String.format("Provider Index:%d  Name:%s  Version:%s  Class:%s", (i + 1), provider.getName(), provider.getVersion(), provider.getClass().getName()));
        }
        log.info("Default JVM Random Algorithm: " + new SecureRandom().getAlgorithm());
        log.info(String.format("Random Algorithm [getPRNG()]: %s  Seed bytes: %d", getPRNG().getAlgorithm(), RANDOM_SEED_BYTES_LENGTH));
        log.info("Provider Name [getProvider()]: " + getProvider().getName());
        log.info("FIPS [inFIPS()] : " + inFIPSMode());
    }

    // TestPros, July 2010, Version 2.1.1, modify to fail if not in FIPS mode
    private static String loadProviders() {

        try {
            log.info(String.format("FIPS Compliant Mode: %b  Version: %s", CryptoJ.isFIPS140Compliant(), CryptoJ.CRYPTO_J_VERSION));
            log.info("Running FIPS 140 self test...");
            try {
                JsafeJCE.ensureSelfIntegrity(true);
            } catch (SecurityException e) {
                throw new Exception("FIPS self test failed");
            }
            log.info("FIPS 140 self-test result: " + (CryptoJ.selfTestPassed() ? "PASS" : "FAIL"));
            
            assert (CryptoJ.getMode() == CryptoJ.OPERATIONAL);
            
            Security.insertProviderAt(new JsafeJCE(), 1);
            isFIPS = true;
            return Security.getProviders()[0].getName();
        } catch (Exception e) {
            log.fatal("FIPS Security provider not present or not running in FIPS mode", e);
            return null;
        } finally {
            logProviders();
        }
    }

    public static Provider getProvider() {
    	return Security.getProviders()[0];
    }

    public static boolean inFIPSMode() {
        return isFIPS;
    }

    public static SecureRandom getPRNG() {

        if (isFIPS) {
            SecureRandom random;
            try {
                random = SecureRandom.getInstance("CTRDRBG128");
                //random = SecureRandom.getInstance("FIPS186PRNG");
            } catch (NoSuchAlgorithmException e) {
                isFIPS = false;
                log.info("Unable to get CTRDRBG128 -- Provider likely not FIPS compliant");
                //log.info("Unable to get FIPS186PRNG -- Provider likely not FIPS compliant");
                return new SecureRandom();
            }
            SecureRandom seedGenerator = CryptoJ.getSeeder();
            random.setSeed(seedGenerator.generateSeed(RANDOM_SEED_BYTES_LENGTH));
            return random;
        } else {
            return new SecureRandom();
        }
    }

    public static void removeFipsProvider() {

        Provider fipsJCE = null;
        try {
            fipsJCE = (Provider) Class.forName("com.rsa.jsafe.provider.JsafeJCE").newInstance();
            Security.removeProvider(fipsJCE.getName());
        } catch (Exception e) {
            log.warn("Possible problem unloading FIPS Provider");
        }
        
        isFIPS = false;
    }
}
