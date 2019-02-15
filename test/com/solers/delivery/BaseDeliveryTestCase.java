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

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;

import com.solers.delivery.content.AllowedHostService;
import com.solers.delivery.content.ContentService;
import com.solers.delivery.event.EventManager;
import com.solers.delivery.user.PasswordService;
import com.solers.delivery.user.UserService;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public abstract class BaseDeliveryTestCase extends TestCase {
    
    protected Start core;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        
        core = new Start();
        core.init(null);
        core.start();
    }

    @Override
    public void tearDown() throws Exception {
        core.getContext().close();
        core.getParentContext().close();
        try {
            File file = new File(System.getProperty("database.dir"));
            FileUtils.deleteDirectory(file);
        } catch (IOException ex) {
            System.err.println("Could not delete database directory");
            ex.printStackTrace();
        }
    }
    
    protected ContentService getContentService() {
        return (ContentService) core.getContext().getBean("contentService");
    }
    
    protected EventManager getEventManager() {
        return (EventManager) core.getContext().getBean("eventManager");
    }
    
    protected AllowedHostService getAllowedHostService() {
        return (AllowedHostService) core.getContext().getBean("allowedHostService");
    }
    
    protected UserService getUserService() {
        return (UserService) core.getContext().getBean("userService");
    }
    
    protected PasswordService getPasswordService() {
        return (PasswordService) core.getContext().getBean("passwordService");
    }
}
