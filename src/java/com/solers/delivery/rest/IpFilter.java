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

import java.util.Collection;

import org.apache.log4j.Logger;
import org.restlet.Filter;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class IpFilter extends Filter {
    
    private static final Logger logger = Logger.getLogger(IpFilter.class);
    
    private final Collection<String> addresses;
    
    public IpFilter(Collection<String> addresses) {
        this.addresses = addresses;
    }

    @Override
    protected int beforeHandle(Request request, Response response) {
        String ip = request.getClientInfo().getAddress();
        
        if (addresses.contains(ip)) {
            return super.beforeHandle(request, response);
        } else {
            logger.warn(ip+" was denied access in IpFilter");
            response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
            return Filter.STOP;
        }
    }
}
