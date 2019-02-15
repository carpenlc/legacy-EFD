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

import org.apache.log4j.Logger;
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
import com.solers.delivery.rest.Utils;
import com.solers.delivery.rest.converter.ContentSetConverter;
import com.solers.util.dao.ValidationException;

/**
 * Lists the available consumer and supplier content sets
 * 
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class ListingResource extends Resource {
    
    private static final Logger logger = Logger.getLogger(ListingResource.class);
    
    protected final ContentService service;
    protected final ContentSetConverter converter;
    
    public ListingResource(ContentService service) {
        this.service = service;
        this.converter = new ContentSetConverter();
    }

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        getVariants().add(new Variant(MediaType.TEXT_XML));
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
    }
    
    @Override
    public Representation represent(Variant variant) {
        return converter.to(variant.getMediaType(), service.getContentSets());
    }
    
    @Override
    public boolean allowPut() {
        return true;
    }
    
    @Override
    public void storeRepresentation(Representation entity) {
        Utils.sendEmptyResponse(getResponse());
        ContentSet cs = convert(entity);
        if (cs != null) {
            try {
                service.save(cs);
                getResponse().setStatus(Status.SUCCESS_CREATED);
                getResponse().setEntity(converter.to(entity.getMediaType(), cs.getId()));
            } catch (ValidationException ex) {
                Utils.sendValidationError(getRequest(), getResponse(), ex);
            }
        }  
    }
    
    protected ContentSet convert(Representation entity) {
        try {
            ContentSet cs = converter.from(entity);
            if (shouldSave(cs)) {
                return cs;
            } else {
                String message = "Invalid ID specified in entity body: " + cs.getId();
                logger.warn(message);
                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, message);
            }
        } catch (Exception ex) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid data specified");
            logger.warn("An error occurred converting a content set", ex);
        }
        return null;
    }
    
    /**
     * @param cs
     * @return True if the entity should be saved
     */
    protected boolean shouldSave(ContentSet cs) {
        return cs.getId() == null;
    }
}
