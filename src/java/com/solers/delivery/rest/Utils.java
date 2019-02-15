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

import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;

import com.solers.delivery.rest.converter.ValidationExceptionConverter;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public final class Utils {
    
    /**
     * Response code sent when a validation error occurs
     */
    public static final Status CLIENT_ERROR_VALIDATION = new Status(450, "Validation Error", "The described input was not validated", "");
    
    private static final ValidationExceptionConverter errorConverter = new ValidationExceptionConverter();
    private static final String RESTLET_HEADERS = "org.restlet.http.headers";
    private static final String ACCEPT_HEADER = "Accept";
    
    private Utils() {
        
    }
    
    public static void checkForException(Response response) {
        if (response.getStatus().equals(CLIENT_ERROR_VALIDATION) || 
            response.getStatus().equals(Status.CLIENT_ERROR_UNAUTHORIZED) ||
            response.getStatus().equals(Status.CLIENT_ERROR_BAD_REQUEST)) {
            throw errorConverter.from(response.getEntity());
        }
    }
    
    public static void sendValidationError(Request request, Response response, Exception ex) {
        response.setStatus(CLIENT_ERROR_VALIDATION);
        setException(request, response, ex);
    }
    
    public static void setException(Request request, Response response, Exception ex) {
        response.setEntity(errorConverter.to(findType(request, MediaType.TEXT_XML), ex));
    }
    
    /**
     * Send an "empty" entity body back to the client.
     * 
     * As of 02/13/2008, there is a bug in the LWP Perl library that 3rd
     * party clients use to access the REST API.  This library expects all responses
     * to have an entity body.
     * 
     * Normally, we would not send an entity body back for PUT or DELETE operations
     * but this bug is forcing us to send something.
     * 
     * You can remove this when the following issue is patched and deployed:
     * 
     * http://rt.cpan.org/Public/Bug/Display.html?id=32595
     * 
     * @param response
     */
    public static void sendEmptyResponse(Response response) {
        response.setEntity(new StringRepresentation("EMPTY", MediaType.TEXT_PLAIN));
    }
    
    /**
     * Find an ID in the request URI.  If the id is malformed, send a 400 (BAD REQUEST) response.
     * 
     * If required is true and id is not present, send a 400 response
     * 
     * @param request
     * @param response
     * @param required
     * @return
     */
    public static long findId(Request request, Response response, boolean required) {
        if (request.getAttributes().containsKey("id")) {
            String id = (String) request.getAttributes().get("id");
            Long result = convertLong(id, response);
            if (result != null) {
                return result;
            }
        }

        if (required) {
            response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "No id given");
        }
        return -1;
    }
    
    public static Long convertLong(String value, Response response) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid number format");
        }
        return null;
    }
    
    /**
     * Find an ID in the request URI.  If the id is malformed or not present, send a 400 (BAD REQUEST) response.
     * 
     * @param request
     * @param response
     * @return
     */
    public static long findId(Request request, Response response) {
        return findId(request, response, true);
    }
    
    /**
     * Find a String in the request URI
     * @param request
     * @param response
     * @param key
     * @return
     */
    public static String findString(Request request, Response response, String key) {
        if (request.getAttributes().containsKey(key)) {
            return (String) request.getAttributes().get(key);
        }
        
        response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, key+" must be given in the URI");
        return null;
    }
    
    /**
     * This method inspects the API request and determines the appropriate output
     * type.  It will first check the request type itself.  If it is XML or JSON,
     * it will return whichever it is.  If it is not XML or JSON, it will examine
     * the Accept header to see if XML or JSON is supported.  Barring positive
     * identification of any user-requested media type, it will return the default.
     * @param request The request object
     * @param defaultType The type to use when positive identification fails.
     * @return The inspected type
     */
    @SuppressWarnings("unchecked")
    public static MediaType findType(Request request, MediaType defaultType) {
        if (request == null) return defaultType;
        
        //Return the same type as the request, if possible
        Representation r = request.getEntity();
        if (r != null) {
            MediaType requestType = r.getMediaType();
            if (requestType != null) {
                if (requestType.equals(MediaType.TEXT_XML)
                    || requestType.equals(MediaType.APPLICATION_JSON))
                    return requestType;
            }
        }
        
        //Inspect the Accept header for a supported output type
        List<Parameter> headerList = (List<Parameter>) request.getAttributes().get(RESTLET_HEADERS);
        String accept = "";
        if (headerList != null) {
            for (Parameter header : headerList) {
                if (header.getName().equals(ACCEPT_HEADER)) {
                    accept = header.getValue();
                    break;
                }
            }
        }
        
        MediaType acceptType = defaultType;
        if (accept.contains(MediaType.TEXT_XML.toString())) {
            acceptType = MediaType.TEXT_XML;
        } else if (accept.contains(MediaType.APPLICATION_JSON.toString())) {
            acceptType = MediaType.APPLICATION_JSON;
        }
        
        return acceptType;
    }
}
