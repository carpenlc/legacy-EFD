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

import com.solers.delivery.StatusAssert;
import com.solers.delivery.management.ConsumerStatus;
import com.solers.delivery.management.SupplierStatus;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class StatusConverterTestCase extends TestCase {
    
    StatusConverter converter = new StatusConverter();
    
    public void testConvertSupplierStatusToJSON() throws Exception {
        SupplierStatus s = StatusAssert.proxy(SupplierStatus.class);
        String json = converter.to(MediaType.APPLICATION_JSON, s).getText();
        StatusAssert.assertSupplierJSON(json);
    }
    
    public void testConvertSupplierStatusToXml() throws Exception {
        SupplierStatus status = StatusAssert.proxy(SupplierStatus.class);
        String xml = converter.to(MediaType.TEXT_XML, status).getText();
        StatusAssert.assertSupplierXML(xml);
    }
    
    public void testConvertConsumerStatusToJSON() throws Exception {
        ConsumerStatus s = StatusAssert.proxy(ConsumerStatus.class);
        String json = converter.to(MediaType.APPLICATION_JSON, s).getText();
        StatusAssert.assertConsumerJSON(json);
    }
    
    public void testConvertConsumerStatustoXml() throws Exception {
        ConsumerStatus status = StatusAssert.proxy(ConsumerStatus.class);
        String xml = converter.to(MediaType.TEXT_XML, status).getText();
        StatusAssert.assertConsumerXML(xml);
    }
}
