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

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.Variant;

import com.solers.delivery.content.AllowedHostService;
import com.solers.delivery.domain.AllowedHost;
import com.solers.delivery.rest.Utils;
import com.solers.delivery.rest.converter.AllowedHostConverter;
import com.solers.util.dao.ValidationException;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class AllowedHostResource extends Resource {
    
    private static final Logger log = Logger.getLogger(AllowedHostResource.class);
    
    private final AllowedHostService service;
    private final AllowedHostConverter converter;
    
    private String alias;
    
    public AllowedHostResource(AllowedHostService service) {
        this.service = service;
        this.converter = new AllowedHostConverter();
    }
    
    @Override
    public boolean allowDelete() {
        return true;
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
        
        alias = null;
        if (request.getAttributes().containsKey("alias")) {
            alias = Reference.decode((String) request.getAttributes().get("alias"));
        }
    }

    @Override
    public Representation represent(Variant variant) {
        if (alias == null) {
            return converter.to(variant.getMediaType(), service.list());
        }
        
        AllowedHost host = service.get(alias);
        if (host == null) {
            getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
        }
        return converter.to(variant.getMediaType(), host);
    }
    
    @Override
    public void removeRepresentations() {
        if (alias == null) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return;
        }
        
        AllowedHost host = service.get(alias);
        if (host != null) {
            service.remove(host);
        } else {
            getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        }
        Utils.sendEmptyResponse(getResponse());
    }
    
    @Override
    public void storeRepresentation(Representation entity) {
        Utils.sendEmptyResponse(getResponse());
        AllowedHost host = convert(entity);
        if (host != null) {
            try {
                service.save(host);
                getResponse().setStatus(Status.SUCCESS_CREATED);
                getResponse().setEntity(converter.to(entity.getMediaType(), host.getId()));
            } catch (ValidationException ex) {
                Utils.sendValidationError(getRequest(), getResponse(), ex);
            }
        }  
    }
    
    protected AllowedHost convert(Representation entity) {
        try {
            AllowedHost host = converter.convertAllowedHost(entity);
            if (shouldSave(host)) {
                return host;
            } else {
                String message = "Invalid ID specified in entity body: " + host.getId();
                log.warn(message);
                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, message);
            }
        } catch (Exception ex) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid data specified");
            log.warn("A conversion error occurred", ex);
        }
        return null;
    }
    
    /**
     * @param host
     * @return True if the entity should be saved
     */
    protected boolean shouldSave(AllowedHost host) {
        if (alias == null) {
            return host.getId() == null;
        }
        return host.getAlias() != null && host.getAlias().equals(alias);
    }
}
