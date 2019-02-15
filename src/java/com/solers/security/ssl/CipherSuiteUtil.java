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
package com.solers.security.ssl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

public class CipherSuiteUtil {

    private static final Logger log = Logger.getLogger(CipherSuiteUtil.class);

    public static String[] getEnabledCipherSuites(String[] supportedCipherSuites, Set<String> allowedCipherSuites) {

        allowedCipherSuites = Collections.unmodifiableSet(allowedCipherSuites);
        // Allows SSL to properly initialize if at least ONE of the cipher suites is valid.
        Set<String> enabled = new HashSet<String>(Arrays.asList(supportedCipherSuites));
        enabled.retainAll(allowedCipherSuites);
        logCipherSuites(supportedCipherSuites,"Supported");
        logCipherSuites(allowedCipherSuites.toArray(new String[] {}),"Proposed");
        
        Set<String> unUsedCipherSuites = new HashSet<String>(allowedCipherSuites);
        unUsedCipherSuites.removeAll(enabled);
        logCipherSuites(unUsedCipherSuites.toArray(new String[] {}),"Proposed but not Supported");
        
        return enabled.toArray(new String[] {});
    }
    
    private static void logCipherSuites(String[] suites, String type) {
        if (log.isDebugEnabled()) {
            StringBuffer sb = new StringBuffer();
            for (String suite : suites) {
                sb.append("\n     Cipher Suite: ").append(suite);
            }
            log.debug(type + " Cipher Suites: " + sb.toString());
        }
    }
}
