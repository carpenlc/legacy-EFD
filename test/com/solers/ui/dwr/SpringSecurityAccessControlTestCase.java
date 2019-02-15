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
package com.solers.ui.dwr;

import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.AccessDeniedException;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationCredentialsNotFoundException;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class SpringSecurityAccessControlTestCase {
    
    SpringSecurityAccessControl ac;
    String scriptName = "scriptName";
    Method method = getClass().getMethods()[0];
    
    Authentication auth = new UsernamePasswordAuthenticationToken("", "", new GrantedAuthority[] {});
    
    @Before
    public void setUp() {
        ac = new SpringSecurityAccessControl();
        SecurityContextHolder.clearContext();
    }
    
    @Test
    public void testNoRestrictions() {
        ac.assertIsRestrictedByRole(scriptName, method);
    }
    
    @Test
    public void testValidRole() {
        ac.addRoleRestriction(scriptName, method.getName(), "TEST_ROLE");
        
        try {
            ac.assertIsRestrictedByRole(scriptName, method);
            Assert.fail();
        } catch (AuthenticationCredentialsNotFoundException expected) {
            
        }
        
        SecurityContext ctx = SecurityContextHolder.getContext();
        ctx.setAuthentication(auth);
        
        try {
            ac.assertIsRestrictedByRole(scriptName, method);
            Assert.fail();
        } catch (AccessDeniedException expected) {
            
        }
        
        auth = new UsernamePasswordAuthenticationToken("", "", new GrantedAuthority[] {new GrantedAuthorityImpl("OTHER_ROLE"), new GrantedAuthorityImpl("TEST_ROLE")});
        ctx.setAuthentication(auth);
        
        ac.assertIsRestrictedByRole(scriptName, method);
    }
    
    @Test
    public void testAnyRole() {
        ac.addRoleRestriction(scriptName, method.getName(), "*");
        
        try {
            ac.assertIsRestrictedByRole(scriptName, method);
            Assert.fail();
        } catch (AuthenticationCredentialsNotFoundException expected) {
            
        }
        
        SecurityContext ctx = SecurityContextHolder.getContext();
        ctx.setAuthentication(auth);
        
        ac.assertIsRestrictedByRole(scriptName, method);
    }
    
    @Test
    public void testAddMultipleRoleRestrictions() {
        ac.addRoleRestriction(scriptName, method.getName(), "ROLE_USER, *, TEST_USER");
        
        
        SecurityContext ctx = SecurityContextHolder.getContext();
        ctx.setAuthentication(auth);
        
        ac.assertIsRestrictedByRole(scriptName, method);
        
        auth = new UsernamePasswordAuthenticationToken("", "", new GrantedAuthority[] {new GrantedAuthorityImpl("ROLE_USER")});
        ac.assertIsRestrictedByRole(scriptName, method);
        
        auth = new UsernamePasswordAuthenticationToken("", "", new GrantedAuthority[] {new GrantedAuthorityImpl("TEST_USER")});
        ac.assertIsRestrictedByRole(scriptName, method);
    }
}
