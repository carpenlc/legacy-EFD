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

import junit.framework.TestCase;

import com.solers.delivery.content.consumer.MockConsumerContentSetManager;
import com.solers.delivery.content.supplier.MockSupplierContentSetManager;
import com.solers.delivery.daos.DAOFactory;
import com.solers.delivery.daos.MockDAOFactory;
import com.solers.delivery.domain.AllowedHost;
import com.solers.delivery.domain.ConsumerContentSet;
import com.solers.delivery.domain.ContentSet;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class ContentServiceTestCase extends TestCase {
    
    ContentService service;
    DAOFactory factory;
    
    public void setUp() {
        factory = new MockDAOFactory();
        service = new ContentServiceImpl(factory, new MockSupplierContentSetManager(), new MockConsumerContentSetManager());
    }
    
    public void testGetConsumers() {
        ConsumerContentSet set = new ConsumerContentSet();
        factory.getConsumerContentSetDAO().makePersistent(set);
        
        List<ConsumerContentSet> sets = service.getConsumers();
        
        assertEquals(1, sets.size());
        assertSame(set, sets.get(0));
    }
    
    public void testGetSuppliers() {
        ContentSet set = new ContentSet();
        factory.getContentSetDAO().makePersistent(set);
        
        List<ContentSet> sets = service.getSuppliers();
        
        assertEquals(1, sets.size());
        assertSame(set, sets.get(0));
    }
    
    public void testGetContentSets() {
        ConsumerContentSet set = new ConsumerContentSet();
        ContentSet set2 = new ContentSet();
        factory.getConsumerContentSetDAO().makePersistent(set);
        factory.getContentSetDAO().makePersistent(set2);
        
        List<ContentSet> sets = service.getContentSets();
        
        assertEquals(2, sets.size());
        assertTrue(sets.contains(set));
        assertTrue(sets.contains(set2));
    }
    
    public void testSaveWithNewAllowedHost() {
        ContentSet contentSet = new ContentSet();
        contentSet.setName("foo");
        contentSet.setSupplier(true);
        contentSet.addAllowedHost(new AllowedHost("foo", "bar"));
        
        service.save(contentSet);
        
        long id = contentSet.getId();
        
        assertEquals(contentSet, service.get(id));
        
        AllowedHost host = factory.getAllowedHostDAO().getByAlias("foo");
        assertEquals("foo", host.getAlias());
        assertEquals("bar", host.getCommonName());
        assertTrue(contentSet.getUpdateTime() > 0);
        assertTrue(contentSet.getUpdateTime() <= System.currentTimeMillis());
    }
    
    public void testSaveWithExistingAllowedHost() {
        ContentSet contentSet = new ContentSet();
        contentSet.setName("foo");
        contentSet.setSupplier(true);
        contentSet.addAllowedHost(new AllowedHost("foo", "bar"));
        
        factory.getAllowedHostDAO().makePersistent(new AllowedHost("foo", "bar"));
        
        service.save(contentSet);
        
        long id = contentSet.getId();
        
        assertEquals(contentSet, service.get(id));
        
        assertEquals(1, factory.getAllowedHostDAO().findAll().size());
        assertTrue(contentSet.getUpdateTime() > 0);
        assertTrue(contentSet.getUpdateTime() <= System.currentTimeMillis());
    }
}
