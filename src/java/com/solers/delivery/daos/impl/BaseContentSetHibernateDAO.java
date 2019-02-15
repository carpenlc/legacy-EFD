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

import org.hibernate.Query;

import com.solers.delivery.domain.ContentSet;
import com.solers.util.dao.GenericHibernateDAO;
import com.solers.util.dao.ValidationException;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class BaseContentSetHibernateDAO<T extends ContentSet, ID extends Serializable> extends GenericHibernateDAO<T, ID> {
    
    @Override
    protected void validate(T contentSet) {
        ValidationException result = null;
        
        try {
            super.validate(contentSet);
        } catch (ValidationException ex) {
            result = ex;
        }
        
        ContentSet existing = byName(contentSet.getName());
        if (existing != null ) {
            if (!equals(contentSet.getId(), existing.getId())) {
                if (result == null) {
                    result = new ValidationException();
                }
                result.addMessage("A Content Set with the given name already exists");
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
    
    private ContentSet byName(String name) {
        Query q = getSession().createQuery("from ContentSet where name = :name");
        q.setParameter("name", name);
        return (ContentSet) q.uniqueResult();
    }
    
}
