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
package com.solers.delivery.content;

import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.data.Response;
import org.restlet.data.Status;

import com.solers.delivery.domain.AllowedHost;
import com.solers.delivery.rest.RestfulService;
import com.solers.delivery.rest.Utils;
import com.solers.delivery.rest.auth.RestAuthentication;
import com.solers.delivery.rest.converter.AllowedHostConverter;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class RestfulAllowedHostService extends RestfulService implements AllowedHostService {

    private final AllowedHostConverter converter;
    
    public RestfulAllowedHostService(String host, int port, RestAuthentication auth) {
        super(host, port, auth);
        converter = new AllowedHostConverter();
    }

    @Override
    public AllowedHost get(String alias) {
        Response response = super.get("admin/allowedHost/", alias);
        if (response.getStatus().equals(Status.CLIENT_ERROR_NOT_FOUND)) {
            return null;
        }
        return converter.convertAllowedHost(response.getEntity());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<AllowedHost> list() {
        return (List<AllowedHost>) converter.convert(super.get("admin/allowedHost/").getEntity());
    }

    @Override
    public void remove(AllowedHost host) {
        delete("admin/allowedHost/", host.getAlias());
    }

    @Override
    public Long save(AllowedHost host) {
        Response response = put(converter.to(MediaType.TEXT_XML, host), "admin/allowedHost/", host.getAlias());
        Utils.checkForException(response);
        return (Long) converter.convert(response.getEntity());
    }
}
