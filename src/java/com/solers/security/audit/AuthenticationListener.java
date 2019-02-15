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
package com.solers.security.audit;

import org.springframework.security.Authentication;
import org.springframework.security.event.authentication.AbstractAuthenticationEvent;
import org.springframework.security.event.authentication.AbstractAuthenticationFailureEvent;
import org.springframework.security.ui.session.HttpSessionApplicationEvent;
import org.springframework.security.ui.session.HttpSessionDestroyedEvent;
import org.springframework.security.ui.webapp.AuthenticationProcessingFilter;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import com.solers.security.audit.Auditable.Severity;

public class AuthenticationListener implements ApplicationListener {
    
    protected static final String CURRENT_USER_LOGGED_IN = AuthenticationProcessingFilter.SPRING_SECURITY_LAST_USERNAME_KEY;
    
    private AuditLogger audit;
    
    public void setAuditLogger(AuditLogger audit) {
        this.audit = audit;
    }
    
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof AbstractAuthenticationEvent) {
            Authentication auth = ((AbstractAuthenticationEvent) event).getAuthentication(); 
            if (auth == null) {
                audit.log("Authentication information not available", Severity.HIGH);
            } else {
                if (event instanceof AbstractAuthenticationFailureEvent) {
                    audit.log(String.format("Authentication FAILED (%s)", ((AbstractAuthenticationFailureEvent) event).getException().getMessage()), Severity.HIGH, auth);
                } else {
                    audit.log("Authentication SUCCEEDED", Severity.LOW, auth);
                }
            }
        } else if (event instanceof HttpSessionApplicationEvent) { //This is not the best place for this but it will do for now.

            if (event instanceof HttpSessionDestroyedEvent) {
                String userName = (String) ((HttpSessionDestroyedEvent) event).getSession().getAttribute(CURRENT_USER_LOGGED_IN);
                String message = String.format("User Session Ended for User: %s", userName);
                audit.log(message, Severity.HIGH);
            }
        }
    }
}
