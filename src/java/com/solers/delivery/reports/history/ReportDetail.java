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
package com.solers.delivery.reports.history;

import java.util.Date;

import com.solers.delivery.event.ContentEvent.ContentEventResult;
import com.solers.lucene.Converter;
import com.solers.lucene.LuceneField;

public class ReportDetail {
    
    private String path;
    private String action;
    private String status;
    private Date searchTime;
    private long timestamp;
    private long size;
    private long transferred;

    public String getPath() {
        return path;
    }

    @LuceneField
    public void setPath(String path) {
        this.path = path;
    }

    public long getSize() {
        return size;
    }

    @LuceneField(name="bytesRequested", converter=Converter.LONG)
    public void setSize(long size) {
        this.size = size;
        setFailed(getSize() - getTransferred());
        setSuccessful(getSize() - getFailed());
    }
    
    public long getTransferred() {
        return this.transferred;
    }
    
    @LuceneField(name="bytesManipulated", converter=Converter.LONG)
    public void setTransferred(long transferred) {
        this.transferred = transferred;
        setFailed(getSize() - getTransferred());
    }
    
    public long getSuccessful() {
        if (status != null && getStatusValue().isComplete())
            return getSize() - getFailed();
        return 0L;
    }
    
    // No-op so Xstream will treat this as a javabean property
    public void setSuccessful(long bytes) {
        
    }
    
    public long getFailed() {
        if (status != null && getStatusValue().isComplete())
            return getSize() - getTransferred();
        return 0L;
    }
    
    // No-op so Xstream will treat this as a javabean property
    public void setFailed(long bytes) {
        
    }

    public String getAction() {
        return action;
    }
    
    @LuceneField
    public void setAction(String action) {
        this.action = action;
    }
    public String getStatus() {
        return status;
    }

    @LuceneField(name="result")
    public void setStatus(String status) {
        this.status = status;
    }
    
    public ContentEventResult getStatusValue() {
        return ContentEventResult.hasLabel(getStatus());
    }

    public String toString() {
        return (action + "," + status + "," + path + "," + size);
    }

    public long getTimestamp() {
        if (timestamp == 0) {
            return searchTime == null ? -1L : searchTime.getTime();
        }
        return timestamp;
    }

    @LuceneField(name="time", converter=Converter.LONG)
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    @LuceneField(name="timeStamp", converter=Converter.DATE)
    public void setSearchTime(Date searchTime) {
        this.searchTime = searchTime;
    }
}
