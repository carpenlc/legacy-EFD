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

import java.util.Calendar;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.Authentication;
import org.springframework.security.BadCredentialsException;
import org.springframework.security.CredentialsExpiredException;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.ui.WebAuthenticationDetails;

import com.solers.delivery.domain.Password;
import com.solers.delivery.domain.User;
import com.solers.delivery.rest.BaseRestTestCase;
import com.solers.delivery.rest.RestfulService;
import com.solers.delivery.rest.converter.AdminConverter;
import com.solers.security.password.MapPasswordProvider;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class RestfulAuthenticationProviderTestCase extends BaseRestTestCase {

    private RestfulAuthenticationProvider manager;
    
    @Override
    public void setUp() {
        super.setUp();
        MapPasswordProvider provider = new MapPasswordProvider();
        provider.setPassword(PasswordType.ENCRYPTION.key(), "41ba6c532588f2b03497000f22a9d504c6b41ee180a65f2c875fad715417b6ea");
        
        manager = new RestfulAuthenticationProvider(new RestfulService("localhost", getPort(), null), new AdminConverter(provider));
    }
    
    @Override
    public void tearDown() {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_THREADLOCAL);
        super.tearDown();
    }

    @Test
    public void testAuthenticate() {
        User user = new User();
        user.setId(1L);
        user.setUsername("username");
        Password password = new Password("password", user);
        
        getDAOFactory().getUserDAO().makePersistent(user);
        getDAOFactory().getPasswordDAO().makePersistent(password);
        
        Authentication result = manager.authenticate(token("username", "password"));
        Assert.assertNotNull(result);
    }
    
    @Test
    public void testInvalidAuthentication() {
        User user = new User();
        user.setId(1L);
        user.setUsername("throw");
        Password password = new Password("password", user);
        
        getDAOFactory().getUserDAO().makePersistent(user);
        getDAOFactory().getPasswordDAO().makePersistent(password);
        
        try {
            manager.authenticate(token("throw", "password"));
            Assert.fail();
        } catch (BadCredentialsException expected) {
            
        }
    }
    
    @Test
    public void testCredentialsExpired() {
        User user = new User();
        user.setId(1L);
        user.setUsername("username");
        Password password = new Password("password", user);
        
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, c.get(Calendar.YEAR) -1 );
        password.setCreatedDate(c.getTime());
        
        
        getDAOFactory().getUserDAO().makePersistent(user);
        getDAOFactory().getPasswordDAO().makePersistent(password);
        
        try {
            manager.authenticate(token("username", "password"));
            Assert.fail();
        } catch (CredentialsExpiredException expected) {
            
        }
    }
    
    @Test
    public void testContextIsClearedBeforeAuthenticate() {
        User user = new User();
        user.setId(1L);
        user.setUsername("throw");
        Password password = new Password("password", user);
        
        getDAOFactory().getUserDAO().makePersistent(user);
        getDAOFactory().getPasswordDAO().makePersistent(password);
        
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_GLOBAL);
        SecurityContextHolder.getContext().setAuthentication(token("id", "test"));
        
        try {
            manager.authenticate(token("throw", "password"));
            Assert.fail();
        } catch (BadCredentialsException expected) {
            
        }
        
        Assert.assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
    
    private Authentication token(String username, String password) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(new MockHttpSession());
        request.setRemoteAddr("127.0.0.1");
        request.setRemoteHost("localhost");
        
        UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(username, password);
        result.setDetails(new WebAuthenticationDetails(request));
        return result;
    }
}
