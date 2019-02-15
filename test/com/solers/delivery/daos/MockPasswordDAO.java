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

import java.util.ArrayList;
import java.util.List;

import com.solers.delivery.domain.Password;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class MockPasswordDAO extends MockGenericDAO<Password, Long> implements PasswordDAO {

    @Override
    public void deleteAll(Long id) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void deleteOldest(Long id) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public List<Password> getPasswords(Long id) {
        List<Password> result = new ArrayList<Password>();
        for (Password p : data.values()) {
            if (p.getUser().getId() == id) {
                result.add(p);
            }
        }
        return result;
    }

    @Override
    public Password makePersistent(Password entity) {
        if (entity.getId() == 0) {
            entity.setId(System.nanoTime());
        }
        data.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public void makeTransient(Password entity) {
        data.remove(entity.getId());
    } 

}
