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
import com.solers.delivery.reports.history.ReportDetail;
import com.solers.util.unit.FileSizeUnit;

public class SynchronizationDetailsExportData implements ExportData<ReportDetail> {
    
    private final boolean isSupplier;
    private final List<ReportDetail> data;
    
    public SynchronizationDetailsExportData(ContentSet contentSet, List<ReportDetail> data) {
        this(contentSet.isSupplier(), data);
    }
    
    public SynchronizationDetailsExportData(boolean isSupplier, List<ReportDetail> data) {
        this.isSupplier = isSupplier;
        this.data = data;
    }
    
    public String getHeader() {
        if (isSupplier) {
            return String.format("%s,%s,%s,%s,%s,%s\n", "Date", "Path", "Size", "Successful", "Failed", "Result");
        }
        return String.format("%s,%s,%s,%s,%s,%s,%s\n", "Date", "Action", "Path", "Size", "Successful", "Failed", "Result");
    }

    public String getRow(ReportDetail row) {
        if (isSupplier) {
            return String.format("%s,%s,%s,%s,%s,%s\n", row.getTimestamp(), row.getPath(), row.getSize(), FileSizeUnit.format(row.getSuccessful()), FileSizeUnit.format(row.getFailed()), row.getStatus());
        }
        return String.format("%s,%s,%s,%s,%s,%s,%s\n", row.getTimestamp(), row.getAction(), row.getPath(), row.getSize(), FileSizeUnit.format(row.getSuccessful()), FileSizeUnit.format(row.getFailed()), row.getStatus());
    }

    public Iterator<ReportDetail> iterator() {
        return data.iterator();
    }
    
    public String getExportFileName() {
        return String.format("sychronization-details-%s.csv", System.currentTimeMillis());
    }
}