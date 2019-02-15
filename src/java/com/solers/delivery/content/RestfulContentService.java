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

import java.util.ArrayList;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.data.Response;
import org.restlet.data.Status;

import com.solers.delivery.domain.ConsumerContentSet;
import com.solers.delivery.domain.ContentSet;
import com.solers.delivery.rest.RestfulService;
import com.solers.delivery.rest.Utils;
import com.solers.delivery.rest.auth.RestAuthentication;
import com.solers.delivery.rest.converter.ContentSetConverter;
import com.solers.util.dao.ValidationException;

/**
 * REST implementation of ContentService
 * 
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class RestfulContentService extends RestfulService implements ContentService {
    
    private final ContentSetConverter converter;
    
    public RestfulContentService(String host, int port, RestAuthentication auth) {
        super(host, port, auth);
        this.converter = new ContentSetConverter();
    }

    @Override
    public void disable(Long id) {
        put("false", MediaType.TEXT_PLAIN, "op/enable/", id);
    }

    @Override
    public void enable(Long id) {
        put("true", MediaType.TEXT_PLAIN, "op/enable/", id);
    }
    
    @Override
    public void remove(Long id) {
        delete("content/", id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends ContentSet> T get(Long id) {
        Response response = get("content/", id);
        if (response.getStatus().equals(Status.CLIENT_ERROR_NOT_FOUND)) {
            return null;
        }
        return (T) converter.from(response.getEntity());
    }

    @Override
    public List<ContentSet> getContentSets() {
        return converter.fromList(get("content/").getEntity());
    }
    
    @Override
    public List<ContentSet> getSuppliers() {
        List<ContentSet> result = new ArrayList<ContentSet>();
        
        for (ContentSet set : getContentSets()) {
            if (set.isSupplier()) {
                result.add(set);
            }
        }
        
        return result;
    }
    
    @Override
    public List<ConsumerContentSet> getConsumers() {
        List<ConsumerContentSet> result = new ArrayList<ConsumerContentSet>();
        
        for (ContentSet set : getContentSets()) {
            if (!set.isSupplier()) {
                result.add((ConsumerContentSet)set);
            }
        }
        
        return result;
    }

    @Override
    public Long save(ContentSet contentSet) throws ValidationException {
        Response response = put(converter.to(MediaType.TEXT_XML, contentSet), "content/", contentSet.getId());
        Utils.checkForException(response);
        return (Long) converter.convert(response.getEntity()); 
    }
}
