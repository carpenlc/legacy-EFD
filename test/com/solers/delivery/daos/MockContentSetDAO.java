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

import com.solers.delivery.domain.ContentSet;


public class MockContentSetDAO extends MockGenericDAO<ContentSet, Long> implements ContentSetDAO {

    Long ids = new Long(0);
    
    MockConsumerContentSetDAO consumerDAO;
    
    MockContentSetDAO(MockConsumerContentSetDAO consumerDAO) {
        this.consumerDAO = consumerDAO;
    }
    
    /**
     * In real life, the content set dao finds both contentset and
     * consumercontentsets
     */
    @Override
    public ContentSet findById(Long id, boolean lock) {
        ContentSet result = super.findById(id, lock);
        if (result == null) {
            result = consumerDAO.findById(id, lock);
        }
        return result;
    }

    @Override
    public List<ContentSet> getSupplierSets() {
        return findAll();
    }

    @Override
    public ContentSet makePersistent(ContentSet entity) {
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
    public void makeTransient(ContentSet entity) {
        data.remove(entity.getId());
    }
    
    @Override
    public ContentSet getSupplierByName(String name) {
        return null;
    }
}
