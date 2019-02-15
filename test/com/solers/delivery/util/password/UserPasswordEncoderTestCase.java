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
package com.solers.delivery.util.password;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class UserPasswordEncoderTestCase extends TestCase {
    
    public void testIsPasswordValid() {
        UserPasswordEncoder encoder = new UserPasswordEncoder();
        String rawPass = "password";
        String encPass = encoder.encodePassword(rawPass);
        
        assertTrue(encoder.isPasswordValid(encPass, rawPass));
        
        encPass = encoder.encodePassword(rawPass, "foo");
        
        assertTrue(encoder.isPasswordValid(encPass, rawPass, "bar"));
    }

}
