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

import com.solers.delivery.domain.AllowedHost;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class MockAllowedHostDAO extends MockGenericDAO<AllowedHost, Long> implements AllowedHostDAO {

    @Override
    public AllowedHost getByAlias(String alias) {
        for (AllowedHost a : data.values()) {
            if (a.getAlias().equals(alias)) {
                return a;
            }
        }
        return null;
    }

    @Override
    public AllowedHost makePersistent(AllowedHost entity) {
        if (throwOnSave) {
            throwValidationException();
        }
        if (entity.getId() == null || entity.getId() == 0) {
            entity.setId(System.currentTimeMillis());
        }
        data.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public void makeTransient(AllowedHost entity) {
        data.remove(entity.getId());
    }

}
