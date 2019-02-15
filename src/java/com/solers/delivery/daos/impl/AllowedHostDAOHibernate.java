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

import org.hibernate.Query;

import com.solers.delivery.daos.AllowedHostDAO;
import com.solers.delivery.domain.AllowedHost;
import com.solers.util.dao.GenericHibernateDAO;
import com.solers.util.dao.ValidationException;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class AllowedHostDAOHibernate extends GenericHibernateDAO<AllowedHost, Long> implements AllowedHostDAO {

    @Override
    public AllowedHost getByAlias(String alias) {
        Query q = getSession().getNamedQuery(AllowedHostDAO.GET_BY_ALIAS);
        q.setParameter("alias", alias);
        return (AllowedHost) q.uniqueResult();
    }
    
    @Override
    protected void validate(AllowedHost allowedHost) {
        ValidationException result = null;
        
        try {
            super.validate(allowedHost);
        } catch (ValidationException ex) {
            result = ex;
        }
        
        AllowedHost existing = getByAlias(allowedHost.getAlias());
        if (existing != null ) {
            if (!equals(allowedHost.getId(), existing.getId())) {
                if (result == null) {
                    result = new ValidationException();
                }
                result.addMessage("An Allowed host with the given alias already exists");
            }
            getSession().evict(existing);
        }
      
        if (result != null) {
            throw result;
        }
    }
    
    private boolean equals(Long one, Long two) {
        return one != null && two != null && one.equals(two);
    }
}
