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

import java.util.List;

import com.solers.delivery.domain.ConsumerContentSet;

public class MockConsumerContentSetDAO extends MockGenericDAO<ConsumerContentSet, Long> implements ConsumerContentSetDAO {
    
    Long ids = new Long(0);
    
    @Override
    public List<ConsumerContentSet> getConsumerSets() {
        return findAll();
    }

    @Override
    public ConsumerContentSet makePersistent(ConsumerContentSet entity) {
        if (throwOnSave) {
            throwValidationException();
        }
        if (entity.getId() == null) {
            entity.setId(++ids);
        }
        data.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public void makeTransient(ConsumerContentSet entity) {
        data.remove(entity.getId());
    }
    
    @Override
    public ConsumerContentSet getConsumerByName(String name) {
        return null;
    }
}
