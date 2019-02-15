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
import java.util.Collection;
import java.util.List;

import com.solers.delivery.domain.Alert;
import com.solers.delivery.domain.Alert.AlertType;
import com.solers.util.Page;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class MockAlertDAO extends MockGenericDAO<Alert, Long> implements AlertDAO {

    @Override
    public Page<Alert> listBy(AlertType type, int startIndex, int numRecords) {
        List<Alert> result = new ArrayList<Alert>();
        for (Alert a : data.values()) {
            if (a.getType() == type) {
                result.add(a);
            }
        }
        return new Page<Alert>(result.size(), result);
    }

    @Override
    public Alert makePersistent(Alert entity) {
        if (entity.getId() == null || entity.getId() == 0) {
            entity.setId(System.nanoTime());
        }
        data.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public void makeTransient(Alert entity) {
        data.remove(entity.getId());
    }

    @Override
    public void makeTransientById(Collection<Long> ids) {
        for(Long id : ids) {
            data.remove(id);
        }        
    }

}
