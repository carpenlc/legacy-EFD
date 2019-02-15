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
package com.solers.delivery.rest.converter;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

/**
 * Base Converter
 * 
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class Converter {
    
    private static final Logger log = Logger.getLogger(Converter.class);
    
    private final Map<String, XStream> streams;

    public Converter() {
        this.streams = new HashMap<String, XStream>();
        registerStream(MediaType.TEXT_XML, initialize(new XStream()));
        registerStream(MediaType.APPLICATION_JSON, initialize(new XStream(new JettisonMappedXmlDriver())));
    }
    
    public void registerStream(MediaType mediaType, XStream stream) {
        streams.put(mediaType.getName(), stream);
    }
    
    public Representation to(Variant variant, Object arg) {
        return to(variant.getMediaType(), arg);
    }
    
    public Representation to(MediaType mediaType, Object arg) {
        XStream stream = getStream(mediaType);
        return new StringRepresentation(toString(stream, arg), mediaType);
    }
    
    protected XStream getStream(MediaType mediaType) {
        XStream stream = streams.get(mediaType.getName());
        if (stream == null) {
            throw new StreamNotFoundException(mediaType+" does not have a registered stream");
        }
        return stream;
    }
    
    public Object convert(Representation r) {
        if (r == null) {
            return null;
        }     
        try {
            XStream stream = getStream(r.getMediaType());
            try (InputStream inputStream = getInputStream(r)) {
                return stream.fromXML(inputStream); 
            }
        } catch (StreamNotFoundException ex) {
            log.error("Couldn't find stream: "+ex.getMessage());
            try {
                throw new ConverterException("Conversion error.  HTTP Response Body: "+r.getText(), ex);
            } catch (IOException io) {
                throw new ConverterException("Could not read response body", io);
            }
        } catch (IOException ex) {
            log.error("Conversion error: "+ex.getMessage(), ex);
            throw new ConverterException(ex);
        } finally {
            r.release();
        }
    }
    
    /* public Object convert(Representation r) {
        if (r == null) {
            return null;
        }     
        try {
            XStream stream = getStream(r.getMediaType());
            return stream.fromXML(getInputStream(r));
        } catch (StreamNotFoundException ex) {
            log.error("Couldn't find stream: "+ex.getMessage());
            try {
                throw new ConverterException("Conversion error.  HTTP Response Body: "+r.getText(), ex);
            } catch (IOException io) {
                throw new ConverterException("Could not read response body", io);
            }
        } catch (IOException ex) {
            log.error("Conversion error: "+ex.getMessage(), ex);
            throw new ConverterException(ex);
        } finally {
            r.release();
        }
    }
    */
    
    /**
     * No-op, subclasses should override this
     * @param stream
     * @return
     */
    protected XStream initialize(XStream stream) {
        return stream;
    }
    
    /**
     * @param r
     * @return The input stream for {@code r
     * @throws IOException
     */
    protected InputStream getInputStream(Representation r) throws IOException {
        return r.getStream();
    }
    
    protected String toString(XStream stream, Object arg) {
        return stream.toXML(arg);
    }
    
    public static class StreamNotFoundException extends RuntimeException {
        
        private static final long serialVersionUID = 1L;

        private StreamNotFoundException(String message) {
            super(message);
        }
        
    }
}
