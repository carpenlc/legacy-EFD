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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.resource.StringRepresentation;

import com.solers.delivery.ContentSetAssert;
import com.solers.delivery.XPathAssert;
import com.solers.delivery.domain.ConsumerContentSet;
import com.solers.delivery.domain.ContentSet;
import com.solers.delivery.util.FileSystemUtil;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class ContentSetConverterTestCase extends TestCase {
    
    ContentSetConverter converter;
    
    public void setUp() {
        converter = new ContentSetConverter();
    }
    
    public void testConvertXMLToEmptyContentSetList() throws Exception {
        StringRepresentation r = new StringRepresentation("<sets/>", MediaType.TEXT_XML);
        
        List<ContentSet> list = converter.fromList(r);
        
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }
    
    public void testConvertXMLToContentSetList() throws Exception {
        StringRepresentation r = new StringRepresentation("<sets>"+ContentSetAssert.CONTENT_SET_XML_EXISTING+"</sets>", MediaType.TEXT_XML);
        
        List<ContentSet> list = converter.fromList(r);
        
        assertNotNull(list);
        assertEquals(1, list.size());
        ContentSetAssert.assertContentSet(list.get(0));
    }
    
    public void testConvertXMLToContentSetAndConsumerContentSetList() throws Exception {
        StringRepresentation r = new StringRepresentation("<sets>"+ContentSetAssert.CONTENT_SET_XML_EXISTING+ContentSetAssert.CONSUMER_CONTENT_SET_XML_EXISTING+"</sets>", MediaType.TEXT_XML);
        
        List<ContentSet> list = converter.fromList(r);
        
        assertNotNull(list);
        assertEquals(2, list.size());
        ContentSetAssert.assertContentSet(list.get(0));
        ContentSetAssert.assertConsumerContentSet((ConsumerContentSet)list.get(1));
    }
    
    public void testConvertXMLTContentSet() throws Exception {
        StringRepresentation r = new StringRepresentation(ContentSetAssert.CONTENT_SET_XML_EXISTING, MediaType.TEXT_XML);
      
        ContentSet contentSet = converter.from(r);
        ContentSetAssert.assertContentSet(contentSet);
    }
    
    public void testConvertXMLTConsumerContentSet() throws Exception {
        StringRepresentation r = new StringRepresentation(ContentSetAssert.CONSUMER_CONTENT_SET_XML_EXISTING, MediaType.TEXT_XML);
        
        ConsumerContentSet contentSet = converter.from(r);
        ContentSetAssert.assertConsumerContentSet(contentSet);
    }
    
    public void testConvertJSONToContentSet() throws Exception {
        StringRepresentation r = new StringRepresentation(ContentSetAssert.CONTENT_SET_JSON_WITH_ID, MediaType.APPLICATION_JSON);
        
        ContentSet contentSet = converter.from(r);
        ContentSetAssert.assertContentSet(contentSet);
    }
    
    public void testConvertJSONToConsumerContentSet() throws Exception {
        StringRepresentation r = new StringRepresentation(ContentSetAssert.CONSUMER_CONTENT_SET_JSON_WITH_ID, MediaType.APPLICATION_JSON);
        
        ConsumerContentSet contentSet = converter.from(r);
        ContentSetAssert.assertConsumerContentSet(contentSet);
    }
    
    public void testConvertJSONToEmptyContentSetList() throws Exception {
        StringRepresentation r = new StringRepresentation("{\"sets\":\"\"}", MediaType.APPLICATION_JSON);
        
        List<ContentSet> list = converter.fromList(r);
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }
    
    public void testConvertJSONToEmptyConsumerContentSetList() throws Exception {
        StringRepresentation r = new StringRepresentation("{\"sets\":\"\"}", MediaType.APPLICATION_JSON);
        
        List<ConsumerContentSet> list = converter.fromList(r);
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }
    
    public void testConvertJSONToContentSetList() throws Exception {
        StringRepresentation r = new StringRepresentation("{\"sets\":"+ContentSetAssert.CONTENT_SET_JSON_WITH_ID+"}", MediaType.APPLICATION_JSON);
        
        List<ContentSet> list = converter.fromList(r);
        assertNotNull(list);
        assertEquals(1, list.size());
        ContentSetAssert.assertContentSet(list.get(0));
    }
    
    public void testConvertJSONToContentSetAndConsumerContentSetList() throws Exception {
        // Removes the trailing } from the content set json
        // Removes the starting " from the consumer content set json
        StringRepresentation r = new StringRepresentation("{\"sets\":"+ContentSetAssert.CONTENT_SET_JSON_WITH_ID.substring(0, ContentSetAssert.CONTENT_SET_JSON_WITH_ID.length()-1)+","+ContentSetAssert.CONSUMER_CONTENT_SET_JSON_WITH_ID.substring(1)+"}", MediaType.APPLICATION_JSON);
        
        List<ContentSet> list = converter.fromList(r);
        assertNotNull(list);
        assertEquals(2, list.size());
        ContentSetAssert.assertContentSet(list.get(0));
        ContentSetAssert.assertConsumerContentSet((ConsumerContentSet)list.get(1));
    }
    
    public void testConvertContentSetToJSON() throws Exception {
        ContentSet c = ContentSetAssert.createContentSet();
        
        String json = converter.to(MediaType.APPLICATION_JSON, c).getText();
        
        JSONObject obj = new JSONObject(json);
        JSONObject contentSet = obj.getJSONObject("supplierContentSet");
        
        assertNotNull(contentSet);
        assertEquals("1", contentSet.getString("id"));
        assertEquals("name", contentSet.getString("name"));
        assertEquals(FileSystemUtil.correctPath("path"), contentSet.getString("path"));
        assertEquals("true", contentSet.getString("enabled"));
        assertEquals("true", contentSet.getString("supplier"));
        assertEquals("100", contentSet.getString("inventoryInterval"));
        assertEquals("SECONDS", contentSet.getString("inventoryIntervalUnit"));
        assertEquals("200", contentSet.getString("creationTime"));
        assertEquals("description", contentSet.getString("description"));
        assertEquals("true", contentSet.getString("supportsGbsTransport"));
        assertEquals("[\"\"]", contentSet.getString("filters"));
        
        JSONObject conn = contentSet.getJSONObject("ftpConnection");
        
        assertNotNull(conn);
        assertEquals("directory", conn.getString("directory"));
        assertEquals("localhost", conn.getString("host"));
        assertEquals("true", conn.getString("passive"));
        assertEquals("password", conn.getString("password"));
        assertEquals("21", conn.getString("port"));
        assertEquals("username", conn.getString("username"));
        
        JSONArray hosts = contentSet.getJSONArray("allowedHosts");
        assertEquals(1, hosts.length());
        JSONObject host = hosts.getJSONObject(0).getJSONObject("allowedHost");
        assertFalse(host == null);
        assertEquals("testAlias", host.getString("alias"));
        assertEquals("testCommonName", host.getString("commonName"));
        
        assertScheduleExpression(contentSet.getJSONArray("scheduleExpressions")); 
    }
    
    public void testConvertContentSetListToJSON() throws Exception {
        ContentSet c = ContentSetAssert.createContentSet();
        
        List<ContentSet> sets = new ArrayList<ContentSet>();
        sets.add(c);
        
        String json = converter.to(MediaType.APPLICATION_JSON, sets).getText();
        
        JSONObject obj = new JSONObject(json);
        JSONArray set = obj.getJSONArray("sets");
        assertNotNull(set);
        
        JSONObject contentSet = set.getJSONObject(0).getJSONObject("supplierContentSet");
        
        assertNotNull(contentSet);
        assertEquals("1", contentSet.getString("id"));
        assertEquals("name", contentSet.getString("name"));
        assertEquals(FileSystemUtil.correctPath("path"), contentSet.getString("path"));
        assertEquals("true", contentSet.getString("enabled"));
        assertEquals("true", contentSet.getString("supplier"));
        assertEquals("100", contentSet.getString("inventoryInterval"));
        assertEquals("SECONDS", contentSet.getString("inventoryIntervalUnit"));
        assertEquals("200", contentSet.getString("creationTime"));
        assertEquals("description", contentSet.getString("description"));
        assertEquals("true", contentSet.getString("supportsGbsTransport"));
        assertEquals("[\"\"]", contentSet.getString("filters"));
        
        JSONObject conn = contentSet.getJSONObject("ftpConnection");
        
        assertNotNull(conn);
        assertEquals("directory", conn.getString("directory"));
        assertEquals("localhost", conn.getString("host"));
        assertEquals("true", conn.getString("passive"));
        assertEquals("password", conn.getString("password"));
        assertEquals("21", conn.getString("port"));
        assertEquals("username", conn.getString("username"));
        
        JSONArray hosts = contentSet.getJSONArray("allowedHosts");
        assertEquals(1, hosts.length());
        JSONObject host = hosts.getJSONObject(0).getJSONObject("allowedHost");
        assertFalse(host == null);
        assertEquals("testAlias", host.getString("alias"));
        assertEquals("testCommonName", host.getString("commonName"));
        
        assertScheduleExpression(contentSet.getJSONArray("scheduleExpressions")); 
    }
    
    public void testConvertConsumerContentSetToJSON() throws Exception {
        ConsumerContentSet c = ContentSetAssert.createConsumerContentSet();
        
        String json = converter.to(MediaType.APPLICATION_JSON, c).getText();
        
        JSONObject obj = new JSONObject(json);
        JSONObject contentSet = obj.getJSONObject("consumerContentSet");
        
        assertNotNull(contentSet);
        assertEquals("1", contentSet.getString("id"));
        assertEquals("name", contentSet.getString("name"));
        assertEquals(FileSystemUtil.correctPath("path"), contentSet.getString("path"));
        assertEquals("true", contentSet.getString("enabled"));
        assertEquals("false", contentSet.getString("supplier"));
        assertEquals("100", contentSet.getString("inventoryInterval"));
        assertEquals("SECONDS", contentSet.getString("inventoryIntervalUnit"));
        assertEquals("200", contentSet.getString("creationTime"));
        assertEquals("description", contentSet.getString("description"));
        assertEquals("true", contentSet.getString("supportsGbsTransport"));
        assertEquals("100", contentSet.getString("fileDeleteDelay"));
        assertEquals("DAYS", contentSet.getString("fileDeleteDelayUnit"));
        assertEquals("100", contentSet.getString("maxFileSize"));
        assertEquals("MEGABYTES", contentSet.getString("maxFileSizeUnit"));
        assertEquals("localhost", contentSet.getString("supplierAddress"));
        assertEquals("Supplier", contentSet.getString("supplierName"));
        assertEquals("443", contentSet.getString("supplierPort"));
        
        JSONArray filters = contentSet.getJSONArray("filters");
        assertEquals(1, filters.length());
        JSONObject filter = filters.getJSONObject(0).getJSONObject("filter");
        assertFalse(filter == null);
        assertEquals("abc", filter.getString("pattern"));
        assertEquals("BEGINS", filter.getString("patternType"));
        
        assertEquals("[\"\"]", contentSet.getString("allowedHosts"));
        
        assertScheduleExpression(contentSet.getJSONArray("scheduleExpressions")); 
    }
    
    public void testConvertConsumerContentSetListToJSON() throws Exception {
        ConsumerContentSet c = ContentSetAssert.createConsumerContentSet();
        
        List<ConsumerContentSet> sets = new ArrayList<ConsumerContentSet>();
        sets.add(c);
        
        String json = converter.to(MediaType.APPLICATION_JSON, sets).getText();
        
        JSONObject obj = new JSONObject(json);
        JSONArray set = obj.getJSONArray("sets");
        assertNotNull(set);
        
        JSONObject contentSet = set.getJSONObject(0).getJSONObject("consumerContentSet");
        
        assertNotNull(contentSet);
        assertEquals("1", contentSet.getString("id"));
        assertEquals("name", contentSet.getString("name"));
        assertEquals(FileSystemUtil.correctPath("path"), contentSet.getString("path"));
        assertEquals("true", contentSet.getString("enabled"));
        assertEquals("false", contentSet.getString("supplier"));
        assertEquals("100", contentSet.getString("inventoryInterval"));
        assertEquals("SECONDS", contentSet.getString("inventoryIntervalUnit"));
        assertEquals("200", contentSet.getString("creationTime"));
        assertEquals("description", contentSet.getString("description"));
        assertEquals("true", contentSet.getString("supportsGbsTransport"));
        assertEquals("100", contentSet.getString("fileDeleteDelay"));
        assertEquals("DAYS", contentSet.getString("fileDeleteDelayUnit"));
        assertEquals("100", contentSet.getString("maxFileSize"));
        assertEquals("MEGABYTES", contentSet.getString("maxFileSizeUnit"));
        assertEquals("localhost", contentSet.getString("supplierAddress"));
        assertEquals("Supplier", contentSet.getString("supplierName"));
        assertEquals("443", contentSet.getString("supplierPort"));
        
        JSONArray filters = contentSet.getJSONArray("filters");
        assertEquals(1, filters.length());
        JSONObject filter = filters.getJSONObject(0).getJSONObject("filter");
        assertFalse(filter == null);
        assertEquals("abc", filter.getString("pattern"));
        assertEquals("BEGINS", filter.getString("patternType"));
        
        assertEquals("[\"\"]", contentSet.getString("allowedHosts"));
        
        assertScheduleExpression(contentSet.getJSONArray("scheduleExpressions"));        
    }
    
    public void testConvertEmptyConsumerContentSetListToJSON() throws Exception {
        List<ConsumerContentSet> sets = new ArrayList<ConsumerContentSet>();
        
        String json = converter.to(MediaType.APPLICATION_JSON, sets).getText();
        
        JSONObject obj = new JSONObject(json);
        assertEquals("[\"\"]", obj.getString("sets"));
    }
    
    public void testConvertEmptyContentSetListToJSON() throws Exception {
        List<ContentSet> sets = new ArrayList<ContentSet>();
        
        String json = converter.to(MediaType.APPLICATION_JSON, sets).getText();
        
        JSONObject obj = new JSONObject(json);
        assertEquals("[\"\"]", obj.getString("sets"));
    }
    
    public void testConvertContentSetToXml() throws Exception {
        ContentSet c = ContentSetAssert.createContentSet();
        
        String xml = converter.to(MediaType.TEXT_XML, c).getText();
        
        XPathAssert.assertXPathNodeExists(xml, "//supplierContentSet");
        XPathAssert.assertXPathNodeValue(xml, "//supplierContentSet/id/text()", "1");
        XPathAssert.assertXPathNodeValue(xml, "//supplierContentSet/name/text()", "name");
        XPathAssert.assertXPathNodeValue(xml, "//supplierContentSet/path/text()", FileSystemUtil.correctPath("path"));
        XPathAssert.assertXPathNodeValue(xml, "//supplierContentSet/enabled/text()", "true");
        XPathAssert.assertXPathNodeValue(xml, "//supplierContentSet/supplier/text()", "true");
        XPathAssert.assertXPathNodeValue(xml, "//supplierContentSet/inventoryInterval/text()", "100");
        XPathAssert.assertXPathNodeValue(xml, "//supplierContentSet/inventoryIntervalUnit/text()", "SECONDS");
        XPathAssert.assertXPathNodeValue(xml, "//supplierContentSet/creationTime/text()", "200");
        XPathAssert.assertXPathNodeValue(xml, "//supplierContentSet/description/text()", "description");
        XPathAssert.assertXPathNodeValue(xml, "//supplierContentSet/supportsGbsTransport/text()", "true");
        XPathAssert.assertXPathNodeSetEmpty(xml, "//supplierContentSet/filters");
        XPathAssert.assertXPathNodeValue(xml, "//supplierContentSet/ftpConnection/directory/text()", "directory");
        XPathAssert.assertXPathNodeValue(xml, "//supplierContentSet/ftpConnection/host/text()", "localhost");
        XPathAssert.assertXPathNodeValue(xml, "//supplierContentSet/ftpConnection/passive/text()", "true");
        XPathAssert.assertXPathNodeValue(xml, "//supplierContentSet/ftpConnection/password/text()", "password");
        XPathAssert.assertXPathNodeValue(xml, "//supplierContentSet/ftpConnection/port/text()", "21");
        XPathAssert.assertXPathNodeValue(xml, "//supplierContentSet/ftpConnection/username/text()", "username");
        XPathAssert.assertXPathNodeExists(xml, "//supplierContentSet/allowedHosts/allowedHost");
        XPathAssert.assertXPathNodeValue(xml, "//supplierContentSet/allowedHosts/allowedHost/alias/text()", "testAlias");
        XPathAssert.assertXPathNodeValue(xml, "//supplierContentSet/allowedHosts/allowedHost/commonName/text()", "testCommonName");
        assertScheduleExpression(xml,"//supplierContentSet");
    }
    
    public void testConvertContentSetListToXml() throws Exception {
        ContentSet c = ContentSetAssert.createContentSet();
        
        List<ContentSet> sets = new ArrayList<ContentSet>();
        sets.add(c);
        
        String xml = converter.to(MediaType.TEXT_XML, sets).getText();
        
        XPathAssert.assertXPathNodeExists(xml, "//sets");
        XPathAssert.assertXPathNodeExists(xml, "//sets/supplierContentSet");
        XPathAssert.assertXPathNodeValue(xml, "//sets/supplierContentSet/id/text()", "1");
        XPathAssert.assertXPathNodeValue(xml, "//sets/supplierContentSet/name/text()", "name");
        XPathAssert.assertXPathNodeValue(xml, "//sets/supplierContentSet/path/text()", FileSystemUtil.correctPath("path"));
        XPathAssert.assertXPathNodeValue(xml, "//sets/supplierContentSet/enabled/text()", "true");
        XPathAssert.assertXPathNodeValue(xml, "//sets/supplierContentSet/supplier/text()", "true");
        XPathAssert.assertXPathNodeValue(xml, "//sets/supplierContentSet/inventoryInterval/text()", "100");
        XPathAssert.assertXPathNodeValue(xml, "//sets/supplierContentSet/inventoryIntervalUnit/text()", "SECONDS");
        XPathAssert.assertXPathNodeValue(xml, "//sets/supplierContentSet/creationTime/text()", "200");
        XPathAssert.assertXPathNodeValue(xml, "//sets/supplierContentSet/description/text()", "description");
        XPathAssert.assertXPathNodeValue(xml, "//sets/supplierContentSet/supportsGbsTransport/text()", "true");
        XPathAssert.assertXPathNodeSetEmpty(xml, "//sets/supplierContentSet/filters");
        XPathAssert.assertXPathNodeValue(xml, "//sets/supplierContentSet/ftpConnection/directory/text()", "directory");
        XPathAssert.assertXPathNodeValue(xml, "//sets/supplierContentSet/ftpConnection/host/text()", "localhost");
        XPathAssert.assertXPathNodeValue(xml, "//sets/supplierContentSet/ftpConnection/passive/text()", "true");
        XPathAssert.assertXPathNodeValue(xml, "//sets/supplierContentSet/ftpConnection/password/text()", "password");
        XPathAssert.assertXPathNodeValue(xml, "//sets/supplierContentSet/ftpConnection/port/text()", "21");
        XPathAssert.assertXPathNodeValue(xml, "//sets/supplierContentSet/ftpConnection/username/text()", "username");
        XPathAssert.assertXPathNodeExists(xml, "//sets/supplierContentSet/allowedHosts/allowedHost");
        XPathAssert.assertXPathNodeValue(xml, "//sets/supplierContentSet/allowedHosts/allowedHost/alias/text()", "testAlias");
        XPathAssert.assertXPathNodeValue(xml, "//sets/supplierContentSet/allowedHosts/allowedHost/commonName/text()", "testCommonName");
        assertScheduleExpression(xml,"//sets/supplierContentSet");
    }
    
    public void testConvertEmptyContentSetListToXml() throws Exception {
        
        List<ContentSet> sets = new ArrayList<ContentSet>();
        
        String xml = converter.to(MediaType.TEXT_XML, sets).getText();
        
        XPathAssert.assertXPathNodeExists(xml, "//sets");
        XPathAssert.assertXPathNodeSetEmpty(xml, "//sets");
    }
    
    public void testConvertEmptyConsumerContentSetListToXml() throws Exception {
        
        List<ConsumerContentSet> sets = new ArrayList<ConsumerContentSet>();
        
        String xml = converter.to(MediaType.TEXT_XML, sets).getText();
        
        XPathAssert.assertXPathNodeExists(xml, "//sets");
        XPathAssert.assertXPathNodeSetEmpty(xml, "//sets");
    }
    
    public void testConvertConsumerContentSetToXml() throws Exception {
        ConsumerContentSet c = ContentSetAssert.createConsumerContentSet();
        
        String xml = converter.to(MediaType.TEXT_XML, c).getText();
        
        XPathAssert.assertXPathNodeExists(xml, "//consumerContentSet");
        XPathAssert.assertXPathNodeValue(xml, "//consumerContentSet/id/text()", "1");
        XPathAssert.assertXPathNodeValue(xml, "//consumerContentSet/name/text()", "name");
        XPathAssert.assertXPathNodeValue(xml, "//consumerContentSet/path/text()", FileSystemUtil.correctPath("path"));
        XPathAssert.assertXPathNodeValue(xml, "//consumerContentSet/enabled/text()", "true");
        XPathAssert.assertXPathNodeValue(xml, "//consumerContentSet/supplier/text()", "false");
        XPathAssert.assertXPathNodeValue(xml, "//consumerContentSet/inventoryInterval/text()", "100");
        XPathAssert.assertXPathNodeValue(xml, "//consumerContentSet/inventoryIntervalUnit/text()", "SECONDS");
        XPathAssert.assertXPathNodeValue(xml, "//consumerContentSet/creationTime/text()", "200");
        XPathAssert.assertXPathNodeValue(xml, "//consumerContentSet/description/text()", "description");
        XPathAssert.assertXPathNodeValue(xml, "//consumerContentSet/supportsGbsTransport/text()", "true");
        XPathAssert.assertXPathNodeValue(xml, "//consumerContentSet/fileDeleteDelay/text()", "100");
        XPathAssert.assertXPathNodeValue(xml, "//consumerContentSet/fileDeleteDelayUnit/text()", "DAYS");
        XPathAssert.assertXPathNodeValue(xml, "//consumerContentSet/maxFileSize/text()", "100");
        XPathAssert.assertXPathNodeValue(xml, "//consumerContentSet/maxFileSizeUnit/text()", "MEGABYTES");
        XPathAssert.assertXPathNodeValue(xml, "//consumerContentSet/supplierAddress/text()", "localhost");
        XPathAssert.assertXPathNodeValue(xml, "//consumerContentSet/supplierName/text()", "Supplier");
        XPathAssert.assertXPathNodeValue(xml, "//consumerContentSet/supplierPort/text()", "443");
        XPathAssert.assertXPathNodeExists(xml, "//consumerContentSet/filters/filter");
        XPathAssert.assertXPathNodeValue(xml, "//consumerContentSet/filters/filter/pattern/text()", "abc");
        XPathAssert.assertXPathNodeValue(xml, "//consumerContentSet/filters/filter/patternType/text()", "BEGINS");
        assertScheduleExpression(xml, "//consumerContentSet");
    }
    
    public void testConvertConsumerContentSetListToXml() throws Exception {
        ConsumerContentSet c = ContentSetAssert.createConsumerContentSet();
        
        List<ConsumerContentSet> sets = new ArrayList<ConsumerContentSet>();
        sets.add(c);
        
        String xml = converter.to(MediaType.TEXT_XML, sets).getText();
        
        XPathAssert.assertXPathNodeExists(xml, "//sets");
        XPathAssert.assertXPathNodeExists(xml, "//sets/consumerContentSet");
        XPathAssert.assertXPathNodeValue(xml, "//sets/consumerContentSet/id/text()", "1");
        XPathAssert.assertXPathNodeValue(xml, "//sets/consumerContentSet/name/text()", "name");
        XPathAssert.assertXPathNodeValue(xml, "//sets/consumerContentSet/path/text()", FileSystemUtil.correctPath("path"));
        XPathAssert.assertXPathNodeValue(xml, "//sets/consumerContentSet/enabled/text()", "true");
        XPathAssert.assertXPathNodeValue(xml, "//sets/consumerContentSet/supplier/text()", "false");
        XPathAssert.assertXPathNodeValue(xml, "//sets/consumerContentSet/inventoryInterval/text()", "100");
        XPathAssert.assertXPathNodeValue(xml, "//sets/consumerContentSet/inventoryIntervalUnit/text()", "SECONDS");
        XPathAssert.assertXPathNodeValue(xml, "//sets/consumerContentSet/creationTime/text()", "200");
        XPathAssert.assertXPathNodeValue(xml, "//sets/consumerContentSet/description/text()", "description");
        XPathAssert.assertXPathNodeValue(xml, "//sets/consumerContentSet/supportsGbsTransport/text()", "true");
        XPathAssert.assertXPathNodeValue(xml, "//sets/consumerContentSet/fileDeleteDelay/text()", "100");
        XPathAssert.assertXPathNodeValue(xml, "//sets/consumerContentSet/fileDeleteDelayUnit/text()", "DAYS");
        XPathAssert.assertXPathNodeValue(xml, "//sets/consumerContentSet/maxFileSize/text()", "100");
        XPathAssert.assertXPathNodeValue(xml, "//sets/consumerContentSet/maxFileSizeUnit/text()", "MEGABYTES");
        XPathAssert.assertXPathNodeValue(xml, "//sets/consumerContentSet/supplierAddress/text()", "localhost");
        XPathAssert.assertXPathNodeValue(xml, "//sets/consumerContentSet/supplierName/text()", "Supplier");
        XPathAssert.assertXPathNodeValue(xml, "//sets/consumerContentSet/supplierPort/text()", "443");
        XPathAssert.assertXPathNodeExists(xml, "//sets/consumerContentSet/filters/filter");
        XPathAssert.assertXPathNodeValue(xml, "//sets/consumerContentSet/filters/filter/pattern/text()", "abc");
        XPathAssert.assertXPathNodeValue(xml, "//sets/consumerContentSet/filters/filter/patternType/text()", "BEGINS");
        assertScheduleExpression(xml, "//sets/consumerContentSet");       
     }
    
    private void assertScheduleExpression(String xml, String prefix) throws Exception {
        XPathAssert.assertXPathNodeExists(xml, prefix + "/scheduleExpressions/scheduleExpression");
        XPathAssert.assertXPathNodeValue(xml, prefix + "/scheduleExpressions/scheduleExpression/cronExpression/text()", "0 0/2 * * * ?");
        XPathAssert.assertXPathNodeValue(xml, prefix + "/scheduleExpressions/scheduleExpression/duration/text()", "10");
        XPathAssert.assertXPathNodeValue(xml, prefix + "/scheduleExpressions/scheduleExpression/durationUnit/text()", "MINUTES");
    }
    private void assertScheduleExpression(JSONArray expressions) throws Exception {
        assertEquals(1, expressions.length());
        JSONObject expression = expressions.getJSONObject(0).getJSONObject("scheduleExpression");
        assertFalse(expression == null);
        assertEquals("0 0/2 * * * ?", expression.getString("cronExpression"));
        assertEquals("10", expression.getString("duration"));
        assertEquals("MINUTES", expression.getString("durationUnit"));
    }
}
