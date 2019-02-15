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

import org.apache.log4j.Logger;
import org.junit.Test;

public class WebLoginTestCase extends BaseSeleniumTestCase {

    private static final Logger log = Logger.getLogger(WebLoginTestCase.class);

    @Test
    public void testLoginAndLogout() throws Exception {
        this.selenium.open("/");
        new Waiter(60) { 
            public boolean condition() {
                return selenium.isVisible("username");
            }
        }.doWait();

        this.selenium.type("username", this.helper.getTestUsername());
        //invalid password
        this.selenium.type("password", "foobar");
        this.selenium.click("//table[@id='login-button']");

        //wait for error dialog
        new Waiter(60) {
            public boolean condition() {
                return selenium.isVisible("ul[@class='error']");
            }
        }.doWait();
        
        assertEquals("Error message should say 'Incorrect username and password", 1,
            this.selenium.getXpathCount("//ul[@class='error']/li[text()='Incorrect username and password']"));

        //click ok
        this.selenium.click("//button[text()='OK']");

        //now try regular login
        if (log.isTraceEnabled()) 
            log.trace(String.format("Logging in as user: %s with password: %s", helper.getTestUser().getUsername(), helper.getTestUserPassword()));
        
        helper.login();

        
        if (log.isTraceEnabled()) 
            log.trace(String.format("Logging out user: %s", helper.getTestUser().getUsername()));
        
        helper.logout();
        
        if (log.isTraceEnabled()) 
            log.trace("Logged out");

    }

}
