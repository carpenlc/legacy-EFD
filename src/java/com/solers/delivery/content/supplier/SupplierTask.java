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
package com.solers.delivery.content.supplier;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Qualifier;

import com.solers.delivery.content.status.CurrentSynchronization;
import com.solers.delivery.domain.ContentSet;
import com.solers.delivery.management.SupplierStatus;
import com.solers.delivery.transport.http.monitor.TransferMonitor;
import com.solers.jmx.registry.AutoRegister;
import com.solers.jmx.registry.AutoRegisterObjectName;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
@AutoRegister(category="Suppliers")
@Configurable
public class SupplierTask implements SupplierStatus, Runnable {
    
    private final InventoryCreationTask task;
    private final ContentSet contentSet;
    
    private TransferMonitor transferMonitor;
    
    public SupplierTask(ContentSet contentSet) {
        this.task = new InventoryCreationTask(contentSet);
        this.contentSet = contentSet;
    }
    
    @Autowired
    public void setTransferMonitor(@Qualifier("transferMonitor")TransferMonitor transferMonitor) {
        this.transferMonitor = transferMonitor;
    }

    @AutoRegisterObjectName
    public Long getContentSetId() {
        return contentSet.getId();
    }
    
    @Override
    public String toString() {
        return "SupplierTask for ContentSet: "+contentSet;
    }
    
    @Override
    public void run() {
        task.run();
    }

    @Override
    public List<CurrentSynchronization> getCurrentSynchronizations() {
        return transferMonitor.getCurrentConsumers(contentSet.getId());
    }
    
    @Override
    public boolean isEnabled() {
        return contentSet.isEnabled();
    }

    @Override
    public long getItemCount() {
        return task.getItemCount();
    }

    @Override
    public long getLastRuntime() {
        return task.getLastRuntime();
    }

    @Override
    public long getNextEstimatedRuntime() {
        return task.getNextEstimatedRuntime();
    }

    @Override
    public long getSize() {
        return task.getSize();
    }

    @Override
    public String getState() {
        return task.getState();
    }
}
