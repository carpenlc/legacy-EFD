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
import org.restlet.data.Reference;
import org.restlet.data.Response;
import org.restlet.data.Status;

import com.solers.delivery.ContentSetAssert;
import com.solers.delivery.reports.history.Synchronization;
import com.solers.delivery.rest.BaseRestTestCase;
import com.solers.delivery.rest.converter.HistoryConverterTestCase;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class SynchronizationHistoryDetailsTestCase extends BaseRestTestCase {
    
    public void setUp() {
        super.setUp();
        uri = "http://localhost:"+getPort()+"/reports/history/";
    }
    
    public void testGetWithoutAuth() throws Exception {
        authenticate = false;
        get(null, Status.CLIENT_ERROR_UNAUTHORIZED);
    }
    
    public void testGetDetailsWithInvalidContentSet() {
        uri = uri + "a/details/fff";
        get(Status.CLIENT_ERROR_BAD_REQUEST);
    }
    
    public void testGetDetailsNoContentSet() {
        uri = uri + "1/details/fff";
        get(Status.CLIENT_ERROR_NOT_FOUND);
    }
    
    public void testGetNoDetails() {
        uri = uri + "1/details/id-1";
        getDAOFactory().getConsumerContentSetDAO().makePersistent(ContentSetAssert.createConsumerContentSet());
        get(Status.CLIENT_ERROR_NOT_FOUND);
    }
    
    public void testGetDetailsXML() throws Exception {
        uri = uri + "1/details/id-1";
        getDAOFactory().getConsumerContentSetDAO().makePersistent(ContentSetAssert.createConsumerContentSet());
        getSyncHistory().sync = new Synchronization();
        
        Reference ref = new Reference(uri);
        ref.addQueryParameter("max", "1");
        
        Response r = get(MediaType.TEXT_XML, Status.SUCCESS_OK, ref);
        
        String xml = r.getEntity().getText();
        HistoryConverterTestCase.assertDetailsXML(xml);
    }
    
    public void testGetDetailsJSON() throws Exception {
        uri = uri + "1/details/id-1";
        getDAOFactory().getConsumerContentSetDAO().makePersistent(ContentSetAssert.createConsumerContentSet());
        getSyncHistory().sync = new Synchronization();
        
        Reference ref = new Reference(uri);
        ref.addQueryParameter("max", "1");
        
        Response r = get(MediaType.APPLICATION_JSON, Status.SUCCESS_OK, ref);
        
        String json = r.getEntity().getText();
        HistoryConverterTestCase.assertDetailsJSON(json);
    }
}
