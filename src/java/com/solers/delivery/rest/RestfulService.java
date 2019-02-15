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

import java.util.Properties;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;

import com.solers.delivery.rest.auth.RestAuthentication;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class RestfulService {
    
    private final Reference base;
    private final RestAuthentication auth;
    private final Properties connectionParameters;
    
    public RestfulService(String host, int port, RestAuthentication auth) {
        this(Protocol.HTTP, host, port, auth);
    }
    
    public RestfulService(String protocolName, String host, int port) {
        this(Protocol.valueOf(protocolName), host, port);
    }
    
    public RestfulService(Protocol protocol, String host, int port) {
        this(protocol, host, port, null);
    }
    
    public RestfulService(Protocol protocol, String host, int port, RestAuthentication auth) {
        this.base = new Reference(protocol, host, port);
        this.auth = auth;
        this.connectionParameters = new Properties();
    }
    
    public void setConnectionParameters(Properties properties) {
        connectionParameters.putAll(properties);
    }

    public Response put(String data, MediaType mediaType, Object...uriParts) {
        return put(toEntity(data, mediaType), uriParts);
    }

    public Response put(Representation entity, Object...uriParts) {
        Reference uri = uri(uriParts);
        Request request = new Request(Method.PUT, uri, entity);
        return handle(request);
    }

    public Response delete(Object...uriParts) {
        Reference uri = uri(uriParts);
        Request request = new Request(Method.DELETE, uri);
        return handle(request);
    }

    public Response get(Object...uriParts) {
        Reference uri = uri(uriParts);
        Request request = new Request(Method.GET, uri);
        return handle(request);
    }

    public Representation toEntity(String data, MediaType mediaType) {
        return new StringRepresentation(data, mediaType);
    }

    public Reference uri(Object...args) {
        Reference reference = new Reference(base);
        for (Object arg : args) {
            if (arg != null) {
                reference.addSegment(String.valueOf(arg));
            }
        }
        
        return reference;
    }
    
    public Response handle(Request request) {
        if (auth != null) {
            request.setChallengeResponse(auth.getDetails());
        }
        
        Client client = new Client(new Context(), Protocol.HTTP);
        for (String name : connectionParameters.stringPropertyNames()) {
            client.getContext().getParameters().add(name, connectionParameters.getProperty(name));
        }
        return client.handle(request);
    }
    
}
