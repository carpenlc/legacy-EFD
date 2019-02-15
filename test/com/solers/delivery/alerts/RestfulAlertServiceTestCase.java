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
package com.solers.delivery.alerts;

import java.util.ArrayList;
import java.util.List;

import com.solers.delivery.domain.Alert;
import com.solers.delivery.domain.Alert.AlertType;
import com.solers.delivery.rest.BaseRestTestCase;
import com.solers.delivery.rest.auth.MockRestAuthentication;
import com.solers.delivery.rest.converter.AdminConverter;
import com.solers.delivery.security.PasswordType;
import com.solers.security.password.MapPasswordProvider;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class RestfulAlertServiceTestCase extends BaseRestTestCase {
    
    private AlertService service;
    
    @Override
    public void setUp() {
        super.setUp();
        MapPasswordProvider provider = new MapPasswordProvider();
        provider.setPassword(PasswordType.ENCRYPTION.key(), "41ba6c532588f2b03497000f22a9d504c6b41ee180a65f2c875fad715417b6ea");
        service = new RestfulAlertService("localhost", getPort(), new MockRestAuthentication(), new AdminConverter(provider));
    }
    
    public void testList() {
        getDAOFactory().getAlertDAO().makePersistent(new Alert("user", AlertType.USER));
        getDAOFactory().getAlertDAO().makePersistent(new Alert("admin", AlertType.ADMIN));
        
        List<Alert> result = service.list(AlertType.USER, 0 ,0).getPage();
        assertEquals(1, result.size());
        assertEquals("user", result.get(0).getMessage());
        
        result = service.list(AlertType.ADMIN, 0 ,0).getPage();
        assertEquals(1, result.size());
        assertEquals("admin", result.get(0).getMessage());
        
        result = service.list(AlertType.ALL, 0 ,0).getPage();
        assertEquals(0, result.size());
    }
    
    public void testRemove() {
        getDAOFactory().getAlertDAO().makePersistent(new Alert("user", AlertType.USER));
        List<Alert> result = service.list(AlertType.USER, 0 ,0).getPage();
        Alert alert = result.get(0);
        
        service.remove(alert.getId());
        
        result = service.list(AlertType.USER, 0 ,0).getPage();
        assertEquals(0, result.size());
    }
    
    public void testSave() {
        List<Alert> result = service.list(AlertType.USER, 0 ,0).getPage();
        assertEquals(0, result.size());
        
        Alert alert = new Alert("user", AlertType.USER);
        service.save(alert);
        
        result = service.list(AlertType.USER, 0 ,0).getPage();
        assertEquals(1, result.size());
        assertEquals(alert.getMessage(), result.get(0).getMessage());
        assertEquals(alert.getTimestamp(), result.get(0).getTimestamp());
    }
    
    public void testGet() {
        assertNull(service.get(1L));
        
        Alert alert = new Alert("user", AlertType.USER);
        alert.setId(1L);
        getDAOFactory().getAlertDAO().makePersistent(alert);
        
        alert = service.get(1L);
        
        assertEquals("user", alert.getMessage());
    }
    
    public void testRemoveMultiple() {
        //Create 5 alerts to delete
        for(int i=0;i<5;i++) {
            getDAOFactory().getAlertDAO().makePersistent(new Alert("user", AlertType.USER));
        }
        List<Alert> result = service.list(AlertType.USER, 0 ,0).getPage();
        
        List<Long> alertIds = new ArrayList<Long>(result.size());
        for(Alert alert : result) {
            alertIds.add(alert.getId());
        }
        
        service.remove(alertIds);
        
        result = service.list(AlertType.USER, 0 ,0).getPage();
        assertEquals(0, result.size());
    }
}
