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

import com.thoughtworks.selenium.Selenium;

public class BaseSeleniumTestCase extends BaseWebTestCase {
    protected SeleniumHelper helper;
    protected Selenium selenium;
    
    @Before
    public void setUp() throws Exception {
        helper = new SeleniumHelper();

        // Let SeleniumHelper create any necessary resources. One required resource is the webapp workdir which
        // is expected by the Jetty spring configuration for StartWeb which is initialized in super.setUp();
        helper.init();

        super.setUp();
        
        //Once the necessary services are initialized in the superclass, share them with SeleniumHelper
        helper.initializeServices(super.getUserService(), super.getPasswordService(), super.getAllowedHostService());
        
        //Now that the services are available and all the spring contexts have been created in the superclass,
        //start SeleniumHelper (initializing the connection to the Selenium Server instance) 
        helper.start();

        this.selenium = helper.getSelenium();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        helper.stop();
    }

}
