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
import java.util.Collection;
import java.util.Set;

import org.apache.log4j.Logger;
import org.directwebremoting.impl.DefaultAccessControl;
import org.springframework.security.AccessDeniedException;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationCredentialsNotFoundException;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class SpringSecurityAccessControl extends DefaultAccessControl {
    
    private static final Logger log = Logger.getLogger(SpringSecurityAccessControl.class);
    
    @Override
    public void addRoleRestriction(String scriptName, String methodName, String role) {
        for (String r : role.split(",")) {
            super.addRoleRestriction(scriptName, methodName, r.trim());
        }
    }

    @Override
    protected void assertIsRestrictedByRole(String scriptName, Method method) {
        String methodName = method.getName();
        
        Set<String> roles = getRoleRestrictions(scriptName, methodName);
        if (roles != null && !roles.isEmpty()) {
            GrantedAuthority[] authorities = getAuthorities(scriptName, methodName);
            boolean allowed = false;
            for (String role : roles) {
                if (isAllowed(role, authorities)) {
                    allowed = true;
                    break;
                }
            }
            if (!allowed) {
                log.warn(String.format("Insufficient access to %s.%s", scriptName, methodName));
                throw new AccessDeniedException("Insufficient permissions for the requested method");
            }
        }
    }
    
    private GrantedAuthority[] getAuthorities(String scriptName, String methodName) {
        SecurityContext ctx = SecurityContextHolder.getContext();
        if (ctx != null) {
            Authentication auth = ctx.getAuthentication();
            if (auth != null) {
                return auth.getAuthorities();
            }
        }
        log.warn(String.format("User is not logged in and trying to access %s.%s", scriptName, methodName));
        throw new AuthenticationCredentialsNotFoundException("Not logged in");
    }
    
    private boolean isAllowed(String role, GrantedAuthority[] auths) {
        if (role.equals("*")) {
            return true;
        }
        
        if (auths == null) {
            return false;
        }
        
        for (GrantedAuthority auth : auths) {
            if (auth.getAuthority().equals(role)) {
                return true;
            }
        }
        
        return false;
    }
}
