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

import com.solers.delivery.daos.UserDAO;
import com.solers.delivery.domain.User;
import com.solers.util.dao.GenericHibernateDAO;
import com.solers.util.dao.ValidationException;

/**
 * Created by IntelliJ IDEA.
 * User: mhasan
 * Date: Mar 22, 2007
 * Time: 1:29:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class UserDAOHibernate extends GenericHibernateDAO<User, Long> implements UserDAO {
    
    public User getUserByUsername(String username) {
        Query q = getSession().getNamedQuery(UserDAO.GET_USER_BY_USERNAME);
        q.setParameter("username", username);
        return (User) q.uniqueResult();
    }
    
    @Override
    protected void validate(User user) {
        ValidationException result = null;
        
        try {
            super.validate(user);
        } catch (ValidationException ex) {
            result = ex;
        }
        
        User existing = getUserByUsername(user.getUsername());
        if (existing != null ) {
            if (!equals(user.getId(), existing.getId())) {
                if (result == null) {
                    result = new ValidationException();
                }
                result.addMessage("A User with the given username already exists");
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
