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

import org.springframework.security.ui.webapp.AuthenticationProcessingFilterEntryPoint;
import org.springframework.security.AuthenticationException;
import org.springframework.security.AuthenticationCredentialsNotFoundException;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 * This class helps to capture the exception and base on the exception type set a proper error message
 */
public class SessionTimeOutProcessing extends AuthenticationProcessingFilterEntryPoint {
    public void commence(ServletRequest request, ServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        if (authException instanceof AuthenticationCredentialsNotFoundException) {
            ((HttpServletRequest) request).getSession().setAttribute("sessionTimeOut", "The previous session has expired due to inactivity. Please try again.");
        }
        super.commence(request, response, authException);
    }
}
