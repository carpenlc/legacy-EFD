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
package com.solers.delivery.daos.impl;

import java.io.Serializable;

import org.hibernate.SessionFactory;

import com.solers.delivery.daos.AlertDAO;
import com.solers.delivery.daos.AllowedHostDAO;
import com.solers.delivery.daos.ConsumerContentSetDAO;
import com.solers.delivery.daos.ContentSetDAO;
import com.solers.delivery.daos.DAOFactory;
import com.solers.delivery.daos.PasswordDAO;
import com.solers.delivery.daos.PendingDeleteDAO;
import com.solers.delivery.daos.UserDAO;
import com.solers.util.dao.GenericDAO;
import com.solers.util.dao.GenericHibernateDAO;

/**
 * Taken from: http://www.hibernate.org/328.html
 * 
 * HibernateDAOFactory creates all DAOs defined in the DAOFactory interface for the Hibernate framework. The get*DAO methods populate the returned DAO object
 * with an active Hibernate session so the returned DAO is ready to use.
 * 
 * @author JGimourginas
 */
public class HibernateDAOFactory implements DAOFactory {

    private SessionFactory sessionFactory = null;
    
    public <T, ID extends Serializable> GenericDAO<T, ID> getGenericDAO(Class<T> entityClass) {
        GenericHibernateDAO<T, ID> dao = new GenericHibernateDAO<T, ID>(entityClass);
        dao.setSession(sessionFactory.getCurrentSession());
        return dao;
    }

    public ContentSetDAO getContentSetDAO() {
        return (ContentSetDAO) instantiateDAO(ContentSetDAOHibernate.class);
    }

    public ConsumerContentSetDAO getConsumerContentSetDAO() {
        return (ConsumerContentSetDAO) instantiateDAO(ConsumerContentSetDAOHibernate.class);
    }

    public PendingDeleteDAO getPendingDeleteDAO() {
        return (PendingDeleteDAO) instantiateDAO(PendingDeleteDAOHibernate.class);
    }
    
    public UserDAO getUserDAO() {
        return (UserDAO) instantiateDAO(UserDAOHibernate.class);
    }
    
    public PasswordDAO getPasswordDAO() {
        return (PasswordDAO) instantiateDAO(PasswordDAOHibernate.class);
    }

    @Override
    public AllowedHostDAO getAllowedHostDAO() {
        return (AllowedHostDAO) instantiateDAO(AllowedHostDAOHibernate.class);
    }

    @Override
    public AlertDAO getAlertDAO() {
        return (AlertDAO) instantiateDAO(AlertDAOHibernate.class);
    }

    /**
     * Used to instantiate a DAO for any class for use with Hibernate. Returns a GenericHibernateDAO object, which can then be cast to the correct DAO type. The
     * DAO returned has its Session populated.
     * 
     * @param daoClass
     *            class for which a new instance should be created - must be an extension of GenericHibernateDAO
     * @return GenericHibernateDAO object with a populated Session member variable.
     */
    @SuppressWarnings("unchecked")
    private GenericHibernateDAO instantiateDAO(Class<?> daoClass) {
        try {
            GenericHibernateDAO dao = (GenericHibernateDAO) daoClass.newInstance();
            dao.setSession(sessionFactory.getCurrentSession());
            return dao;
        } catch (Exception ex) {
            throw new RuntimeException("Can not instantiate DAO: " + daoClass, ex);
        }
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

}
