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
package com.solers.delivery.web.remoting;

import java.util.Arrays;

import org.directwebremoting.WebContextFactory;
import org.directwebremoting.impl.DefaultContainer;
import org.directwebremoting.impl.DefaultWebContextBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.Authentication;
import org.springframework.security.BadCredentialsException;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.ProviderManager;

import com.solers.delivery.acegi.MockAuthentication;
import com.solers.delivery.acegi.MockAuthenticationProvider;
import com.solers.delivery.daos.DAOFactory;
import com.solers.delivery.daos.MockDAOFactory;
import com.solers.delivery.domain.User;
import com.solers.delivery.user.PasswordServiceImpl;
import com.solers.delivery.user.UserService;
import com.solers.delivery.user.UserServiceImpl;
import com.solers.delivery.util.password.InvalidPasswordException;
import com.solers.delivery.util.password.UserPasswordEncoder;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class SystemHelperTestCase {
    
    private SystemHelper helper;
    private PasswordServiceImpl passwordService;
    private User user;
    private UserPasswordEncoder encoder;
    private ProviderManager authManager;
    private UserService userService;
    private DAOFactory factory;
    
    @Before
    public void setUp() {
        factory = new MockDAOFactory();
        encoder = new UserPasswordEncoder();
        authManager = new ProviderManager();
        authManager.setProviders(Arrays.asList(new MockAuthenticationProvider()));
        passwordService = new PasswordServiceImpl(factory);
        passwordService.setPasswordExpirationDays(5);
        userService = new UserServiceImpl(factory);
        helper = new SystemHelper(authManager, userService, passwordService);
        
        user = new User();
        user.setUsername("user");
        user.setFirstName("first");
        user.setLastName("last");
        user.setAdminRole(true);
        factory.getUserDAO().makePersistent(user);
        
        passwordService.updatePassword(user.getUsername(), "!@#123ADSolers1");
        
        final DefaultWebContextBuilder wb = new DefaultWebContextBuilder();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(new MockHttpSession());
        wb.engageThread(new DefaultContainer(), request, null);
        WebContextFactory.attach(new DefaultContainer() {
            public Object getBean(String id) {
                return wb;
            }
        });
        
        SecurityContext ctx = SecurityContextHolder.getContext();
        ctx.setAuthentication(new MockAuthentication(user.getUsername(), "!@#123ADSolers1", true));
    }
    
    @Test
    public void testLoginOK() {
        SecurityContextHolder.clearContext();
        User result = helper.login(user.getUsername(), "!@#123ADSolers1");
        Assert.assertEquals(user.getUsername(), result.getUsername());
        
        SecurityContext ctx = SecurityContextHolder.getContext();
        Authentication auth = ctx.getAuthentication();
        Assert.assertEquals("!@#123ADSolers1", auth.getCredentials());
        Assert.assertEquals(user.getUsername(), auth.getName());
    }
    
    @Test
    public void testInvalidLogin() {
        SecurityContextHolder.clearContext();
        try {
            helper.login("throw", "invalid");
            Assert.fail();
        } catch (BadCredentialsException expected) {
            
        }
    }
    
    @Test
    public void testChangePassword() throws Exception { 
        Assert.assertEquals(encoder.encodePassword("!@#123ADSolers1"), passwordService.getPassword(user.getUsername()));
        try {
            helper.changePassword("old", "new", "different");
            Assert.fail();
        } catch (InvalidPasswordException expected) {
            
        }
        
        try {
            helper.changePassword("old", "new", "new");
            Assert.fail();
        } catch (InvalidPasswordException expected) {
            
        }
        
        // Password shouldn't have changed
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Assert.assertEquals("!@#123ADSolers1", auth.getCredentials());
        
        Thread.sleep(200);
        helper.changePassword("!@#123ADSolers1", "!@#123ADSolers2", "!@#123ADSolers2");
        
        Assert.assertEquals(encoder.encodePassword("!@#123ADSolers2"), passwordService.getPassword(user.getUsername()));
        
        auth = SecurityContextHolder.getContext().getAuthentication();
        Assert.assertEquals("!@#123ADSolers2", auth.getCredentials());
    }
}
