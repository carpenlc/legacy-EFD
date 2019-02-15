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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.solers.delivery.daos.MockDAOFactory;
import com.solers.delivery.domain.User;
import com.solers.delivery.user.UserService;
import com.solers.delivery.user.UserServiceImpl;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class UserServiceImplTestCase {
    
    private MockDAOFactory factory;
    private UserService service;
    
    @Before
    public void setUp() {
        factory = new MockDAOFactory();
        service = new UserServiceImpl(factory);
    }
    
    @Test
    public void testEnable() {
        User user = new User();
        service.save(user);
        
        user.setEnabled(false);
        user.setFailedLogins(5);
        
        service.enable(user.getId());
        
        Assert.assertTrue(user.isEnabled());
        Assert.assertEquals(0, user.getFailedLogins());
    }
    
}
