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

import java.util.ArrayList;
import java.util.List;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

import com.solers.delivery.content.ContentService;
import com.solers.delivery.domain.ContentSet;
import com.solers.delivery.management.StatusService;
import com.solers.delivery.management.SupplierStatus;
import com.solers.delivery.rest.converter.ContentSetConverter;
import com.solers.delivery.web.remoting.ContentSetNode;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class SupplierListingResource extends Resource {

    private final ContentService service;
    private final StatusService statusService;
    private final ContentSetConverter converter;
    
    public SupplierListingResource(ContentService service, StatusService statusService) {
        this.service = service;
        this.statusService = statusService;
        this.converter = new ContentSetConverter();
    }
    
    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        getVariants().add(new Variant(MediaType.TEXT_XML));
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        List<ContentSet> suppliers = service.getSuppliers();
        List<ContentSetNode> result = new ArrayList<ContentSetNode>(suppliers.size());
        
        for (ContentSet c : suppliers) {
            SupplierStatus status = statusService.getSupplierStatus(c.getId());
            ContentSetNode node = new ContentSetNode(c.getId(), c.getName());
            node.setDescription(c.getDescription());
            node.setItems(status.getItemCount());
            node.setSize(status.getSize());
            result.add(node);
        }
        
        return converter.to(variant, result);
        
    }
}
