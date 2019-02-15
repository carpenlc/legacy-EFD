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

import org.restlet.resource.Representation;
import org.springframework.security.AuthenticationException;

import com.solers.delivery.util.password.InvalidPasswordException;
import com.solers.util.dao.ValidationException;
import com.thoughtworks.xstream.XStream;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class ValidationExceptionConverter extends Converter {
    
    public RuntimeException from(Representation r) {
        return (RuntimeException) convert(r);
    }
    
    protected XStream initialize(XStream stream) {
        stream.setMode(XStream.NO_REFERENCES);
        
        stream.omitField(Throwable.class, "stackTrace");
        stream.alias("validation-errors", ValidationException.class);
        stream.alias("invalid-password", InvalidPasswordException.class);
        stream.alias("authentication-exception", AuthenticationException.class);
        stream.addImplicitCollection(ValidationException.class, "messages");
        
        return stream;
    }
}
