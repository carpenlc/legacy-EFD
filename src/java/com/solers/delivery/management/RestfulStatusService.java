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
package com.solers.delivery.management;

import java.io.IOException;
import java.util.List;

import com.solers.delivery.content.status.CurrentSynchronization;
import com.solers.delivery.rest.RestfulService;
import com.solers.delivery.rest.auth.RestAuthentication;
import com.solers.delivery.rest.converter.StatusConverter;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class RestfulStatusService extends RestfulService implements StatusService {
    
    private final StatusConverter converter;
    
    public RestfulStatusService(String host, int port, RestAuthentication auth) {
        super(host, port, auth);
        this.converter = new StatusConverter();
    }

    @Override
    public ConsumerStatus getConsumerStatus(Long contentSetId) {
        return convert(ConsumerStatus.class, contentSetId);
    }

    @Override
    public SupplierStatus getSupplierStatus(Long contentSetId) {
        return convert(SupplierStatus.class, contentSetId);
    }
    
    @Override
    public List<CurrentSynchronization> getCurrentSynchronizations() {
        try {
            return converter.fromList(get("reports/status/current").getEntity());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected <T> T convert(Class<T> type, Long contentSetId) {
        try {
            return converter.from(get("reports/status/content/", contentSetId).getEntity(), type);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
