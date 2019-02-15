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
package com.solers.delivery.rest.reports;

import org.restlet.data.MediaType;
import org.restlet.data.Response;
import org.restlet.data.Status;

import com.solers.delivery.ContentSetAssert;
import com.solers.delivery.StatusAssert;
import com.solers.delivery.management.ConsumerStatus;
import com.solers.delivery.management.SupplierStatus;
import com.solers.delivery.rest.BaseRestTestCase;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class StatusResourceTestCase extends BaseRestTestCase {
    
    public void setUp() {
        super.setUp();
        uri = "http://localhost:"+getPort()+"/reports/status/content/";
    }
    
    public void testGetWithoutAuth() throws Exception {
        authenticate = false;
        get(null, Status.CLIENT_ERROR_UNAUTHORIZED);
    }
    
    public void testGetNotFound() {
        get(Status.CLIENT_ERROR_NOT_FOUND);
        uri = uri + "13";
        get(Status.CLIENT_ERROR_NOT_FOUND);
    }
    
    public void testGetBadId() {
        uri = uri + "a";
        get(Status.CLIENT_ERROR_BAD_REQUEST);
    }
    
    public void testGetSupplierXml() throws Exception {
        uri = uri + "1";
        
        getDAOFactory().getContentSetDAO().makePersistent(ContentSetAssert.createContentSet());
        getRegistrar().registerManagedBean(StatusAssert.proxy(SupplierStatus.class), "suppliers", new Long(1));
        
        Response r = get(MediaType.TEXT_XML);
        StatusAssert.assertSupplierXML(r.getEntity().getText());
    }
    
    public void testGetSupplierJSON() throws Exception {
        uri = uri + "1";
        
        getDAOFactory().getContentSetDAO().makePersistent(ContentSetAssert.createContentSet());
        getRegistrar().registerManagedBean(StatusAssert.proxy(SupplierStatus.class), "suppliers", new Long(1));
        
        Response r = get(MediaType.APPLICATION_JSON);
        StatusAssert.assertSupplierJSON(r.getEntity().getText());
    }
    
    public void testGetConsumerXml() throws Exception {
        uri = uri + "1";
        
        getDAOFactory().getConsumerContentSetDAO().makePersistent(ContentSetAssert.createConsumerContentSet());
        getRegistrar().registerManagedBean(StatusAssert.proxy(ConsumerStatus.class), "suppliers", new Long(1));
        
        Response r = get(MediaType.TEXT_XML);
        StatusAssert.assertConsumerXML(r.getEntity().getText());
    }
    
    public void testGetConsumerJSON() throws Exception {
        uri = uri + "1";
        
        getDAOFactory().getConsumerContentSetDAO().makePersistent(ContentSetAssert.createConsumerContentSet());
        getRegistrar().registerManagedBean(StatusAssert.proxy(ConsumerStatus.class), "suppliers", new Long(1));
        
        Response r = get(MediaType.APPLICATION_JSON);
        StatusAssert.assertConsumerJSON(r.getEntity().getText());
    }
}
