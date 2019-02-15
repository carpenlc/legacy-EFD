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
package com.solers.delivery.web.user;

import java.util.List;
import java.util.Properties;

import org.junit.Assert;

import com.solers.delivery.domain.User;
import com.solers.delivery.rest.BaseRestTestCase;
import com.solers.delivery.rest.auth.MockRestAuthentication;
import com.solers.delivery.rest.converter.AdminConverter;
import com.solers.delivery.security.PasswordType;
import com.solers.delivery.user.RestfulUserService;
import com.solers.delivery.user.UserService;
import com.solers.security.password.MapPasswordProvider;
import com.solers.util.dao.ValidationException;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class RestfulUserServiceTestCase extends BaseRestTestCase {
    
    private UserService service;
    
    @Override
    public void setUp() {
        super.setUp();
        MapPasswordProvider provider = new MapPasswordProvider();
        provider.setPassword(PasswordType.ENCRYPTION.key(), "41ba6c532588f2b03497000f22a9d504c6b41ee180a65f2c875fad715417b6ea");
        service = new RestfulUserService("localhost", getPort(), new MockRestAuthentication(), new Properties(), new AdminConverter(provider));
    }
    
    public void testGetUsers() {
        List<User> users = service.getUsers();
        Assert.assertEquals(0, users.size());
        
        User user = createUser();
        getDAOFactory().getUserDAO().makePersistent(user);
        
        users = service.getUsers();
        Assert.assertEquals(1, users.size());
        assertEquals(user, users.get(0));
    }
    
    public void testGetById() {
        User actual = service.get(4L);
        assertNull(actual);
        
        User expected = createUser();
        getDAOFactory().getUserDAO().makePersistent(expected);
        
        actual = service.get(4L);
        assertEquals(expected, actual);
    }
    
    public void testGetByUsername() {
        User actual = service.get("username");
        assertNull(actual);
        
        User expected = createUser();
        expected.setUsername("user name");
        getDAOFactory().getUserDAO().makePersistent(expected);
        
        actual = service.get("user name");
        assertEquals(expected, actual);
    }
    
    public void testEnable() {
        User user = createUser();
        user.setEnabled(false);
        getDAOFactory().getUserDAO().makePersistent(user);
        
        service.enable(user.getId());
        
        user = service.get(user.getId());
        assertTrue(user.isEnabled());
    }
    
    public void testDisable() {
        User user = createUser();
        user.setEnabled(true);
        getDAOFactory().getUserDAO().makePersistent(user);
        
        service.disable(user.getId());
        
        user = service.get(user.getId());
        assertFalse(user.isEnabled());
    }
    
    public void testRemove() {
        User user = createUser();
        getDAOFactory().getUserDAO().makePersistent(user);
        
        user = service.get(user.getId());
        assertNotNull(user);
        
        service.remove(user.getId());
        
        user = service.get(user.getId());
        assertNull(user);
    }
    
    public void testSaveNewUser() {
        User user = createUser();
        user.setId(null);
        
        Long id = service.save(user);
        
        assertNotNull(id);
        
        User saved = service.get(id);
        
        assertEquals(user.getUsername(), saved.getUsername());
    }
    
    public void testUpdateUser() {
        User user = createUser();
        getDAOFactory().getUserDAO().makePersistent(user);
        
        user.setUsername("new username");
        
        Long id = service.save(user);
        
        User saved = service.get(id);
        
        assertEquals(user.getId(), id);
        assertEquals("new username", saved.getUsername());
    }
    
    public void testSaveWithValidationException() {
        getDAOFactory().getUserDAO().throwOnSave = true;
        
        User user = createUser();
        user.setId(null);
        
        try {
            service.save(user);
            fail();
        } catch (ValidationException expected) {
            List<String> messages = expected.getMessages();
            assertEquals(3, messages.size());
            assertEquals("test1", messages.get(0));
            assertEquals("test2", messages.get(1));
            assertEquals("test3", messages.get(2));
        }
    }
    
    private User createUser() {
        User user = new User();
        user.setId(4L);
        user.setFirstName("first name");
        user.setLastName("last name");
        user.setUsername("username");
        return user;
    }
    
    private void assertEquals(User expected, User actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.getUsername(), actual.getUsername());
    }
}
