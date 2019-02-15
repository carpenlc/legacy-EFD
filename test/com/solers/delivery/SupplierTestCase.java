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

public class SupplierTestCase extends BaseSeleniumTestCase {
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.helper.login();
    }
    
    @After
    public void tearDown() throws Exception {
        this.helper.logout();
        super.tearDown();
    }
    
    /**
     * Apps cannot load bean 'inventoryBundler'.
     * Workaround for now is to trigger another error instead of actually saving a supplier and 
     * check the error messages.
     * TODO figure out why we cannot load inventoryBundler bean in test env.
     */
    @Test
    public void testErrorMessageForSupplierNameThatIsTooLong() throws TimeoutException, InterruptedException {
        this.selenium.click("//table[@id='new-cs-button']");
        //menu gets added as the last div
        this.selenium.click("//a[text()='New Supplier']");
        
        String maxName = "12345678901234567890123456789012345678901234567890123456789012345678901234567890";
        
        //make sure no error dialog
        assertEquals("Shouldn't be any errors", 0, this.selenium.getXpathCount("//ul[@class='error']"));
        //type in correct length name
        this.selenium.type("name", maxName);
        //this should trigger an error since we cannot use path within efd.home        
        this.selenium.type("path", System.getProperty("efd.home"));
        //click save
        this.selenium.click("//button[text()='Save']");
        
        //expecting error dialog with 'OK' button
        new Waiter(1000) {
            public boolean condition() {
                return selenium.isVisible("//button[text()='OK']");
            }
        }.doWait();

        //should be an error
        assertEquals("There should be an error dialog", 1, this.selenium.getXpathCount("//ul[@class='error']"));
        //should not have a name length error
        assertEquals("There should not be an error on the supplier name", 0, this.selenium.getXpathCount("//ul[@class='error']/li[text()='Name can only have a maximum of 80 characters']"));

        //type in long name > 80
        this.selenium.type("name", maxName + "a");
        //click save
        this.selenium.click("//button[text()='Save']");

        //expecting error dialog with 'OK' button
        new Waiter(1000) {
            public boolean condition() {
                return selenium.isVisible("//button[text()='OK']");
            }
        }.doWait();

        assertEquals("There should be an error dialog", 1, this.selenium.getXpathCount("//ul[@class='error']"));
        //should not have a conversion error
        assertEquals("There should be an error on the supplier name", 0, this.selenium.getXpathCount("//ul[@class='error']/li[text()='Conversion error. HTTP Response Body: EMPTY']"));
        //should have a name length error
        assertEquals("There should be an error on the supplier name", 1, this.selenium.getXpathCount("//ul[@class='error']/li[text()='Name can only have a maximum of 80 characters']"));
        
        //click ok
        this.selenium.click("//button[text()='OK']");

        //expecting error dialog to go away
        new Waiter(1000) {
            public boolean condition() {
                return !selenium.isVisible("//button[text()='OK']");
            }
        }.doWait();
    }
}
