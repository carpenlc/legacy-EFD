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

import junit.framework.Assert;

import org.junit.Test;

import com.solers.delivery.security.PasswordType;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class PasswordTypeTestCase {
    
    @Test
    public void testKeyValueOf() {
        PasswordType type = PasswordType.keyValueOf(PasswordType.PKI_KEY.key());
        Assert.assertEquals(PasswordType.PKI_KEY, type);
        
        try {
            PasswordType.keyValueOf("error");
            Assert.fail();
        } catch (IllegalArgumentException expected) {
            
        }
    }
    
    @Test
    public void testToString() {
        Assert.assertEquals(PasswordType.PKI_KEY.key(), PasswordType.PKI_KEY.toString());
    }
    
}
