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
package com.solers.delivery.rest.content;

import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.data.Response;
import org.restlet.data.Status;

import com.solers.delivery.ContentSetAssert;
import com.solers.delivery.content.consumer.MockConsumerContentSetManager;
import com.solers.delivery.content.supplier.MockSupplierContentSetManager;
import com.solers.delivery.daos.MockDAOFactory;
import com.solers.delivery.domain.ConsumerContentSet;
import com.solers.delivery.domain.ContentSet;
import com.solers.delivery.rest.BaseRestTestCase;
import com.solers.delivery.rest.Utils;
import com.solers.delivery.rest.converter.ContentSetConverter;
import com.solers.delivery.rest.converter.ValidationExceptionConverter;
import com.solers.util.dao.ValidationException;

public class ListingResourceTestCase extends BaseRestTestCase {
    
    ContentSetConverter converter = new ContentSetConverter();
    
    public void testGetEmptyXmlList() throws Exception {
        Response r = get(MediaType.TEXT_XML);
        
        List<ContentSet> sets = converter.fromList(r.getEntity());
        assertNotNull(sets);
        assertTrue(sets.isEmpty());
    }
    
    public void testGetWithoutAuth() throws Exception {
        authenticate = false;
        get(null, Status.CLIENT_ERROR_UNAUTHORIZED);
    }
    
    public void testGetEmptyJsonList() throws Exception {
        Response r = get(MediaType.APPLICATION_JSON);
        
        List<ContentSet> sets = converter.fromList(r.getEntity());
        assertNotNull(sets);
        assertTrue(sets.isEmpty());
    }
    
    public void testGetXmlContentSets() throws Exception {
        MockDAOFactory factory = getDAOFactory();
        
        factory.getContentSetDAO().makePersistent(ContentSetAssert.createContentSet());
        factory.getConsumerContentSetDAO().makePersistent(ContentSetAssert.createConsumerContentSet());
        
        Response r = get(MediaType.TEXT_XML);
        
        List<ContentSet> sets = converter.fromList(r.getEntity());
        assertNotNull(sets);
        assertEquals(2, sets.size());
        ContentSetAssert.assertContentSet(sets.get(0));
        ContentSetAssert.assertConsumerContentSet((ConsumerContentSet)sets.get(1));
    }
    
    public void testGetJSONContentSets() throws Exception {
        MockDAOFactory factory = getDAOFactory();
        
        factory.getContentSetDAO().makePersistent(ContentSetAssert.createContentSet());
        factory.getConsumerContentSetDAO().makePersistent(ContentSetAssert.createConsumerContentSet());
        
        Response r = get(MediaType.TEXT_XML);
        
        List<ContentSet> sets = converter.fromList(r.getEntity());
        assertNotNull(sets);
        assertEquals(2, sets.size());
        ContentSetAssert.assertContentSet(sets.get(0));
        ContentSetAssert.assertConsumerContentSet((ConsumerContentSet)sets.get(1));
    }
    
    public void testPutWithoutAuth() throws Exception {
        authenticate = false;
        put(ContentSetAssert.CONTENT_SET_XML_NEW, null, Status.CLIENT_ERROR_UNAUTHORIZED);
    }
    
    public void testPutXmlContentSet() throws Exception {
        put(ContentSetAssert.CONTENT_SET_XML_NEW, MediaType.TEXT_XML);
        
        MockDAOFactory factory = getDAOFactory();
        MockSupplierContentSetManager manager = getSupplierManager();
        
        List<ContentSet> sets = factory.getContentSetDAO().findAll();
        assertNotNull(sets);
        assertEquals(1, sets.size());
        
        ContentSet set = sets.get(0);
        ContentSetAssert.assertContentSet(set);
        assertSame(set, manager.added);
    }
    
    public void testValidationException() throws Exception {
        
        getDAOFactory().getContentSetDAO().throwOnSave = true;
        
        Response r = put(ContentSetAssert.CONTENT_SET_XML_NEW, MediaType.TEXT_XML, Utils.CLIENT_ERROR_VALIDATION);
        
        ValidationException ex = (ValidationException) new ValidationExceptionConverter().from(r.getEntity());
        
        assertNotNull(ex);
        assertEquals(3, ex.getMessages().size());
        assertTrue(ex.getMessages().contains("test1"));
        assertTrue(ex.getMessages().contains("test2"));
        assertTrue(ex.getMessages().contains("test3"));
    }
    
    /**
     * Putting an entity with an ID is invalid
     */
    public void testPutXmlContentSetWithId() throws Exception {
        put(ContentSetAssert.CONTENT_SET_XML_EXISTING, MediaType.TEXT_XML, Status.CLIENT_ERROR_BAD_REQUEST);
        
        MockDAOFactory factory = getDAOFactory();
        MockSupplierContentSetManager manager = getSupplierManager();
        
        List<ContentSet> sets = factory.getContentSetDAO().findAll();
        assertNotNull(sets);
        assertTrue(sets.isEmpty());

        assertNull(manager.added);
    }
    
    /**
     * Putting an entity with an ID is invalid
     */
    public void testPutJSONContentSetWithId() throws Exception {
        put(ContentSetAssert.CONTENT_SET_JSON_WITH_ID, MediaType.APPLICATION_JSON, Status.CLIENT_ERROR_BAD_REQUEST);
        
        MockDAOFactory factory = getDAOFactory();
        MockSupplierContentSetManager manager = getSupplierManager();
        
        List<ContentSet> sets = factory.getContentSetDAO().findAll();
        assertNotNull(sets);
        assertTrue(sets.isEmpty());

        assertNull(manager.added);
    }
    
    public void testPutJSONContentSet() throws Exception {
        put(ContentSetAssert.CONTENT_SET_JSON_WITHOUT_ID, MediaType.APPLICATION_JSON);
        
        MockDAOFactory factory = getDAOFactory();
        MockSupplierContentSetManager manager = getSupplierManager();
        
        List<ContentSet> sets = factory.getContentSetDAO().findAll();
        assertNotNull(sets);
        assertEquals(1, sets.size());
        
        ContentSet set = sets.get(0);
        ContentSetAssert.assertContentSet(set);
        assertSame(set, manager.added);
    }
    
    public void testPutJSONConsumerContentSet() throws Exception {
        put(ContentSetAssert.CONSUMER_CONTENT_SET_JSON_WITHOUT_ID, MediaType.APPLICATION_JSON);
        
        MockDAOFactory factory = getDAOFactory();
        MockConsumerContentSetManager manager = getConsumerManager();
        
        List<ConsumerContentSet> sets = factory.getConsumerContentSetDAO().findAll();
        assertNotNull(sets);
        assertEquals(1, sets.size());
        
        ConsumerContentSet set = sets.get(0);
        ContentSetAssert.assertConsumerContentSet(set);
        assertSame(set, manager.added);
    }
    
    public void testPutXmlConsumerContentSet() throws Exception {
        put(ContentSetAssert.CONSUMER_CONTENT_SET_XML_NEW, MediaType.TEXT_XML);
        
        MockDAOFactory factory = getDAOFactory();
        MockConsumerContentSetManager manager = getConsumerManager();
        
        List<ConsumerContentSet> sets = factory.getConsumerContentSetDAO().findAll();
        assertNotNull(sets);
        assertEquals(1, sets.size());
        
        ConsumerContentSet set = sets.get(0);
        ContentSetAssert.assertConsumerContentSet(set);
        assertSame(set, manager.added);
    }
    
    public void testPutBadEntity() throws Exception {
        put("foobar", MediaType.TEXT_XML, Status.CLIENT_ERROR_BAD_REQUEST);
        put("jfj", MediaType.APPLICATION_JSON, Status.CLIENT_ERROR_BAD_REQUEST);
    }
}
