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
package com.solers.delivery.daos;


public class MockDAOFactory implements DAOFactory {
    
    MockConsumerContentSetDAO consumerDao = new MockConsumerContentSetDAO();
    MockContentSetDAO supplierDao = new MockContentSetDAO(consumerDao);
    MockPasswordDAO passwordDao = new MockPasswordDAO();
    MockUserDAO userDao = new MockUserDAO();
    MockAllowedHostDAO allowedHostDao = new MockAllowedHostDAO();
    MockAlertDAO alertDao = new MockAlertDAO();
    
    @Override
    public MockConsumerContentSetDAO getConsumerContentSetDAO() {
        return consumerDao;
    }

    @Override
    public MockContentSetDAO getContentSetDAO() {
        return supplierDao;
    }

    @Override
    public PendingDeleteDAO getPendingDeleteDAO() {
        return null;
    }

    @Override
    public MockUserDAO getUserDAO() {
        return userDao;
    }

    @Override
    public PasswordDAO getPasswordDAO() {
        return passwordDao;
    }

    @Override
    public MockAllowedHostDAO getAllowedHostDAO() {
        return allowedHostDao;
    }

    @Override
    public AlertDAO getAlertDAO() {
        return alertDao;
    }

}
