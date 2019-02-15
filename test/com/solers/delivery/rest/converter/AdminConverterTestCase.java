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
package com.solers.delivery.rest.converter;

import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.resource.StringRepresentation;

import com.solers.delivery.domain.User;
import com.solers.delivery.security.PasswordType;
import com.solers.security.EncryptionUtils;
import com.solers.security.password.MapPasswordProvider;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class AdminConverterTestCase {
    
    private AdminConverter converter;
    
    @Before
    public void setUp() {
        MapPasswordProvider provider = new MapPasswordProvider();
        provider.setPassword(PasswordType.ENCRYPTION.key(), EncryptionUtils.toHex(EncryptionUtils.generateKey().getEncoded()));
        converter = new AdminConverter(provider);
    }
    
    @Test
    public void testConvertUser() throws Exception {
        Date date = new Date();
        Date lastLogin = new Date();
        
        User expected = new User();
        expected.setAdminRole(true);
        expected.setCurrLogin(date);
        expected.setEnabled(true);
        expected.setFailedLogins(3);
        expected.setFirstName("firstName");
        expected.setId(10L);
        expected.setInitialPassword(false);
        expected.setLastFailedLogins(4);
        expected.setLastLocation("abc");
        expected.setLastLogin(lastLogin);
        expected.setLastName("lastName");
        expected.setUsername("username");
        expected.setLocation("cba");
        
        String xml = converter.to(MediaType.TEXT_XML, expected).getText();
        
        User actual = converter.convertUser(new StringRepresentation(xml, MediaType.TEXT_XML));
        
        Assert.assertEquals(expected.isAdminRole(), actual.isAdminRole());
        Assert.assertEquals(expected.getCurrLogin(), actual.getCurrLogin());
        Assert.assertEquals(expected.isEnabled(), actual.isEnabled());
        Assert.assertEquals(expected.getFailedLogins(), actual.getFailedLogins());
        Assert.assertEquals(expected.getFirstName(), actual.getFirstName());
        Assert.assertEquals(expected.getId(), actual.getId());
        Assert.assertEquals(expected.isInitialPassword(), actual.isInitialPassword());
        Assert.assertEquals(expected.getLastFailedLogins(), actual.getLastFailedLogins());
        Assert.assertEquals(expected.getLastLocation(), actual.getLastLocation());
        Assert.assertEquals(expected.getLastLogin(), actual.getLastLogin());
        Assert.assertEquals(expected.getLastName(), actual.getLastName());
        Assert.assertEquals(expected.getUsername(), actual.getUsername());
        Assert.assertEquals(expected.getLocation(), actual.getLocation());
    }
    
}
