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
package com.solers.delivery.rest.content;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

import com.solers.delivery.content.ContentService;
import com.solers.delivery.domain.ContentSet;
import com.solers.delivery.rest.Utils;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class ContentSetResource extends ListingResource {
    
    private Long id;

    public ContentSetResource(ContentService service) {
        super(service);
    }
    
    @Override
    public boolean allowDelete() {
        return true;
    }

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        getVariants().clear();
        getVariants().add(new Variant(MediaType.TEXT_XML));
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
        this.id = Utils.findId(request, response);
    }
    
    @Override
    public Representation represent(Variant variant) {  
        ContentSet cs = service.get(id);
        if (cs == null) {
            getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
        }
        
        return converter.to(variant.getMediaType(), cs);
    }
    
    /**
     * Ensure that the entity actually exists before attempting to save
     */
    @Override
    public void storeRepresentation(Representation entity) {
        ContentSet set = service.get(id);
        if (set == null) {
            getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, id+" not found");
        } else {
            super.storeRepresentation(entity);
        }
    }
    
    @Override
    public void removeRepresentations() {
        ContentSet cs = service.get(id);
        if (cs != null) {
            service.remove(cs.getId());
        } else {
            getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        }
        Utils.sendEmptyResponse(getResponse());
    }
    
    @Override
    protected boolean shouldSave(ContentSet cs) {
        return cs.getId() != null && cs.getId().equals(id);
    }
}
