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
package com.solers.delivery.gbs;

import org.apache.log4j.Logger;

public final class GBS {
    private static final Logger log = Logger.getLogger(GBS.class);
    
    private GBS() {
        //do not construct
    }

    private static Boolean gbsEnabled = null;
    
    public static synchronized void setGbsEnabled(boolean enabled) {
        gbsEnabled = enabled;
        log.info("GBS enabled: " + gbsEnabled);
    }
    
    public static boolean isGbsEnabled() {
        return (gbsEnabled == null) ? false : gbsEnabled.booleanValue();
    }
}
