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

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.restlet.Client;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.ClientInfo;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.solers.delivery.acegi.MockAuthenticationProvider;
import com.solers.delivery.content.consumer.MockConsumerContentSetManager;
import com.solers.delivery.content.supplier.MockSupplierContentSetManager;
import com.solers.delivery.daos.MockDAOFactory;
import com.solers.delivery.management.registry.MockRegistrar;
import com.solers.delivery.reports.history.MockSynchronizationHistory;

public abstract class BaseRestTestCase extends TestCase {
    
    protected ClassPathXmlApplicationContext ctx;
    protected String uri;
    protected boolean authenticate;
    
    public void setUp() {
        System.setProperty("rest.port", "8004");
        ctx = new ClassPathXmlApplicationContext(new String[] { "./restlet-test.xml", "./spring/rest.xml" });
        ctx.registerShutdownHook();
        uri = "http://localhost:"+getPort()+"/content/";
        authenticate = true;
    }
    
    public void tearDown() {
        ctx.destroy();
    }
    
    @SuppressWarnings("unchecked")
    protected ClientInfo getInfo(MediaType mediaType) {
        ClientInfo result = new ClientInfo();
        
        List<Preference<MediaType>> data = Arrays.asList(new Preference<MediaType>(mediaType));
        result.setAcceptedMediaTypes(data);
        
        return result;
    }
    
    protected Response get(MediaType mediaType) {
        return get(mediaType, Status.SUCCESS_OK);
    }
    
    protected Response get(Status status) {
        return get(null, status);
    }
    
    protected Response get(MediaType mediaType, Status status) {
        return get(mediaType, status, new Reference(uri));
    }
    
    protected Response get(MediaType mediaType, Status status, Reference reference) {
        Request request = new Request(Method.GET, reference);
        if (mediaType != null) {
            request.setClientInfo(getInfo(mediaType));
        }
        
        if (authenticate) {
            request.setChallengeResponse(new ChallengeResponse(ChallengeScheme.HTTP_BASIC, MockAuthenticationProvider.USER, MockAuthenticationProvider.PASS.toCharArray()));
        }
        
        Response r = new Client(Protocol.HTTP).handle(request);
        assertEquals(r.getStatus().getDescription(), status, r.getStatus());
        if (mediaType != null) {
            assertEquals(mediaType.getName(), r.getEntity().getMediaType().getName());
        }
        return r;
    }
    
    protected Response put(String data, MediaType mediaType) {
        return put(data, mediaType, Status.SUCCESS_CREATED);
    }
    
    protected Response put(Representation entity) {
        return put(entity, Status.SUCCESS_CREATED);
    }
    
    protected Response put(String data, MediaType mediaType, Status status) {
        return put(toEntity(data, mediaType), status);
    }
    
    protected Response put(Representation entity, Status status) {
        Request request = new Request(Method.PUT, uri, entity);
        if (authenticate) {
            request.setChallengeResponse(new ChallengeResponse(ChallengeScheme.HTTP_BASIC, MockAuthenticationProvider.USER, MockAuthenticationProvider.PASS.toCharArray()));
        }
        Response r = new Client(Protocol.HTTP).handle(request);
        assertEquals(status, r.getStatus());
        return r;
    }
    
    protected void delete() {
        delete(Status.SUCCESS_OK);
    }
    
    protected void delete(Status status) {
        Request request = new Request(Method.DELETE, uri);
        if (authenticate) {
            request.setChallengeResponse(new ChallengeResponse(ChallengeScheme.HTTP_BASIC, MockAuthenticationProvider.USER, MockAuthenticationProvider.PASS.toCharArray()));
        }
        Response r = new Client(Protocol.HTTP).handle(request);
        assertEquals(status, r.getStatus());
    }
    
    protected Representation toEntity(String data, MediaType mediaType) {
        return new StringRepresentation(data, mediaType);
    }
    
    protected MockDAOFactory getDAOFactory() {
        return (MockDAOFactory) ctx.getBean("daoFactory");
    }
    
    protected MockConsumerContentSetManager getConsumerManager() {
        return (MockConsumerContentSetManager) ctx.getBean("consumerContentSetManager");
    }
    
    protected MockSupplierContentSetManager getSupplierManager() {
        return (MockSupplierContentSetManager) ctx.getBean("supplierContentSetManager");
    }
    
    protected MockRegistrar getRegistrar() {
        return (MockRegistrar) ctx.getBean("mxbeanRegistrar");
    }
    
    protected MockSynchronizationHistory getSyncHistory() {
        return (MockSynchronizationHistory) ctx.getBean("synchronizationHistory");
    }
    
    protected int getPort() {
        return Integer.parseInt(System.getProperty("rest.port"));
    }
}
