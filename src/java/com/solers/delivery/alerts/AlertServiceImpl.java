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
package com.solers.delivery.alerts;

import java.util.Collection;

import org.springframework.transaction.annotation.Transactional;

import com.solers.delivery.daos.AlertDAO;
import com.solers.delivery.daos.DAOFactory;
import com.solers.delivery.domain.Alert;
import com.solers.delivery.domain.Alert.AlertType;
import com.solers.util.Page;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class AlertServiceImpl implements AlertService {

    private final DAOFactory factory;
    
    public AlertServiceImpl(DAOFactory factory) {
        this.factory = factory;
    }

    @Override
    @Transactional
    public Page<Alert> list(AlertType type, int startIndex, int numRecords) {
        AlertDAO dao = factory.getAlertDAO();
        return dao.listBy(type, startIndex, numRecords);
    }

    @Override
    @Transactional
    public void remove(Long id) {
        AlertDAO dao = factory.getAlertDAO();
        Alert alert = dao.findById(id, false);
        dao.makeTransient(alert);
    }

    @Override
    @Transactional
    public void save(Alert alert) {
        AlertDAO dao = factory.getAlertDAO();
        dao.makePersistent(alert);
    }

    @Override
    @Transactional
    public Alert get(Long id) {
        AlertDAO dao = factory.getAlertDAO();
        return dao.getById(id);
    }

    @Override
    @Transactional
    public void remove(Collection<Long> ids) {
        AlertDAO dao = factory.getAlertDAO();
        dao.makeTransientById(ids);
    }
}
