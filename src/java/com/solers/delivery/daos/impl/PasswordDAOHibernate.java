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

import java.util.List;

import org.hibernate.Query;

import com.solers.delivery.daos.PasswordDAO;
import com.solers.delivery.domain.Password;
import com.solers.util.dao.GenericHibernateDAO;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class PasswordDAOHibernate extends GenericHibernateDAO<Password, Long> implements PasswordDAO {
    
    @Override
    @SuppressWarnings("unchecked")
    public List<Password> getPasswords(Long id) {
        List<Password> retVal = null;
        Query q = getSession().getNamedQuery(PasswordDAO.GET_PASSWORDS);
        q.setParameter("id", id);
        retVal = (List<Password>) q.list();
        return retVal;
    }

    @Override
    public void deleteOldest(Long id) {
        Query q = getSession().getNamedQuery(PasswordDAO.DELETE_OLDEST);
        q.setParameter("id", id);
        q.executeUpdate();
    }

    @Override
    public void deleteAll(Long id) {
        Query q = getSession().getNamedQuery(PasswordDAO.DELETE_ALL);
        q.setParameter("id", id);
        q.executeUpdate();
    }
    
}
