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

import com.solers.delivery.domain.User;
import com.solers.delivery.rest.Utils;
import com.solers.delivery.rest.converter.AdminConverter;
import com.solers.delivery.user.PasswordService;
import com.solers.delivery.user.UserService;
import com.solers.delivery.util.password.InvalidPasswordException;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class PasswordResource extends Resource {
    
    private final PasswordService service;
    private final UserService users;
    private final AdminConverter converter;
    
    private String username;
    
    public PasswordResource(PasswordService service, UserService users, AdminConverter converter) {
        this.service = service;
        this.users = users;
        this.converter = converter;
    }
    
    @Override
    public boolean allowPut() {
        return true;
    }
    
    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        getVariants().add(new Variant(MediaType.TEXT_XML));
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
        
        username = Utils.findString(request, response, "username");
    }
    
    @Override
    public Representation represent(Variant variant) {
        User user = users.get(username);
        if (user == null) {
            getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
        }
        
        Object [] values = new Object[] { service.getPassword(username), service.isPasswordExpired(username) };
        return converter.to(variant, values);
    }
    
    @Override
    public void storeRepresentation(Representation entity) {
        Utils.sendEmptyResponse(getResponse());
        
        User user = users.get(username);
  
        if (user == null) {
            getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return;
        }
        
        String [] passwords = converter.convertPasswords(entity);
        
        try {
            if (changingPassword(passwords)) {
                String oldpassword = passwords[0];
                String newPassword = passwords[1];
                service.changePassword(username, oldpassword, newPassword);
            } else {
                String newPassword = passwords[0];
                service.updatePassword(username, newPassword);
            }
        } catch (InvalidPasswordException ex) {
            Utils.sendValidationError(getRequest(), getResponse(), ex);
        }
    }
    
    private boolean changingPassword(String [] passwords) {
        return passwords.length == 2;
    }
}
