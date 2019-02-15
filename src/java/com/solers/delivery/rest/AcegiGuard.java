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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.restlet.Guard;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationException;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.AuthenticationProvider;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import com.solers.delivery.alerts.AlertManager;  
import com.solers.delivery.user.UserService;

import com.solers.delivery.Start;
import com.solers.delivery.domain.Alert;
import com.solers.delivery.domain.User;
import com.solers.delivery.domain.Alert.AlertType;

/**
 * Guard class that delegates authentication to Acegi
 * 
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 * @author Tim Heintz (revised for three logins)
 */
public class AcegiGuard extends Guard {
    
	public static final int MAX_FAILED_PASSWORDS = 3;
    private static final Logger log = Logger.getLogger(AcegiGuard.class);
    
    private final AuthenticationProvider provider;
    private final List<String> ignored;
    
    /**
     * The AcegiGuard uses an AuthenticationProvider instead of AuthenticationManager
     * so that AuthenticationEvents are not published through Spring
     * 
     * @param provider
     */
    public AcegiGuard(AuthenticationProvider provider) {
        super(null, ChallengeScheme.HTTP_BASIC, "EFD");
        this.provider = provider;
        this.ignored = new ArrayList<String>();
    }
    
    public void setIgnored(List<String> values) {
        ignored.addAll(values);
    }
    
    // TestPros, July 2010, Version 2.1.1 - notify to enforce REST account disabling after 3 failed logins
    @Override
    public boolean checkSecret(Request request, Response response, String identifier, char[] secret) {
        boolean authenticated = false;
        SecurityContextHolder.clearContext();
        UsernamePasswordAuthenticationToken usernameToken = new UsernamePasswordAuthenticationToken(identifier, new String(secret));
        usernameToken.setDetails(request.getClientInfo().getAddress());
    	String userName = usernameToken.getName();
    	
    	// check to see if user is enabled
    	ConfigurableApplicationContext context = Start.getDeliveryService().getContext();

    	UserService userService = (UserService) context.getBean("userService");
		User user = userService.get(userName);
		if (user == null)  {
	        log.warn("User does not exist: Authentication  failed for user: " + identifier);
			return false;
		}
		if (!user.isEnabled()) {
            log.warn("User is Disabled: Authentication  failed for user: " + identifier);
            return false;
		}
        
		try {
            Authentication token = provider.authenticate(usernameToken);
            authenticated = token != null && token.isAuthenticated();
            if (authenticated) {
                SecurityContextHolder.getContext().setAuthentication(usernameToken);
            }
        } catch (AuthenticationException ex) {
            log.warn("Authentication failed for user: " + identifier);
            if (log.isDebugEnabled()) {
                log.debug("Authentication exception was: "+ex.getMessage(), ex);
            }
            Utils.setException(request, response, ex);
            authenticated = false;
        } catch (Exception ex) {
            log.error("An error occurred authenicating", ex);
            throw new RuntimeException(ex);
        }
        // apply 3 logout rule
        if (! authenticated) {
   	        user.updateFailedLogin();
   	        if (user.getFailedLogins() >= MAX_FAILED_PASSWORDS && user.isEnabled()) {
   	            user.setEnabled(false);
   	            AlertManager alertManager = (AlertManager) context.getBean("alertManager");
   	            alertManager.onAlert(new Alert(user.getUsername()+"'s account has been disabled after "+user.getFailedLogins()+" failed login attempts", AlertType.ADMIN));
   	        }
    	    userService.save(user);
        }
        
        return authenticated;
    }

    @Override
    public int authenticate(Request request, Response response) {
        for (String value : ignored) {
            if (request.getResourceRef().getPath().startsWith(value)) {
                return Guard.AUTHENTICATION_VALID;
            }
        }
        return super.authenticate(request, response);
    }
}
