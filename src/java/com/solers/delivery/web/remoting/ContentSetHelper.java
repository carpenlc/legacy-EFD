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
package com.solers.delivery.web.remoting;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.restlet.data.ClientInfo;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

import com.solers.delivery.content.ContentService;
import com.solers.delivery.domain.ConsumerContentSet;
import com.solers.delivery.domain.ContentSet;
import com.solers.delivery.rest.RestfulService;
import com.solers.delivery.rest.converter.ContentSetConverter;
import com.solers.delivery.transport.http.HTTPHeaders;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class ContentSetHelper {
    
    private static final Logger log = Logger.getLogger(ContentSetHelper.class);
    
    private final ContentService service;
    private final ContentSetConverter converter;

    public ContentSetHelper(ContentService service) {
        this.service = service;
        this.converter = new ContentSetConverter();
    }
    
    public Set<ContentSetNode> getConsumers() {
        return getNodes(service.getConsumers());
    }
    
    public Set<ContentSetNode> getSuppliers() {
        return getNodes(service.getSuppliers());
    }
    
    public ContentSet getContentSet(Long id) {
        return service.get(id);
    }
    
    public ContentSet newSupplier() {
        return new ContentSet();
    }
    
    public ConsumerContentSet newConsumer() {
        return new ConsumerContentSet();
    }
    
    public Long saveSupplier(ContentSet contentSet) {
        return service.save(contentSet);
    }
    
    public Long saveConsumer(ConsumerContentSet contentSet) {
        return service.save(contentSet);
    }
    
    public void enable(Long [] ids) {
        for (Long id : ids) {
            service.enable(id);
        }
    }
    
    public void disable(Long [] ids) {
        for (Long id : ids) {
            service.disable(id);
        }
    }
    
    public void remove(Long [] ids) {
        for (Long id : ids) {
            service.remove(id);
        }
    }
    
    @SuppressWarnings("unchecked")
    public List<ContentSetNode> listSuppliers(String host, int port) {
        RestfulService service = new RestfulService(Protocol.HTTPS, host, port);
        Reference uri = service.uri("api/suppliers/");
        Request request = new Request(Method.GET, uri);
        
        ClientInfo clientInfo = new ClientInfo();
        clientInfo.setAgent(HTTPHeaders.USER_AGENT.defaultValue());
        request.setClientInfo(clientInfo);
        
        Response response = service.handle(request);
        if (response.getStatus().isSuccess()) {
            return (List<ContentSetNode>) converter.convert(response.getEntity());
        } else {
            //EFD-597 Supplier Lookup error message when 2.2 consumer looks up 2.0 supplier
            //If the response status code is 400, assume that it is because this instance is an EFD 2.2+ web client and the supplier
            //is EFD 2.0 or earlier.
            if (Status.CLIENT_ERROR_BAD_REQUEST.getCode() == response.getStatus().getCode()) {
                log.error("The content set look up capability is not supported by the remote supplier. Error retrieving suppliers for <"+host+":"+port+">.  Response status: "+response.getStatus());
                throw new RuntimeException("The content set look up capability is not supported by the remote supplier.");
            } else {
                //Handle any other exception as usual
                log.warn("Error retrieving suppliers for <"+host+":"+port+">.  Response status: "+response.getStatus());
                throw new RuntimeException("Could not retrieve suppliers.  Reason: "+response.getStatus());
            }
        }
    }
    
    public void updateDisplayPath(String displayPath, Long id) {
        ContentSet contentSet = service.get(id);
        contentSet.setDisplayPath(displayPath);
        service.save(contentSet);
    }
    
    private Set<ContentSetNode> getNodes(List<? extends ContentSet> data) {
        Set<ContentSetNode> result = new HashSet<ContentSetNode>(data.size());
        for (ContentSet c : data) {
            ContentSetNode n = new ContentSetNode(c.getId(), c.getName());
            n.setSupplier(c.isSupplier());
            n.setDescription(c.getDescription());
            n.setDate(c.getUpdateTime());
            n.setEnabled(c.isEnabled());
            n.setDisplayPath(c.getDisplayPath());
            result.add(n);
        }
        return result;
    }
}
