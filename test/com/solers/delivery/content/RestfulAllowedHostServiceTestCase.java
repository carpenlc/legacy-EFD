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

import com.solers.delivery.domain.AllowedHost;
import com.solers.delivery.rest.BaseRestTestCase;
import com.solers.delivery.rest.auth.MockRestAuthentication;
import com.solers.util.dao.ValidationException;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class RestfulAllowedHostServiceTestCase extends BaseRestTestCase {
    
    private AllowedHostService service;
    
    @Override
    public void setUp() {
        super.setUp();
        service = new RestfulAllowedHostService("localhost", getPort(), new MockRestAuthentication());
    }
    
    public void testList() {
        assertEquals(0, service.list().size());
        
        AllowedHost expected = new AllowedHost();
        expected.setAlias("my alias");
        expected.setCommonName("common name");
        expected.setId(2L);
        
        getDAOFactory().getAllowedHostDAO().makePersistent(expected);
        
        List<AllowedHost> hosts = service.list();
        
        assertEquals(1, hosts.size());
        
        AllowedHost actual = hosts.get(0);
        
        assertEquals(expected.getAlias(), actual.getAlias());
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getCommonName(), actual.getCommonName());
    }
    
    public void testGet() {
        
        assertNull(service.get("my alias"));
        
        AllowedHost expected = new AllowedHost();
        expected.setAlias("my alias");
        expected.setCommonName("common name");
        expected.setId(2L);
        
        getDAOFactory().getAllowedHostDAO().makePersistent(expected);
        
        AllowedHost actual = service.get("my alias");
        
        assertEquals(expected.getAlias(), actual.getAlias());
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getCommonName(), actual.getCommonName());
    }
    
    public void testRemove() {
        assertNull(service.get("my alias"));
        
        AllowedHost expected = new AllowedHost();
        expected.setAlias("my alias");
        expected.setCommonName("common name");
        expected.setId(2L);
        
        getDAOFactory().getAllowedHostDAO().makePersistent(expected);
        
        assertNotNull(service.get("my alias"));
        
        service.remove(expected);
        assertNull(service.get("my alias"));
    }
    
    public void testSaveNew() {
        AllowedHost host = new AllowedHost();
        host.setAlias("my alias");
        host.setCommonName("common name");
        host.setId(null);
        
        Long id = service.save(host);
        
        assertNotNull(id);
        
        AllowedHost saved = service.get(host.getAlias());
        
        assertEquals(host.getAlias(), saved.getAlias());
    }
    
    public void testSaveUpdate() {
        AllowedHost host = new AllowedHost();
        host.setAlias("my alias");
        host.setCommonName("common name");
        host.setId(2L);
        getDAOFactory().getAllowedHostDAO().makePersistent(host);
        
        AllowedHost newHost = new AllowedHost();
        newHost.setAlias("new alias");
        newHost.setCommonName("new common name");
        newHost.setId(2L);
        
        service.save(newHost);
        
        AllowedHost actual = service.get(newHost.getAlias());
        
        assertEquals(newHost.getAlias(), actual.getAlias());
        assertEquals(newHost.getId(), actual.getId());
        assertEquals(newHost.getCommonName(), actual.getCommonName());
    }
    
    public void testSaveValidationException() {
        AllowedHost newHost = new AllowedHost();
        newHost.setAlias("foobar");
        newHost.setCommonName(null);
        newHost.setId(2L);
        
        getDAOFactory().getAllowedHostDAO().throwOnSave = true;
        
        try {
            service.save(newHost);
            fail();
        } catch (ValidationException expected) {
            
        }
    }
}
