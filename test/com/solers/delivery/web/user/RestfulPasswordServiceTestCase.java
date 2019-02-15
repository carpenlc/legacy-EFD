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
package com.solers.delivery.web.user;

import java.util.Calendar;

import com.solers.delivery.domain.Password;
import com.solers.delivery.domain.User;
import com.solers.delivery.rest.BaseRestTestCase;
import com.solers.delivery.rest.auth.MockRestAuthentication;
import com.solers.delivery.rest.converter.AdminConverter;
import com.solers.delivery.security.PasswordType;
import com.solers.delivery.user.PasswordService;
import com.solers.delivery.user.RestfulPasswordService;
import com.solers.delivery.util.password.InvalidPasswordException;
import com.solers.delivery.util.password.UserPasswordEncoder;
import com.solers.delivery.util.password.PasswordReusedException;
import com.solers.security.password.MapPasswordProvider;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class RestfulPasswordServiceTestCase extends BaseRestTestCase {
    
    private PasswordService service;
    
    @Override
    public void setUp() {
        super.setUp();
        MapPasswordProvider provider = new MapPasswordProvider();
        provider.setPassword(PasswordType.ENCRYPTION.key(), "41ba6c532588f2b03497000f22a9d504c6b41ee180a65f2c875fad715417b6ea");
        service = new RestfulPasswordService("localhost", getPort(), new MockRestAuthentication(), new AdminConverter(provider));
    }

    public void testGetPassword() {
        assertNull(service.getPassword("noexist"));
        
        User user = new User();
        user.setId(1L);
        user.setUsername("username");
        
        getDAOFactory().getUserDAO().makePersistent(user);
        getDAOFactory().getPasswordDAO().makePersistent(new Password("password", user));
        
        assertEquals("password", service.getPassword(user.getUsername()));
    }
    
    public void testChangePassword() {
        UserPasswordEncoder encoder = new UserPasswordEncoder();
        User user = new User();
        user.setId(1L);
        user.setAdminRole(true);
        user.setFirstName("first name");
        user.setLastName("lastName");
        user.setUsername("foobar");
        
        String old = "!@#123ADSolers2";
        
        getDAOFactory().getUserDAO().makePersistent(user);
        getDAOFactory().getPasswordDAO().makePersistent(new Password(encoder.encodePassword(old), user));
        
        try {
            service.changePassword(user.getUsername(), "incorrect oldpassword", "newpassword");
            fail();
        } catch (InvalidPasswordException expected) {
            
        }
        
        try {
            service.changePassword(user.getUsername(), old, "invalid new password");
            fail();
        } catch (InvalidPasswordException expected) {
            
        }
        
        try {
            service.changePassword(user.getUsername(), old, old);
            fail();
        } catch (PasswordReusedException expected) {
            
        }
        
        service.changePassword(user.getUsername(), old, "!@#123ADSolers1");
        assertEquals(encoder.encodePassword("!@#123ADSolers1"), service.getPassword(user.getUsername()));
    }
    
    public void testUpdatePassword() {
        UserPasswordEncoder encoder = new UserPasswordEncoder();
        User user = new User();
        user.setId(1L);
        user.setFirstName("first name");
        user.setLastName("lastName");
        user.setUsername("foobar");
        
        String old = "!@#123ADSolers2";
        
        getDAOFactory().getUserDAO().makePersistent(user);
        getDAOFactory().getPasswordDAO().makePersistent(new Password(encoder.encodePassword(old), user));
        
        try {
            service.updatePassword(user.getUsername(), "invalid new password");
            fail();
        } catch (InvalidPasswordException expected) {
            
        }
        
        try {
            service.updatePassword(user.getUsername(), old);
            fail();
        } catch (PasswordReusedException expected) {
            
        }
        
        service.updatePassword(user.getUsername(), "!@#123ADSolers1");
        assertEquals(encoder.encodePassword("!@#123ADSolers1"), service.getPassword(user.getUsername()));
    }
    
    public void testIsExpired() {
        UserPasswordEncoder encoder = new UserPasswordEncoder();
        User user = new User();
        user.setId(1L);
        user.setFirstName("first name");
        user.setLastName("lastName");
        user.setUsername("foobar");
        
        String old = "!@#123ADSolers2";
        
        Password password = new Password(encoder.encodePassword(old), user);
        
        getDAOFactory().getUserDAO().makePersistent(user);
        getDAOFactory().getPasswordDAO().makePersistent(password);
        
        assertFalse(service.isPasswordExpired(user.getUsername()));
        
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, c.get(Calendar.YEAR) - 1);
        password.setCreatedDate(c.getTime());
        
        assertTrue(service.isPasswordExpired(user.getUsername()));
    }
}
