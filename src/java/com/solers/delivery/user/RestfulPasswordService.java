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

import org.restlet.data.MediaType;
import org.restlet.data.Response;
import org.restlet.data.Status;

import com.solers.delivery.rest.RestfulService;
import com.solers.delivery.rest.Utils;
import com.solers.delivery.rest.auth.RestAuthentication;
import com.solers.delivery.rest.converter.AdminConverter;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class RestfulPasswordService extends RestfulService implements PasswordService {

    private final AdminConverter converter;
    
    public RestfulPasswordService(String host, int port, RestAuthentication auth, AdminConverter converter) {
        super(host, port, auth);
        this.converter = converter;
    }
    
    @Override
    public String getPassword(String username) {
        Response response = get("admin/password/", username);
        if (response.getStatus().equals(Status.CLIENT_ERROR_NOT_FOUND)) {
            return null;
        }
        Object [] values = (Object[]) converter.convert(response.getEntity());
        if (values == null) {
            return null;
        }
        return (String) values[0];
    }

    @Override
    public void changePassword(String username, String oldpassword, String newPassword) {
        String [] pws = new String[] { oldpassword, newPassword };
        Response response = put(converter.to(MediaType.TEXT_XML, pws), "admin/password/", username);
        Utils.checkForException(response);
    }

    @Override
    public boolean isPasswordExpired(String username) {
        Object [] values = (Object[]) converter.convert(get("admin/password/", username).getEntity());
        if (values == null) {
            return false;
        }
        return (Boolean) values[1];
    }

    @Override
    public void updatePassword(String username, String newPassword) {
        String [] pws = new String[] { newPassword };
        Response response = put(converter.to(MediaType.TEXT_XML, pws), "admin/password/", username);
        Utils.checkForException(response);
    }
}
