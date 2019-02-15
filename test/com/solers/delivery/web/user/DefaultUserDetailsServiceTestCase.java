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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;

import com.solers.delivery.daos.DAOFactory;
import com.solers.delivery.daos.MockDAOFactory;
import com.solers.delivery.domain.Password;
import com.solers.delivery.domain.User;
import com.solers.delivery.user.DefaultUserDetailsService;
import com.solers.delivery.user.PasswordServiceImpl;
import com.solers.delivery.user.UserServiceImpl;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class DefaultUserDetailsServiceTestCase {
    
    private PasswordServiceImpl passwordService;
    private UserServiceImpl userService;
    private DAOFactory factory;
    private UserDetailsService service;
    
    @Before
    public void setUp() {
        factory = new MockDAOFactory();
        passwordService = new PasswordServiceImpl(factory);
        passwordService.setPasswordExpirationDays(10);
        userService = new UserServiceImpl(factory);
        service = new DefaultUserDetailsService(userService, passwordService);
        
        User user = new User();
        user.setId(1l);
        user.setUsername("username");
        user.setInitialPassword(false);
        factory.getUserDAO().makePersistent(user);
        
        Password password = new Password("password", user);
        factory.getPasswordDAO().makePersistent(password);
    }
    
    @Test
    public void testLoadUserByUsername() {
        UserDetails u = service.loadUserByUsername("username");
        
        Assert.assertTrue(u.isAccountNonExpired());
        Assert.assertTrue(u.isCredentialsNonExpired());
        Assert.assertTrue(u.isAccountNonLocked());
        Assert.assertFalse(u.isEnabled());
        
        passwordService.setPasswordExpirationDays(-3);
        u = service.loadUserByUsername("username");
        Assert.assertTrue(u.isCredentialsNonExpired());
        
        factory.getUserDAO().getUserByUsername("username").setEnabled(true);
        u = service.loadUserByUsername("username");
        Assert.assertTrue(u.isEnabled());
    }
}
