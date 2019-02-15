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
package com.solers.delivery;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.solers.delivery.domain.User;

public class ContextualAdminMenuTestCase extends BaseSeleniumTestCase {
    User adminUser;
    User webUser;
    String adminUserPath;
    String webUserPath;
    
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.adminUser = helper.getTestAdminUser();
        this.webUser = helper.getTestUser();

        String userPath = "//div[@id='users-tree']//a[span='%s']";
        this.adminUserPath = String.format(userPath, this.adminUser.getUsername());
        this.webUserPath = String.format(userPath, this.webUser.getUsername());
        
        this.helper.loginAdmin();
        
        //click on Users on the nav panel
        this.selenium.doubleClick("//div[@id='users-tree']//a[span='Users']");
        //wait for User node to expand
        new Waiter(1000) {
            public boolean condition() {
                return selenium.isVisible(adminUserPath);
            }
        }.doWait();

    }
    
    @After
    public void tearDown() throws Exception {
        this.helper.logout();
        super.tearDown();
    }
    
    @Test
    public void testContextualAdminMenu() throws Exception {
        //context menu get generated and inserted to the bottom of the body tag
        String editPath = "//body/div[last()]//li[a='Edit']";
        String disablePath = "//body/div[last()]//li[a='Disable']";
        String deletePath = "//body/div[last()]//li[a='Delete']";
        
        //right click on self, should get truncated menu
        //click slightly to the right to allow clicking of the next one 
        this.selenium.contextMenuAt(this.adminUserPath, "100,0");

        assertEquals("Edit menu item should be visible", 
            1, this.selenium.getXpathCount(editPath));
        assertEquals("Disable menu item should not be visible", 
            0, this.selenium.getXpathCount(disablePath));
        assertEquals("Delete menu item should not be visible", 
            0, this.selenium.getXpathCount(deletePath));
        
        //right click on someone else, should get full menu
        //right click on self, should get truncated menu
        this.selenium.contextMenuAt(this.webUserPath, "0,0");

        assertEquals("Edit menu item should be visible", 
            1, this.selenium.getXpathCount(editPath));
        assertEquals("Disable menu item should be visible", 
            1, this.selenium.getXpathCount(disablePath));
        assertEquals("Delete menu item should be visible", 
            1, this.selenium.getXpathCount(deletePath));

        //click else where to close context menu
        this.selenium.clickAt(this.adminUserPath, "100,0");
        
        //test multi delete
        this.selenium.controlKeyDown();
        this.selenium.click(this.adminUserPath);
        this.selenium.click(this.webUserPath);
        this.selenium.controlKeyUp();
        
        String deleteIconPath = "//table[@id='admin-delete-button']";
        
        this.selenium.click(deleteIconPath);
        
        //confirmation dialog gets added to the bottom of body tag
        //context menu get generated and inserted to the bottom of the body tag
        String userListPath = "//body/div[last()]//ul[@class='confirm'][li='%s']";
        assertEquals("Admin user should not be on the to-be-deleted-users list", 
            0, this.selenium.getXpathCount(String.format(userListPath, this.adminUser.getUsername())));
        assertEquals("Web user should be on the to-be-deleted-users list", 
            1, this.selenium.getXpathCount(String.format(userListPath, this.webUser.getUsername())));
        
        //make sure there's no deleted text
        assertEquals("'Deleted' should not be visible", 0, this.selenium.getXpathCount("//div[text()='Deleted']"));

        this.selenium.click("//body/div[last()]//em[button='OK']");
        
        //wait till user is deleted
        new Waiter(60) {
            public boolean condition() {
                return !selenium.isVisible(webUserPath);
            }
        }.doWait();
        
        //make sure admin user is not deleted
        assertEquals("Admin user should not be deleted", 1, this.selenium.getXpathCount(this.adminUserPath));
        //make sure web user is deleted
        assertEquals("Web user should be deleted", 0, this.selenium.getXpathCount(this.webUserPath));
        
    }
}
