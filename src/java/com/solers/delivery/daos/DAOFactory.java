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




/**
 * Taken from: http://www.hibernate.org/328.html
 *
 * Creates a standalone DAOFactory that returns unmanaged DAO beans for use in any environment Hibernate has been
 * configured for. Uses SessionFactory and Hibernate context propagation (CurrentSessionContext),
 * thread-bound or transaction-bound, and transaction scoped.
 *
 * @author JGimourginas
 */
public interface DAOFactory {

    ContentSetDAO getContentSetDAO();

    ConsumerContentSetDAO getConsumerContentSetDAO();

    PendingDeleteDAO getPendingDeleteDAO();
    
    UserDAO getUserDAO();
    
    PasswordDAO getPasswordDAO();
    
    AllowedHostDAO getAllowedHostDAO();
    
    AlertDAO getAlertDAO();
}
