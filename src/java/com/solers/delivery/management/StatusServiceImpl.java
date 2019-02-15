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
package com.solers.delivery.management;

import java.util.List;

import com.solers.delivery.content.status.CurrentSynchronization;
import com.solers.delivery.transport.http.monitor.TransferMonitor;
import com.solers.jmx.registry.Registrar;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class StatusServiceImpl implements StatusService {
    
    private final Registrar registrar;
    private TransferMonitor monitor;
    
    public StatusServiceImpl(Registrar registrar) {
        this.registrar = registrar;
    }

    public void setTransferMonitor(TransferMonitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public ConsumerStatus getConsumerStatus(Long contentSetId) {
        return registrar.getManagedBean(ConsumerStatus.class, contentSetId);
    }

    @Override
    public SupplierStatus getSupplierStatus(Long contentSetId) {
        return registrar.getManagedBean(SupplierStatus.class, contentSetId);
    }

    @Override
    public List<CurrentSynchronization> getCurrentSynchronizations() {
        return monitor.getCurrentSynchronizations();
    }

    
}
