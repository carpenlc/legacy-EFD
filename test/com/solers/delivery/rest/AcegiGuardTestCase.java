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
package com.solers.delivery.rest;

import junit.framework.TestCase;

import org.restlet.data.ClientInfo;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.engine.Engine;
import org.restlet.resource.Representation;
import org.springframework.security.Authentication;
import org.springframework.security.BadCredentialsException;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;

import com.solers.delivery.acegi.MockAuthenticationProvider;
import com.solers.delivery.rest.converter.ValidationExceptionConverter;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class AcegiGuardTestCase extends TestCase {
    
    public void testCheckSecret() throws Exception {
        Engine.register(false);
        
        MockAuthenticationProvider p = new MockAuthenticationProvider();
        AcegiGuard guard = new AcegiGuard(p);
        
        Request r = new Request();
        Response response = new Response(r);
        ClientInfo info = new ClientInfo();
        info.setAddress("192.168.1.1");
        r.setClientInfo(info);
        
        assertFalse(guard.checkSecret(r, response, "bad", "pass".toCharArray()));
        assertNull(response.getEntity());
        
        assertFalse(guard.checkSecret(r, response, "throw", "pass".toCharArray()));
        assertInstanceof(response.getEntity(), BadCredentialsException.class);
        
        assertTrue(guard.checkSecret(r, response, MockAuthenticationProvider.USER, MockAuthenticationProvider.PASS.toCharArray()));
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        assertEquals(MockAuthenticationProvider.USER, auth.getName());
        assertEquals(MockAuthenticationProvider.PASS, auth.getCredentials());
        assertEquals("192.168.1.1", auth.getDetails());
    }
    
    public void testContextIsClearedBeforeAuthenticate() {
        Engine.register(false);
        
        MockAuthenticationProvider p = new MockAuthenticationProvider();
        AcegiGuard guard = new AcegiGuard(p);
        
        Request r = new Request();
        Response response = new Response(r);
        ClientInfo info = new ClientInfo();
        info.setAddress("192.168.1.1");
        r.setClientInfo(info);
        
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("id", "test"));
        
        assertFalse(guard.checkSecret(r, response, "bad", "pass".toCharArray()));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
    
    private void assertInstanceof(Representation entity, Class<?> clazz) {
        ValidationExceptionConverter converter = new ValidationExceptionConverter();
        Object result = converter.from(entity);
        assertTrue("Result is: "+result, clazz.isInstance(result));
    }
}
