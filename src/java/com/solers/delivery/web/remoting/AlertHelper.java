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
package com.solers.delivery.web.remoting;

import java.util.List;

import com.solers.delivery.alerts.AlertService;
import com.solers.delivery.domain.Alert;
import com.solers.delivery.domain.Alert.AlertType;
import com.solers.util.Page;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class AlertHelper {

    private final AlertService service;

    public AlertHelper(AlertService service) {
        this.service = service;
    }

    public Page<Alert> getUserAlerts(int start, int numRecords) {
        return service.list(AlertType.USER, start, numRecords);
    }

    public Page<Alert> getAdminAlerts(int start, int numRecords) {
        return service.list(AlertType.ADMIN, start, numRecords);
    }

    public void markRead(Long id) {
        Alert alert = service.get(id);
        alert.setUnread(false);
        service.save(alert);
    }

    public void remove(List<Long> ids) {
        service.remove(ids);
    }

}
