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
package com.solers.delivery.content;

import java.util.List;

import com.solers.delivery.ContentSetAssert;
import com.solers.delivery.content.RestfulContentService;
import com.solers.delivery.domain.ConsumerContentSet;
import com.solers.delivery.domain.ContentSet;
import com.solers.delivery.rest.BaseRestTestCase;
import com.solers.delivery.rest.auth.MockRestAuthentication;
import com.solers.delivery.util.FileSystemUtil;
import com.solers.util.dao.ValidationException;


/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class RestfulContentServiceTestCase extends BaseRestTestCase {
    
    RestfulContentService service;
    
    public void setUp() {
        super.setUp();
        service = new RestfulContentService("localhost", getPort(), new MockRestAuthentication());
    }
    
    public void testGetContentSets() {
        getDAOFactory().getContentSetDAO().makePersistent(ContentSetAssert.createContentSet());
        
        List<ContentSet> sets = service.getContentSets();
        
        assertNotNull(sets);
        assertEquals(1, sets.size());
        
        ContentSetAssert.assertContentSet(sets.get(0));
    }
    
    public void testGetSuppliers() {
        getDAOFactory().getContentSetDAO().makePersistent(ContentSetAssert.createContentSet());
        
        List<ContentSet> sets = service.getSuppliers();
        
        assertNotNull(sets);
        assertEquals(1, sets.size());
        
        ContentSetAssert.assertContentSet(sets.get(0));
    }
    
    public void testGetConsumers() {
        getDAOFactory().getConsumerContentSetDAO().makePersistent(ContentSetAssert.createConsumerContentSet());
        
        List<ConsumerContentSet> sets = service.getConsumers();
        
        assertNotNull(sets);
        assertEquals(1, sets.size());
        
        ContentSetAssert.assertConsumerContentSet(sets.get(0));
    }
    
    public void testGet() {
        getDAOFactory().getContentSetDAO().makePersistent(ContentSetAssert.createContentSet());
        
        ContentSet set = service.get(1l);
        
        assertNotNull(set);
        ContentSetAssert.assertContentSet(set);
    }
    
    public void testGetNotFound() {
        ContentSet set = service.get(1l);
        
        assertNull(set);
    }
    
    public void testGetConsumer() {
        getDAOFactory().getConsumerContentSetDAO().makePersistent(ContentSetAssert.createConsumerContentSet());
        
        ConsumerContentSet set = service.get(1l);
        
        assertNotNull(set);
        ContentSetAssert.assertConsumerContentSet(set);
    }
    
    public void testRemove() {
        getDAOFactory().getContentSetDAO().makePersistent(ContentSetAssert.createContentSet());
        
        assertFalse(getDAOFactory().getContentSetDAO().findAll().isEmpty());
        
        ContentSet set = service.get(1l);
        service.remove(set.getId());
        
        assertTrue(getDAOFactory().getContentSetDAO().findAll().isEmpty());
    }
    
    public void testEnable() {
        ContentSet set = ContentSetAssert.createContentSet();
        set.setEnabled(false);
        getDAOFactory().getContentSetDAO().makePersistent(set);
        
        set = service.get(1l);
        assertFalse(set.isEnabled());
        
        service.enable(set.getId());
        set = service.get(1l);
        assertTrue(set.isEnabled());
    }
    
    public void testDisable() {
        ContentSet set = ContentSetAssert.createContentSet();
        set.setEnabled(true);
        getDAOFactory().getContentSetDAO().makePersistent(set);
        
        set = service.get(1l);
        assertTrue(set.isEnabled());
        
        service.disable(set.getId());
        set = service.get(1l);
        assertFalse(set.isEnabled());
    }
    
    public void testSaveNew() {
        ContentSet set = ContentSetAssert.createContentSet();
        set.setId(null);
        
        long id = service.save(set);
        
        assertEquals(1, id);
        
        List<ContentSet> sets = getDAOFactory().getContentSetDAO().findAll();
        
        assertNotNull(sets);
        assertEquals(1, sets.size());
        ContentSetAssert.assertContentSet(sets.get(0));
    }
    
    public void testSaveUpdated() {
        ContentSet set = ContentSetAssert.createContentSet();
        getDAOFactory().getContentSetDAO().makePersistent(set);
        
        set = service.get(1l);
        
        set.setDescription("testSaveUpdated description");
        set.setPath("/new/path");
        
        long id = service.save(set);
        
        assertEquals(1, id);
        
        set = service.get(1l);
        
        assertEquals("testSaveUpdated description", set.getDescription());
        assertEquals(FileSystemUtil.correctPath("/new/path"), set.getPath());
        
        List<ContentSet> sets = getDAOFactory().getContentSetDAO().findAll();
        
        assertNotNull(sets);
        assertEquals(1, sets.size());
    }
    
    public void testValidationException() {
        ContentSet set = ContentSetAssert.createContentSet();
        getDAOFactory().getContentSetDAO().makePersistent(set);
        
        getDAOFactory().getContentSetDAO().throwOnSave = true;
        
        try {
            service.save(set);
            fail();
        } catch (ValidationException ex) {
            assertEquals(3, ex.getMessages().size());
            assertTrue(ex.getMessages().contains("test1"));
            assertTrue(ex.getMessages().contains("test2"));
            assertTrue(ex.getMessages().contains("test3"));
        }
    }
}
