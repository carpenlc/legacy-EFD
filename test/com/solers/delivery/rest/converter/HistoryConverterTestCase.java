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

import java.util.List;

import junit.framework.TestCase;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.restlet.data.MediaType;

import com.solers.delivery.XPathAssert;
import com.solers.delivery.reports.history.MockSynchronizationHistory;
import com.solers.delivery.reports.history.ReportDetail;
import com.solers.delivery.reports.history.Synchronization;
import com.solers.delivery.reports.history.SynchronizationHistory;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class HistoryConverterTestCase extends TestCase {
    
    MockSynchronizationHistory history = new MockSynchronizationHistory();
    HistoryConverter converter = new HistoryConverter();
    
    public static void assertDetailsXML(String xml) throws Exception {
        XPathAssert.assertXPathNodeExists(xml, "//list");
        XPathAssert.assertXPathNodeExists(xml, "//list/detail");
        XPathAssert.assertXPathNodeValue(xml, "//list/detail/action/text()", "ADD");
        XPathAssert.assertXPathNodeValue(xml, "//list/detail/path/text()", "path");
        XPathAssert.assertXPathNodeValue(xml, "//list/detail/size/text()", "5000");
        XPathAssert.assertXPathNodeValue(xml, "//list/detail/successful/text()", "5000");
        XPathAssert.assertXPathNodeValue(xml, "//list/detail/failed/text()", "0");
        XPathAssert.assertXPathNodeValue(xml, "//list/detail/status/text()", "Success");
        XPathAssert.assertXPathNodeValue(xml, "//list/detail/timestamp/text()", ""+MockSynchronizationHistory.DATE.getTime());
    }
    
    public static void assertSynchronizationsXML(String xml) throws Exception {
        XPathAssert.assertXPathNodeExists(xml, "//list");
        XPathAssert.assertXPathNodeExists(xml, "//list/synchronization");
        XPathAssert.assertXPathNodeValue(xml, "//list/synchronization/id/text()", "id-1");
        XPathAssert.assertXPathNodeValue(xml, "//list/synchronization/elapsedTime/text()", "100");
        XPathAssert.assertXPathNodeValue(xml, "//list/synchronization/bytesAdded/text()", "1");
        XPathAssert.assertXPathNodeValue(xml, "//list/synchronization/bytesUpdated/text()", "2");
        XPathAssert.assertXPathNodeValue(xml, "//list/synchronization/bytesDeleted/text()", "3");
        XPathAssert.assertXPathNodeValue(xml, "//list/synchronization/adds/text()", "4");
        XPathAssert.assertXPathNodeValue(xml, "//list/synchronization/updates/text()", "5");
        XPathAssert.assertXPathNodeValue(xml, "//list/synchronization/deletes/text()", "6");
        XPathAssert.assertXPathNodeValue(xml, "//list/synchronization/host/text()", "127.0.0.1");
        XPathAssert.assertXPathNodeValue(xml, "//list/synchronization/timestamp/text()", ""+MockSynchronizationHistory.DATE.getTime());
    }
    
    public static void assertDetailsJSON(String json) throws Exception {
        JSONObject obj = new JSONObject(json);
        
        JSONArray list = obj.getJSONArray("list");
        assertNotNull(list);
        
        JSONObject detail = list.getJSONObject(0).getJSONObject("detail");
        assertNotNull(detail);
        
        assertEquals("ADD", detail.getString("action"));
        assertEquals("path", detail.getString("path"));
        assertEquals(5000, detail.getLong("size"));
        assertEquals("Success", detail.getString("status"));
        assertEquals(MockSynchronizationHistory.DATE.getTime(), detail.getLong("timestamp"));
    }
    
    public static void assertSynchronizationsJSON(String json) throws Exception {
        JSONObject obj = new JSONObject(json);
        
        JSONArray list = obj.getJSONArray("list");
        assertNotNull(list);
        
        JSONObject sync = list.getJSONObject(0).getJSONObject("synchronization");
        
        assertEquals("id-1", sync.getString("id"));
        assertEquals(100, sync.getLong("elapsedTime"));
        assertEquals(1, sync.getLong("bytesAdded"));
        assertEquals(2, sync.getLong("bytesUpdated"));
        assertEquals(3, sync.getLong("bytesDeleted"));
        assertEquals(4, sync.getLong("adds"));
        assertEquals(5, sync.getLong("updates"));
        assertEquals(6, sync.getLong("deletes"));
        assertEquals("127.0.0.1", sync.getString("host"));
        assertEquals(MockSynchronizationHistory.DATE.getTime(), sync.getLong("timestamp"));
    }
    
    public void testConvertXmlToDetailsList() throws Exception {
        List<ReportDetail> expected = history.getSynchronizationDetails(null, null, null, null, SynchronizationHistory.PAGE_SIZE);
        List<ReportDetail> actual = converter.fromList(converter.to(MediaType.TEXT_XML, expected));
        
        assertEquals(expected.size(), actual.size());
        
        ReportDetail e = expected.iterator().next();
        ReportDetail a = actual.iterator().next();
        
        assertEquals(e.getAction(), a.getAction());
        assertEquals(e.getPath(), a.getPath());
        assertEquals(e.getSize(), a.getSize());
        assertEquals(e.getStatus(), a.getStatus());
        assertEquals(e.getTimestamp(), a.getTimestamp());
    }
    
    public void testConvertJSONToDetailsList() throws Exception {
        List<ReportDetail> expected = history.getSynchronizationDetails(null, null, null, null, SynchronizationHistory.PAGE_SIZE);
        List<ReportDetail> actual = converter.fromList(converter.to(MediaType.APPLICATION_JSON, expected));
        
        assertEquals(expected.size(), actual.size());
        
        ReportDetail e = expected.iterator().next();
        ReportDetail a = actual.iterator().next();
        
        assertEquals(e.getAction(), a.getAction());
        assertEquals(e.getPath(), a.getPath());
        assertEquals(e.getSize(), a.getSize());
        assertEquals(e.getStatus(), a.getStatus());
        assertEquals(e.getTimestamp(), a.getTimestamp());
    }
    
    public void testConvertListOfDetailsToXml() throws Exception {
        List<ReportDetail> data = history.getSynchronizationDetails(null, null, null, null, SynchronizationHistory.PAGE_SIZE);
        
        String xml = converter.to(MediaType.TEXT_XML, data).getText();
        
        assertDetailsXML(xml);
    }
    
    public void testConvertSubListOfDetailsToXml() throws Exception {
        List<ReportDetail> data = history.getSynchronizationDetails(null, null, null, null, SynchronizationHistory.PAGE_SIZE);
        
        String xml = converter.to(MediaType.TEXT_XML, data.subList(0, data.size())).getText();
        
        assertDetailsXML(xml);
    }
    
    public void testConvertListOfDetailsToJSON() throws Exception {
        List<ReportDetail> data = history.getSynchronizationDetails(null, null, null, null, SynchronizationHistory.PAGE_SIZE);
        
        String json = converter.to(MediaType.APPLICATION_JSON, data).getText();
        
        assertDetailsJSON(json);
    }
    
    public void testConvertListOfSynchronizationsToXml() throws Exception {
        List<Synchronization> data = history.getSynchronizations(null, null, null, false, SynchronizationHistory.PAGE_SIZE);
        
        String xml = converter.to(MediaType.TEXT_XML, data).getText();
        
        assertSynchronizationsXML(xml);
    }
    
    public void testConvertListOfSynchronizationsToJSON() throws Exception {
        List<Synchronization> data = history.getSynchronizations(null, null, null, false, SynchronizationHistory.PAGE_SIZE);
        
        String json = converter.to(MediaType.APPLICATION_JSON, data).getText();
        
        assertSynchronizationsJSON(json);
    }
}
