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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Response;
import org.restlet.data.Status;

import com.solers.delivery.ContentSetAssert;
import com.solers.delivery.reports.history.ReportDetail;
import com.solers.delivery.reports.history.Synchronization;
import com.solers.delivery.reports.history.SynchronizationHistory;
import com.solers.delivery.rest.BaseRestTestCase;
import com.solers.delivery.rest.converter.HistoryConverter;
import com.solers.delivery.rest.converter.HistoryConverterTestCase;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class SupplierHistoryTestCase extends BaseRestTestCase {
    
    HistoryConverter converter = new HistoryConverter();
    
    public void setUp() {
        super.setUp();
        uri = "http://localhost:"+getPort()+"/reports/history/";
    }
    
    public void testGetBadId() {
        uri = uri + "a";
        get(Status.CLIENT_ERROR_BAD_REQUEST);
    }
    
    public void testGetWithoutAuth() throws Exception {
        authenticate = false;
        get(null, Status.CLIENT_ERROR_UNAUTHORIZED);
    }
    
    public void testGetBadMaxParam() {
        uri = uri + "1";
        getDAOFactory().getContentSetDAO().makePersistent(ContentSetAssert.createContentSet());
        
        Reference ref = new Reference(uri);
        ref.addQueryParameter("max", "ff");
        
        get(null, Status.CLIENT_ERROR_BAD_REQUEST, ref);
    }
    
    public void testGetNotFound() throws Exception {
        uri = uri + "2";
        getDAOFactory().getContentSetDAO().makePersistent(ContentSetAssert.createContentSet());
        
        get(null, Status.CLIENT_ERROR_NOT_FOUND);
    }
    
    public void testGetXml() throws Exception {
        uri = uri + "1";
        getDAOFactory().getContentSetDAO().makePersistent(ContentSetAssert.createContentSet());
        
        Reference ref = new Reference(uri);
        ref.addQueryParameter("max", "1");
        
        Response r = get(MediaType.TEXT_XML, Status.SUCCESS_OK, ref);
        
        String xml = r.getEntity().getText();
        HistoryConverterTestCase.assertSynchronizationsXML(xml);
    }
    
    public void testGetJSON() throws Exception {
        uri = uri + "1";
        getDAOFactory().getContentSetDAO().makePersistent(ContentSetAssert.createContentSet());
        
        Reference ref = new Reference(uri);
        ref.addQueryParameter("max", "1");
        
        Response r = get(MediaType.APPLICATION_JSON, Status.SUCCESS_OK, ref);
        
        String json = r.getEntity().getText();
        HistoryConverterTestCase.assertSynchronizationsJSON(json);
    }
    
    public void testGetLimitXml() throws Exception {
        uri = uri + "1";
        getDAOFactory().getContentSetDAO().makePersistent(ContentSetAssert.createContentSet());
        
        getSyncHistory().syncData = syncData(30);
        
        Reference ref = new Reference(uri);
        ref.addQueryParameter("max", "5");
        
        Response r = get(MediaType.TEXT_XML, Status.SUCCESS_OK, ref);
        List<ReportDetail> data = converter.fromList(r.getEntity());
        assertEquals(5, data.size());
        
        ref = new Reference(uri);
        ref.addQueryParameter("max", "2");
        
        r = get(MediaType.TEXT_XML, Status.SUCCESS_OK, ref);
        data = converter.fromList(r.getEntity());
        assertEquals(2, data.size());
        
        // No max value gets everything
        ref = new Reference(uri);
        
        r = get(MediaType.TEXT_XML, Status.SUCCESS_OK, ref);
        data = converter.fromList(r.getEntity());
        assertEquals(30, data.size());
    }
    
    public void testGetLimitJSON() throws Exception {
        uri = uri + "1";
        getDAOFactory().getContentSetDAO().makePersistent(ContentSetAssert.createContentSet());
        
        getSyncHistory().syncData = syncData(10);
        
        Reference ref = new Reference(uri);
        ref.addQueryParameter("max", "5");
        
        Response r = get(MediaType.APPLICATION_JSON, Status.SUCCESS_OK, ref);
        List<Synchronization> data = converter.fromList(r.getEntity());
        assertEquals(5, data.size());
        
        ref = new Reference(uri);
        ref.addQueryParameter("max", "2");
        
        r = get(MediaType.APPLICATION_JSON, Status.SUCCESS_OK, ref);
        data = converter.fromList(r.getEntity());
        assertEquals(2, data.size());
        
    }
    
    public void testGetWithDateRange() throws Exception {
        uri = uri + "1";
        getDAOFactory().getContentSetDAO().makePersistent(ContentSetAssert.createContentSet());
        
        Date start = new Date();
        Date end = new Date(System.currentTimeMillis()+5000);
        
        Reference ref = new Reference(uri);
        ref.addQueryParameter("max", "1");
        ref.addQueryParameter("startTime", start.getTime()+"");
        ref.addQueryParameter("endTime", end.getTime()+"");
        
        Response r = get(MediaType.TEXT_XML, Status.SUCCESS_OK, ref);
        
        String xml = r.getEntity().getText();
        HistoryConverterTestCase.assertSynchronizationsXML(xml);
        
        assertEquals(start, getSyncHistory().startTime);
        assertEquals(end, getSyncHistory().endTime);
        
        r = get(MediaType.APPLICATION_JSON, Status.SUCCESS_OK, ref);
        
        String json = r.getEntity().getText();
        HistoryConverterTestCase.assertSynchronizationsJSON(json);
        
        assertEquals(start, getSyncHistory().startTime);
        assertEquals(end, getSyncHistory().endTime);
    }
    
    public void testGetWithMalformedDateRange() throws Exception {
        uri = uri + "1";
        getDAOFactory().getContentSetDAO().makePersistent(ContentSetAssert.createContentSet());
        
        Date start = new Date();
        
        Reference ref = new Reference(uri);
        ref.addQueryParameter("max", "1");
        ref.addQueryParameter("startTime", start.getTime()+"");
        ref.addQueryParameter("endTime", "foobar");
        
        Response r = get(MediaType.TEXT_XML, Status.SUCCESS_OK, ref);
        
        String xml = r.getEntity().getText();
        HistoryConverterTestCase.assertSynchronizationsXML(xml);
        
        assertEquals(start, getSyncHistory().startTime);
        assertNull(getSyncHistory().endTime);
        
        r = get(MediaType.APPLICATION_JSON, Status.SUCCESS_OK, ref);
        
        String json = r.getEntity().getText();
        HistoryConverterTestCase.assertSynchronizationsJSON(json);
        
        assertEquals(start, getSyncHistory().startTime);
        assertNull(getSyncHistory().endTime);
    }
    
    protected List<ReportDetail> data(int count) {
        List<ReportDetail> data = new ArrayList<ReportDetail>();
   
        for (int i=0; i < count; i++) {
            for (ReportDetail r : getSyncHistory().getSynchronizationDetails(null, null, null, null, SynchronizationHistory.PAGE_SIZE)) {
                data.add(r);
            }
        }
        
        return data;
    }
    
    protected List<Synchronization> syncData(int count) {
        List<Synchronization> data = new ArrayList<Synchronization>();
   
        for (int i=0; i < count; i++) {
            for (Synchronization s : getSyncHistory().getSynchronizations(null, null, null, false, SynchronizationHistory.PAGE_SIZE)) {
                data.add(s);
            }
        }
        
        return data;
    }
}
