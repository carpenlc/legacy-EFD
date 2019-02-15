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
package com.solers.delivery.rest.reports;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.Variant;

import com.solers.delivery.content.ContentService;
import com.solers.delivery.domain.ContentSet;
import com.solers.delivery.management.ConsumerStatus;
import com.solers.delivery.management.StatusService;
import com.solers.delivery.management.SupplierStatus;
import com.solers.delivery.rest.Utils;
import com.solers.delivery.rest.converter.StatusConverter;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class StatusResource extends Resource {
    
    private final StatusService status;
    private final ContentService service;
    private final StatusConverter converter;
    
    private long id;
    
    public StatusResource(StatusService status, ContentService service) {
        this.status = status;
        this.service = service;
        this.converter = new StatusConverter();
    }
    
    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        getVariants().add(new Variant(MediaType.TEXT_XML));
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
        this.id = Utils.findId(request, response);
    }
    
    @Override
    public Representation represent(Variant variant) {
        ContentSet cs = service.get(id);
        
        if (cs == null) {
            getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, id + " not found");
            return null;
        }
        
        if (cs.isSupplier()) {
            SupplierStatus supplier = status.getSupplierStatus(id);
            return converter.to(variant.getMediaType(), supplier);
        }
        
        ConsumerStatus consumer = status.getConsumerStatus(id);
        return converter.to(variant.getMediaType(), consumer);
    }
}
