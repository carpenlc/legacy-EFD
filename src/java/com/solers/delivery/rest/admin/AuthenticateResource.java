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
package com.solers.delivery.rest.admin;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.Variant;
import org.springframework.security.Authentication;
import org.springframework.security.CredentialsExpiredException;
import org.springframework.security.SpringSecurityException;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.ProviderManager;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;

import com.solers.delivery.rest.Utils;
import com.solers.delivery.rest.converter.AdminConverter;
import com.solers.delivery.security.RestfulAuthenticationDetails;
import com.solers.delivery.user.PasswordService;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class AuthenticateResource extends Resource {
    
    private final AdminConverter converter;
    private final ProviderManager provider;
    private final PasswordService passwordService;
    
    private String username;
    
    public AuthenticateResource(AdminConverter converter, ProviderManager provider, PasswordService passwordService) {
        this.converter = converter;
        this.provider = provider;
        this.passwordService = passwordService;
    }
    
    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        getVariants().add(new Variant(MediaType.TEXT_XML));
        
        username = Utils.findString(request, response, "username");
    }
    
    @Override
    public boolean allowPut() {
        return true;
    }
    
    @Override
    public boolean allowGet() {
        return false;
    }
    
    @Override
    public void storeRepresentation(Representation entity) {
        SecurityContextHolder.clearContext();
        RestfulAuthenticationDetails details = (RestfulAuthenticationDetails) converter.convert(entity);
        
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, details.getPassword());
        token.setDetails(details);
        
        try {
            Authentication result = provider.doAuthentication(token);
            if (passwordService.isPasswordExpired(username)) {
                throw new CredentialsExpiredException("User credentials have expired");
            }
            getResponse().setEntity(converter.to(MediaType.TEXT_XML, result));
        } catch (SpringSecurityException ex) {
            getResponse().setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            Utils.setException(getRequest(), getResponse(), ex);
        }
    }
    
}
