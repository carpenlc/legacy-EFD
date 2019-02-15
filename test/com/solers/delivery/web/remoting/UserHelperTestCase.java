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
package com.solers.delivery.web.remoting;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.solers.delivery.daos.DAOFactory;
import com.solers.delivery.daos.MockDAOFactory;
import com.solers.delivery.domain.User;
import com.solers.delivery.user.PasswordService;
import com.solers.delivery.user.PasswordServiceImpl;
import com.solers.delivery.user.UserService;
import com.solers.delivery.user.UserServiceImpl;
import com.solers.delivery.util.password.InvalidPasswordException;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class UserHelperTestCase {
    
    private UserHelper helper;
    private UserService userService;
    private DAOFactory factory;
    
    @Before
    public void setUp() {
        factory = new MockDAOFactory();
        PasswordService passwordService = new PasswordServiceImpl(factory);
        userService = new UserServiceImpl(factory);
        helper = new UserHelper(userService, passwordService);
    }
    
    @Test
    public void testSaveNewUserWithInvalidPassword() {
        User user = new User();
        user.setUsername("testSaveNewUserWithInvalidPassword");
        
        
        try {
            helper.saveUser(user, "", "");
            Assert.fail();
        } catch (InvalidPasswordException expected) {
            
        }
        Assert.assertEquals(0, userService.getUsers().size());
        
        user = new User();
        user.setUsername("testSaveNewUserWithInvalidPassword");
        try {
            helper.saveUser(user, "xxxxxxx", "");
            Assert.fail();
        } catch (InvalidPasswordException expected) {
            
        }
        Assert.assertEquals(0, userService.getUsers().size());
        
        user = new User();
        user.setUsername("testSaveNewUserWithInvalidPassword");
        try {
            helper.saveUser(user, "xxx", "xxx");
            Assert.fail();
        } catch (InvalidPasswordException expected) {
            
        }
        Assert.assertEquals(0, userService.getUsers().size());
    }
    
    @Test
    public void testSaveNewUser() {
        User user = new User();
        user.setUsername("testSaveNewUser");
        user.setFirstName("firstName");
        user.setLastName("lastName");
        
        Long id = helper.saveUser(user, "!@#123ADSolers1", "!@#123ADSolers1");
        
        Assert.assertEquals(id, user.getId());
    }
    
    @Test
    public void testUpdateUser() {
        User user = new User();
        user.setUsername("testUpdateUser");
        user.setFirstName("firstName");
        user.setLastName("lastName");
        
        helper.saveUser(user, "!@#123ADSolers1", "!@#123ADSolers1");
        
        user.setFirstName("foobar");
        helper.saveUser(user, "", "");
        Assert.assertEquals("foobar", userService.get(user.getId()).getFirstName());
        
        
        User clone = new User();
        clone.setId(user.getId());
        clone.setFirstName("xxxx");
        clone.setLastName("yyyyy");
        clone.setUsername("clone");
        try {
            helper.saveUser(clone, "!@#123ADSolers1", "xxxx");
            Assert.fail();
        } catch (InvalidPasswordException expected) {
            
        }
        
        Assert.assertEquals(1, userService.getUsers().size());
        Assert.assertEquals("foobar", userService.get(user.getId()).getFirstName());
        
        try {
            helper.saveUser(clone, "", "xxxx");
            Assert.fail();
        } catch (InvalidPasswordException expected) {
            
        }
        
        Assert.assertEquals(1, userService.getUsers().size());
        Assert.assertEquals("foobar", userService.get(user.getId()).getFirstName());
    }
}
