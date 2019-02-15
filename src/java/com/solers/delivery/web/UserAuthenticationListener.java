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
package com.solers.delivery.web;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.Authentication;
import org.springframework.security.event.authentication.AbstractAuthenticationEvent;
import org.springframework.security.event.authentication.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.event.authentication.AuthenticationSuccessEvent;

import com.solers.delivery.alerts.AlertManager;
import com.solers.delivery.domain.Alert;
import com.solers.delivery.domain.User;
import com.solers.delivery.domain.Alert.AlertType;
import com.solers.delivery.security.RestfulAuthenticationDetails;
import com.solers.delivery.user.UserService;

public class UserAuthenticationListener implements ApplicationListener {

    private UserService userService;
    private int maxFailedPasswords;
    private AlertManager alertManager;
    
    public void setMaxFailedPasswords( int maxFailedPasswords) {
        this.maxFailedPasswords = maxFailedPasswords;
    }
    
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
    
    public void setAlertManager(AlertManager alertManager) {
        this.alertManager = alertManager;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof AbstractAuthenticationEvent) {
            Authentication auth = ((AbstractAuthenticationEvent) event).getAuthentication();
            String userName = auth.getName();

            if (event instanceof AuthenticationFailureBadCredentialsEvent) {
                User user = userService.get(userName);
                if (user != null) {
                    user.updateFailedLogin();
                    if (user.getFailedLogins() >= maxFailedPasswords && user.isEnabled()) {
                        user.setEnabled(false);
                        alertManager.onAlert(new Alert(user.getUsername()+"'s account has been disabled after "+user.getFailedLogins()+" failed login attempts", AlertType.ADMIN));
                    }
                    userService.save(user);
                }
            } else if (event instanceof AuthenticationSuccessEvent) {               
                User user = userService.get(userName); 
                RestfulAuthenticationDetails details = (RestfulAuthenticationDetails) auth.getDetails();
                user.updateLastLogin(details.getRemoteAddress());
                userService.save(user);
            }
        }
    }
}

