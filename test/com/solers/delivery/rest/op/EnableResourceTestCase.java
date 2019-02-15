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
package com.solers.delivery.rest.op;

import org.restlet.data.MediaType;
import org.restlet.data.Status;

import com.solers.delivery.ContentSetAssert;
import com.solers.delivery.domain.ContentSet;
import com.solers.delivery.rest.BaseRestTestCase;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class EnableResourceTestCase extends BaseRestTestCase {
    
    public void setUp() {
        super.setUp();
        uri = "http://localhost:"+getPort()+"/op/enable/";
    }
    
    public void testPutWithoutAuth() throws Exception {
        authenticate = false;
        put("true", null, Status.CLIENT_ERROR_UNAUTHORIZED);
    }
    
    public void testEnable() {
        ContentSet set = ContentSetAssert.createContentSet();
        set.setEnabled(false);
        
        getDAOFactory().getContentSetDAO().makePersistent(set);
        uri = uri + "1";
        
        put("true", MediaType.TEXT_PLAIN, Status.SUCCESS_OK);
        
        assertTrue(set.isEnabled());
    }
    
    public void testDisable() {
        ContentSet set = ContentSetAssert.createContentSet();
        set.setEnabled(true);
        
        getDAOFactory().getContentSetDAO().makePersistent(set);
        uri = uri + "1";
        
        put("false", MediaType.TEXT_PLAIN, Status.SUCCESS_OK);
        
        assertFalse(set.isEnabled());
    }
    
    public void testBadEntityBody() {
        getDAOFactory().getContentSetDAO().makePersistent(ContentSetAssert.createContentSet());
        uri = uri + "1";
        
        put("falsafse", MediaType.TEXT_PLAIN, Status.CLIENT_ERROR_BAD_REQUEST);
    }
    
    public void testNotFound() {
        uri = uri + "1";
        
        put("false", MediaType.TEXT_PLAIN, Status.CLIENT_ERROR_NOT_FOUND);
    }
    
    public void testBadId() {
        uri = uri + "foo";
        
        put("false", MediaType.TEXT_PLAIN, Status.CLIENT_ERROR_BAD_REQUEST);
    }
}
