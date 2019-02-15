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
package com.solers.delivery.user;

import java.util.List;
import java.util.Properties;

import org.restlet.data.MediaType;
import org.restlet.data.Response;
import org.restlet.data.Status;

import com.solers.delivery.domain.User;
import com.solers.delivery.rest.RestfulService;
import com.solers.delivery.rest.Utils;
import com.solers.delivery.rest.auth.RestAuthentication;
import com.solers.delivery.rest.converter.AdminConverter;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class RestfulUserService implements UserService {
    
    private final AdminConverter converter;
    private final RestfulService service;
    
    public RestfulUserService(String host, int port, RestAuthentication auth, Properties connectionParameters, AdminConverter converter) {
        service = new RestfulService(host, port, auth);
        service.setConnectionParameters(connectionParameters);
        this.converter = converter;
    }
    
    @Override
    public User get(String username) {
         Response response = service.get("admin/user/", username);
    	
        if (response.getStatus().equals(Status.CLIENT_ERROR_NOT_FOUND)) {
            return null;
        }
        
        Utils.checkForException(response);
        
        return converter.convertUser(response.getEntity());
    }
    
    @Override
    public User get(Long id) {
        Response response = service.get("admin/user/", id);
        if (response.getStatus().equals(Status.CLIENT_ERROR_NOT_FOUND)) {
            return null;
        }
        return converter.convertUser(response.getEntity());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<User> getUsers() {
        return (List<User>) converter.convert(service.get("admin/user/").getEntity());
    }

    @Override
    public void disable(Long id) {
        service.put("false", MediaType.TEXT_PLAIN, "admin/user/enable/", id);
    }

    @Override
    public void enable(Long id) {
        service.put("true", MediaType.TEXT_PLAIN, "admin/user/enable/", id);
    }

    @Override
    public void remove(Long id) {
        service.delete("admin/user/", id);
    }

    @Override
    public Long save(User user) {
        Response response = service.put(converter.to(MediaType.TEXT_XML, user), "admin/user/", user.getId());
        Utils.checkForException(response);
        return (Long) converter.convert(response.getEntity());
    }
}
