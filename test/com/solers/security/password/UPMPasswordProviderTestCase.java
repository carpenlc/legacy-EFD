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
package com.solers.security.password;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import com.solers.security.password.PasswordProvider;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class UPMPasswordProviderTestCase {
    
    private final static String LDAP = "WAATSLDAPPassword";
    private final static String DATABASE = "DatabasePassword";
    private final static String PORTAL = "IDMPortalUserPassword";
    private final static String KEYSTORE = "IDMKeystorePassword";
    
    @Test
    public void testParseFile() throws Exception {
        UPMPasswordProvider provider = new UPMPasswordProvider(file("/upm_keystore.p12").getPath());
        provider.initialize("password".toCharArray());
        check(provider);
    }
    
    @Test
    public void testParseURI() throws Exception {
        UPMPasswordProvider provider = new UPMPasswordProvider(file("/upm_keystore.p12").toURI().toString());
        provider.initialize("password".toCharArray());
        check(provider);
    }
    
    private void check(PasswordProvider provider) {
        Assert.assertEquals("password", provider.getPassword(LDAP));
        Assert.assertEquals("password", provider.getPassword(DATABASE));
        Assert.assertEquals("password", provider.getPassword(PORTAL));
        Assert.assertEquals("password", provider.getPassword(KEYSTORE));
        Assert.assertNull(provider.getPassword("does not exist"));
    }
    
    private File file(String name) throws Exception {
        return new File((getClass().getResource(name)).toURI());
    }
}
