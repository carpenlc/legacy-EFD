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
package com.solers.delivery.management;

import java.util.List;

import com.solers.delivery.ContentSetAssert;
import com.solers.delivery.StatusAssert;
import com.solers.delivery.content.status.CurrentSynchronization;
import com.solers.delivery.management.ConsumerStatus;
import com.solers.delivery.management.RestfulStatusService;
import com.solers.delivery.management.SupplierStatus;
import com.solers.delivery.rest.BaseRestTestCase;
import com.solers.delivery.rest.auth.MockRestAuthentication;
import com.solers.delivery.transport.http.TransferStatus;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class RestfulStatusServiceTestCase extends BaseRestTestCase {
    
    RestfulStatusService service;
    
    public void setUp() {
        super.setUp();
        service = new RestfulStatusService("localhost", getPort(), new MockRestAuthentication());
    }
    
    public void testGetSupplierStatus() {
        getDAOFactory().getContentSetDAO().makePersistent(ContentSetAssert.createContentSet());
        getRegistrar().registerManagedBean(StatusAssert.proxy(SupplierStatus.class), "suppliers", new Long(1));
        
        SupplierStatus status = service.getSupplierStatus(1l);
        
        assertTrue(status.isEnabled());
        assertEquals(5, status.getNextEstimatedRuntime());
        assertEquals(5, status.getItemCount());
        assertEquals(5, status.getLastRuntime());
        assertEquals(5, status.getSize());
        
        List<CurrentSynchronization> syncs = status.getCurrentSynchronizations();
        assertEquals(1, syncs.size());
        assertEquals("mock-name", syncs.get(0).getContentSetName());
        assertEquals("mock-id", syncs.get(0).getId());
        assertEquals(1, syncs.get(0).getCurrentTransfers().size());
        assertEquals(200000, syncs.get(0).getCurrentTransfers().get(0).getBytesTransferred());
        assertEquals("/mock/filename2", syncs.get(0).getCurrentTransfers().get(0).getFileName());
    }
    
    public void testGetConsumerStatus() {
        getDAOFactory().getContentSetDAO().makePersistent(ContentSetAssert.createConsumerContentSet());
        getRegistrar().registerManagedBean(StatusAssert.proxy(ConsumerStatus.class), "suppliers", new Long(1));
        
        ConsumerStatus status = service.getConsumerStatus(1l);
        
        assertTrue(status.isEnabled());
        assertEquals(5, status.getNextEstimatedRuntime());
        assertEquals(5, status.getItemCount());
        assertEquals(5, status.getLastRuntime());
        assertEquals(5, status.getSize());
        
        List<TransferStatus> xfers = status.getCurrentTransfers();
        
        assertEquals(2, xfers.size());
        assertEquals(50.0d, xfers.get(0).getPercentComplete(), 0.0d);
        assertEquals(100000, xfers.get(0).getBytesTransferred());
        assertEquals("/mock/filename1", xfers.get(0).getFileName());
        
        assertEquals(70.0d, xfers.get(1).getPercentComplete(), 0.0d);
        assertEquals(200000, xfers.get(1).getBytesTransferred());
        assertEquals("/mock/filename2", xfers.get(1).getFileName());
    }
    
    public void testGetNullStatus() {
        getDAOFactory().getContentSetDAO().makePersistent(ContentSetAssert.createConsumerContentSet());
        
        ConsumerStatus status = service.getConsumerStatus(1l);
        
        assertNull(status);
    }
}
