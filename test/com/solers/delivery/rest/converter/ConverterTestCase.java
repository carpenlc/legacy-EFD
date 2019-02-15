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

import junit.framework.TestCase;

import org.restlet.data.MediaType;

import com.solers.delivery.rest.converter.Converter.StreamNotFoundException;
import com.thoughtworks.xstream.XStream;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class ConverterTestCase extends TestCase {
    
    MockConverter converter = new MockConverter();
    
    public void testGetDefaultStreams() {
        assertNotNull(converter.getStream(MediaType.TEXT_XML));
        assertNotNull(converter.getStream(MediaType.APPLICATION_JSON));
    }
    
    public void testGetUnregisteredStream() {
        try {
            converter.getStream(MediaType.TEXT_PLAIN);
            fail();
        } catch (StreamNotFoundException expected) {
            
        }
    }
    
    public void testRegisterStream() {
        try {
            converter.getStream(MediaType.TEXT_PLAIN);
            fail();
        } catch (StreamNotFoundException expected) {
            
        }
        
        XStream stream = new XStream();
        converter.registerStream(MediaType.TEXT_PLAIN, stream);
        assertSame(stream, converter.getStream(MediaType.TEXT_PLAIN));
    }
    
    public void testConvertNull() throws Exception {
        assertNull(converter.convert(null));
    }
    
    class MockConverter extends Converter {
        protected XStream initialize(XStream stream) {
            return stream;
        }
    }
}
