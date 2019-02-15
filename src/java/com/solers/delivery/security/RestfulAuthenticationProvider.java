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

import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationException;
import org.springframework.security.providers.AuthenticationProvider;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.providers.rcp.RemoteAuthenticationException;
import org.springframework.security.ui.WebAuthenticationDetails;

import com.solers.delivery.rest.RestfulService;
import com.solers.delivery.rest.Utils;
import com.solers.delivery.rest.converter.AdminConverter;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class RestfulAuthenticationProvider implements AuthenticationProvider {
    
    private static final Logger log = Logger.getLogger(RestfulAuthenticationProvider.class);
    
    private final RestfulService service;
    private final AdminConverter converter;
    
    public RestfulAuthenticationProvider(RestfulService service, AdminConverter converter) {
        this.service = service;
        this.converter = converter;
    }

    /**
     * @see org.springframework.security.AuthenticationManager#authenticate(org.springframework.security.Authentication)
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        WebAuthenticationDetails source = (WebAuthenticationDetails) authentication.getDetails();
        RestfulAuthenticationDetails details = new RestfulAuthenticationDetails(source.getRemoteAddress(), source.getSessionId(), (String) authentication.getCredentials());
        Response response = service.put(converter.to(MediaType.TEXT_XML, details), "admin/authenticate", authentication.getName());
        Utils.checkForException(response);
        
        if (response.getStatus().equals(Status.SUCCESS_OK)) {
            return (Authentication) converter.convert(response.getEntity());
        } else {
            log.warn("Unexpected response: "+response.getStatus());
            throw new RemoteAuthenticationException("Error authenticating.  Server responded with: "+response.getStatus());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean supports(Class authentication) {
        return authentication.isAssignableFrom(UsernamePasswordAuthenticationToken.class);
    }

}
