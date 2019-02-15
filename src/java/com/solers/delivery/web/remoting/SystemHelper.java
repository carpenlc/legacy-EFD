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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.directwebremoting.WebContextFactory;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationCredentialsNotFoundException;
import org.springframework.security.AuthenticationException;
import org.springframework.security.CredentialsExpiredException;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.ProviderManager;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.ui.WebAuthenticationDetails;
import org.springframework.security.ui.webapp.AuthenticationProcessingFilter;

import com.solers.delivery.domain.User;
import com.solers.delivery.user.PasswordService;
import com.solers.delivery.user.UserService;
import com.solers.delivery.util.password.InvalidPasswordException;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class SystemHelper {
    
    private static final Logger log = Logger.getLogger(SystemHelper.class);
    
    private final ProviderManager auth;
    private final UserService userService;
    private final PasswordService passwordService;

    public SystemHelper(ProviderManager auth, UserService userService, PasswordService passwordService) {
        this.auth = auth;
        this.userService = userService;
        this.passwordService = passwordService;
    }
    
    public void changePassword(String oldpw, String newpw, String newpwconfirm) {
        Authentication auth = getAuth();
        String username = auth.getName();
        
        if (!newpw.equals(newpwconfirm)) {
            throw new InvalidPasswordException("Passwords do not match");
        }
        
        passwordService.changePassword(username, oldpw, newpw);
        login(username, newpw);
    }
    
    public User login(String username, String password) throws AuthenticationException {
        getSession().setAttribute(AuthenticationProcessingFilter.SPRING_SECURITY_LAST_USERNAME_KEY, username);
 
        UsernamePasswordAuthenticationToken request = new UsernamePasswordAuthenticationToken(username, password);
        request.setDetails(new WebAuthenticationDetails(getRequest()));
        SecurityContextHolder.getContext().setAuthentication(request);
        
        try {
            Authentication result = auth.doAuthentication(request);
            SecurityContextHolder.getContext().setAuthentication(result);
            return userService.get(username);
        } catch (CredentialsExpiredException ex) {
            // There needs to be an auth value
            // for the user to change his password
            SecurityContextHolder.getContext().setAuthentication(request);
            throw ex;
        } catch (RuntimeException ex) {
            log.error("Unhandled exception while logging in", ex);
            throw ex;
        }
    }
    
    public void logout() {
        getSession().invalidate();
    }
    
    /**
     * Heartbeat method.  The client sends this periodically to keep its
     * session alive.
     */
    public void heartbeat() {
        
    }
    
    private HttpSession getSession() {
        return WebContextFactory.get().getSession();
    }     
    
    private HttpServletRequest getRequest() {
        return WebContextFactory.get().getHttpServletRequest();
    }
    
    private Authentication getAuth() {
        SecurityContext ctx = SecurityContextHolder.getContext();
        if (ctx != null) {
            Authentication auth = ctx.getAuthentication();
            if (auth != null) {
                return auth;
            }
        }
        throw new AuthenticationCredentialsNotFoundException("Not logged in");
    }
}
