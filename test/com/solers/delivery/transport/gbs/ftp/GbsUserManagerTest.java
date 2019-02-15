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
package com.solers.delivery.transport.gbs.ftp;

import java.io.IOException;

import junit.framework.Assert;
import junit.framework.JUnit4TestAdapter;

import org.apache.ftpserver.ftplet.Authentication;
import org.apache.ftpserver.ftplet.AuthenticationFailedException;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the Gbs User Manager that handles the logins of FTP connections
 * 
 * @author dthemistokleous
 * 
 */
public class GbsUserManagerTest {

    private static final String admin = "admin";
    private static final String password = "password";
    private static FTPUserManager manager = null;
    private static final String homeDir = ".";
    /**
     * Runs before the tests are executed
     * 
     * @throws IOException
     * @throws FtpException
     */
    @BeforeClass
    public static void setup() throws Exception {
        
        manager = new FTPUserManager();
        manager.setName(admin);
        manager.setArchive(homeDir);
        manager.setPassword(password);
        manager.configure();
    }

    @Test
    public void testIsAdmin() throws FtpException {
        Assert.assertTrue(manager.isAdmin(admin));
    }

    @Test
    public void testIsNotAdmin() throws FtpException {
        Assert.assertFalse(manager.isAdmin("cat"));
    }

    @Test
    public void testSuccessAuthenticate() throws FtpException {
    	Authentication auth = new UsernamePasswordAuthentication(admin,password);
        User user = manager.authenticate(auth);
        Assert.assertNotNull(user);
        Assert.assertNotNull(user.getName());
        Assert.assertTrue(user.getName().equals(admin));
    }

    @Test
    public void testFailAuthenticate() throws FtpException {
    	try {
    	   Authentication auth = new UsernamePasswordAuthentication(admin,"cat");
           Assert.assertNull(manager.authenticate(auth));
        
    	   auth = new UsernamePasswordAuthentication("dog", password);
           Assert.assertNull(manager.authenticate(auth));       
    	} catch (AuthenticationFailedException ae) {
    		return;
    	}
    	
    	Assert.fail();
    }

    @Test
    public void testDeleteUser() {
        try {
            manager.delete(admin);
        } catch (FtpException e) {
            return;
        }
        Assert.fail();
    }

    @Test
    public void testUserExists() throws FtpException {
        Assert.assertTrue(manager.doesExist(admin));
    }

    @Test
    public void testUserDoesNotExists() throws FtpException {
        Assert.assertFalse(manager.doesExist("dog"));
    }

    @Test
    public void testSuccessAdminName() throws FtpException {
        Assert.assertEquals(admin, manager.getAdminName());
    }
    
    @Test
    public void testFailAdminName() throws FtpException {
        String admin = manager.getAdminName();
        if ("cat".equals(admin)) {
            Assert.fail();
        }
    }

    @Test
    public void testOnlyReturnAdminName() throws FtpException {
        String[] names = manager.getAllUserNames();
        if (names.length != 1) {
            Assert.fail();
        }

        for (String adminName : names) {
            Assert.assertEquals(adminName, admin);
        }
    }

   @Test
    public void testGetUserByName() throws FtpException {
        User user = manager.getUserByName(admin);
        Assert.assertEquals(user.getName(), admin); 
        Assert.assertEquals(user.getHomeDirectory(), homeDir);
    }

    @Test
    public void testFailedUserSave() {
        BaseUser user = new BaseUser();
        user.setName("cat");
        
        try {
            manager.save(user);
        } catch (FtpException e) {
            Assert.fail();
        }
    }

    /**
     * Runs after all tests have completed
     */
    @AfterClass
    public static void teardown() {
    // do nothing
    }

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(GbsUserManagerTest.class);
    }
}
