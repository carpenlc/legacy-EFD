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
package com.solers.delivery.web.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

/**
 * SecurityBannerConfig helps configure which level the security banner should display.
 * Additional, makes sure that the following pairs should be matched up together:
 *   - Unclassified + green background
 *   - Secret + red background
 *   - Top Secret + orange background
 *   - Top Secret/SCI + yellow background
 */
public class SecurityBannerConfig {
    private String securityLevel;
    private String backgroundColor;
    private String hostName;

    private static Map<String, String> PAIRS;

    static {
        PAIRS = new HashMap<String, String>(4);
        PAIRS.put("UNCLASSIFIED","#00CC33");
        PAIRS.put("SECRET","#FF0033");
        PAIRS.put("TOP SECRET","#FF9933");
        PAIRS.put("TOP SECRET/SCI","#FFFF33");
        PAIRS = Collections.unmodifiableMap(PAIRS);
    }

    public void setSecurityLevel(String securityLevel) {
        this.securityLevel = securityLevel;
    }

    public String getSecurityLevel() {
        return securityLevel.toUpperCase();
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getBackgroundColor() {
        if ( PAIRS.containsKey(getSecurityLevel()) )
            return PAIRS.get(getSecurityLevel()); 
        else
            return backgroundColor;
    }

    public String getHostName() {
        try {
            java.net.InetAddress localMachine = java.net.InetAddress.getLocalHost();
            hostName = localMachine.getHostName();
        }
        catch(java.net.UnknownHostException ex) {
            //handle exception
        }
        return hostName;
    }
}
