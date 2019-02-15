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
package com.solers.delivery.web.export;

import java.util.Iterator;
import java.util.List;

import com.solers.delivery.domain.ContentSet;
import com.solers.delivery.reports.history.Synchronization;

public class SynchronizationExportData implements ExportData<Synchronization> {
    
    private final boolean isSupplier;
    private final List<Synchronization> data;
    
    public SynchronizationExportData(ContentSet contentSet, List<Synchronization> data) {
        this(contentSet.isSupplier(), data);
    }
    
    public SynchronizationExportData(boolean isSupplier, List<Synchronization> data) {
        this.isSupplier = isSupplier;
        this.data = data;
    }
    
    public String getHeader() {
        if (isSupplier) {
            return String.format("%s,%s,%s,%s,%s,%s,%s\n", "Date", "Duration", "Host", "Items Supplied", "Bytes Supplied", "Items Failed", "Bytes Failed");
        }
        return String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n", "Date", "Duration", "Items Added", "Items Updated", "Items Deleted", "Bytes Added", "Bytes Updated", "Bytes Deleted", "Items Failed", "Bytes Failed");
    }

    public String getRow(Synchronization row) {
        if (isSupplier) {
            return String.format("%s,%s,%s,%s,%s,%s,%s\n", row.getTimestamp(), row.getElapsedTime(), row.getHost(), row.getAdds(), row.getBytesAdded(), row.getFailures(), row.getBytesFailed());
        }
        return String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n", row.getTimestamp(), row.getElapsedTime(), row.getAdds(), row.getUpdates(), row.getDeletes(), row.getBytesAdded(), row.getBytesUpdated(), row.getBytesDeleted(), row.getFailures(), row.getBytesFailed());
    }

    public Iterator<Synchronization> iterator() {
        return data.iterator();
    }
    
    public String getExportFileName() {
        return String.format("synchronizations-%s.csv", System.currentTimeMillis());
    }
}