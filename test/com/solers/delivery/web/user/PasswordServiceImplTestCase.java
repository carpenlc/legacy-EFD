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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;

import com.solers.delivery.acegi.MockAuthentication;
import com.solers.delivery.daos.DAOFactory;
import com.solers.delivery.daos.MockDAOFactory;
import com.solers.delivery.domain.Password;
import com.solers.delivery.domain.User;
import com.solers.delivery.user.PasswordServiceImpl;
import com.solers.delivery.util.password.InvalidPasswordException;
import com.solers.delivery.util.password.UserPasswordEncoder;
import com.solers.util.unit.TimeIntervalUnit;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class PasswordServiceImplTestCase {
    
    private PasswordServiceImpl passwordService;
    private DAOFactory factory;
    private User user;
    private Password password;
    private User adminUser;
    
    @Before
    public void setUp() {
        factory = new MockDAOFactory();
        passwordService = new PasswordServiceImpl(factory);
        
        user = new User();
        user.setId(1l);
        user.setLastName("last");
        user.setFirstName("first");
        user.setUsername("foo");
        
        adminUser = new User();
       adminUser.setId(2L);
       adminUser.setUsername("nothing");
       adminUser.setFirstName("nothing");
       adminUser.setLastName("much");
       adminUser.setAdminRole(true);
        
        password = new Password(new UserPasswordEncoder().encodePassword("password"), user);
        factory.getPasswordDAO().makePersistent(password);
        factory.getUserDAO().makePersistent(user);
        factory.getUserDAO().makePersistent(adminUser);
    
    }
    
    @Test
    public void testMaxUserChanges() throws Exception {
        Calendar tenDaysAgo = Calendar.getInstance();
        tenDaysAgo.set(Calendar.DAY_OF_YEAR, tenDaysAgo.get(Calendar.DAY_OF_YEAR) - 10);
        password.setCreatedDate(tenDaysAgo.getTime());
        
        passwordService.setMaxUserChanges(2);
        passwordService.setUserChangesInterval((int)TimeIntervalUnit.DAYS.toMillis(2));
        
        passwordService.changePassword(user.getUsername(), "password", "!@#123ADSolers1");
        Thread.sleep(200);
        passwordService.changePassword(user.getUsername(), "!@#123ADSolers1", "!@#123ADSolers2");
        Thread.sleep(200);
        
        try {
            passwordService.changePassword(user.getUsername(), "!@#123ADSolers2", "!@#123ADSolers3");
            Assert.fail();
        } catch (InvalidPasswordException expected) {
            
        }
    }
    
    @Test
    public void testMaxUserChangesAdmin() throws Exception {
        Calendar tenDaysAgo = Calendar.getInstance();
        tenDaysAgo.set(Calendar.DAY_OF_YEAR, tenDaysAgo.get(Calendar.DAY_OF_YEAR) - 10);
        password.setCreatedDate(tenDaysAgo.getTime());
        
        passwordService.setMaxUserChanges(2);
        passwordService.setUserChangesInterval((int)TimeIntervalUnit.DAYS.toMillis(2));
        
        user.setAdminRole(true);
        
        passwordService.changePassword(user.getUsername(), "password", "!@#123ADSolers1");
        Thread.sleep(200);
        passwordService.changePassword(user.getUsername(), "!@#123ADSolers1", "!@#123ADSolers2");
        Thread.sleep(200);
        passwordService.changePassword(user.getUsername(), "!@#123ADSolers2", "!@#123ADSolers3");
   
    }
    

    @Test
    public void testMaxUserChangesAdminCreated() throws Exception {
        Calendar tenDaysAgo = Calendar.getInstance();
        tenDaysAgo.set(Calendar.DAY_OF_YEAR, tenDaysAgo.get(Calendar.DAY_OF_YEAR) - 10);
        password.setCreatedDate(tenDaysAgo.getTime());
        
        passwordService.setMaxUserChanges(2);
        passwordService.setUserChangesInterval((int)TimeIntervalUnit.DAYS.toMillis(2));
        
        user.setAdminRole(false);
        
        passwordService.changePassword(user.getUsername(), "password", "!@#123ADSolers1");
        Thread.sleep(200);
        
        SecurityContext ctx = SecurityContextHolder.getContext();
        ctx.setAuthentication(new MockAuthentication(adminUser.getUsername(), "!@#123ADSolers1", true));
        
        passwordService.changePassword(user.getUsername(), "!@#123ADSolers1", "!@#123ADSolers2");
        Thread.sleep(200);
  
        ctx.setAuthentication(new MockAuthentication(user.getUsername(), "!@#123ADSolers1", true));
  
        passwordService.changePassword(user.getUsername(), "!@#123ADSolers2", "!@#123ADSolers3");
    }

    
    @Test
    public void testIsExpiredOnInitialPassword() {
        passwordService.setPasswordExpirationDays(10);
        Assert.assertFalse(passwordService.isPasswordExpired(user.getUsername()));
        user.setInitialPassword(true);
        Assert.assertTrue(passwordService.isPasswordExpired(user.getUsername()));
    }
    
    @Test
    public void testIsExpiredByDate() {
        user.setInitialPassword(false);
        passwordService.setPasswordExpirationDays(-2);
        Assert.assertTrue(passwordService.isPasswordExpired(user.getUsername()));
        
        passwordService.setPasswordExpirationDays(0);
        Assert.assertTrue(passwordService.isPasswordExpired(user.getUsername()));
    }
    
    @Test
    public void testNotExpired() {
        user.setInitialPassword(false);
        passwordService.setPasswordExpirationDays(5);
        Assert.assertFalse(passwordService.isPasswordExpired(user.getUsername()));
    }
}
