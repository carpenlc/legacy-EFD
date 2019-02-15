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

import com.solers.delivery.domain.User;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class MockUserDAO extends MockGenericDAO<User, Long> implements UserDAO {
    
    @Override
    public User getUserByUsername(String username) {
        for (User u : data.values()) {
            if (u.getUsername().equals(username)) {
                return u;
            }
        }
        return null;
    }

    @Override
    public User makePersistent(User entity) {
        if (throwOnSave) {
            throwValidationException();
        }
        if (entity.getId() == null) {
            entity.setId(System.currentTimeMillis());
        }
        data.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public void makeTransient(User entity) {
        data.remove(entity.getId());
    }
    
}
