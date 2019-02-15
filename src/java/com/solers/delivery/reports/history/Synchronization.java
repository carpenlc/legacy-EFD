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

import com.solers.lucene.Converter;
import com.solers.lucene.LuceneField;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class Synchronization {
    
    private String id;
    private long elapsedTime;
    private long bytesAdded;
    private long bytesUpdated;
    private long bytesDeleted;
    private long bytesFailed;
    private long adds;
    private long updates;
    private long deletes;
    private long failures;
    private long timestamp;
    private Date searchTime;
    private Date endDate;
    private String host;
    
    public long getBytesFailed() {
        return bytesFailed;
    }

    @LuceneField(converter=Converter.LONG)
    public void setBytesFailed(long bytesFailed) {
        this.bytesFailed = bytesFailed;
    }

    public long getFailures() {
        return failures;
    }

    @LuceneField(converter=Converter.LONG)
    public void setFailures(long failures) {
        this.failures = failures;
    }

    public String getHost() {
        return host;
    }
    
    @LuceneField
    public void setHost(String host) {
        this.host = host;
    }
    
    /**
     * @return the id
     */
    public String getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    @LuceneField
    public void setId(String id) {
        this.id = id;
    }
    /**
     * @return the elapsedTime
     */
    public long getElapsedTime() {
        return elapsedTime;
    }
    /**
     * @param elapsedTime the elapsedTime to set
     */
    @LuceneField(converter=Converter.LONG)
    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }
    /**
     * @return the bytesAdded
     */
    public long getBytesAdded() {
        return bytesAdded;
    }
    /**
     * @param bytesAdded the bytesAdded to set
     */
    @LuceneField(converter=Converter.LONG)
    public void setBytesAdded(long bytesAdded) {
        this.bytesAdded = bytesAdded;
    }
    /**
     * @return the bytesUpdated
     */
    public long getBytesUpdated() {
        return bytesUpdated;
    }
    /**
     * @param bytesUpdated the bytesUpdated to set
     */
    @LuceneField(converter=Converter.LONG)
    public void setBytesUpdated(long bytesUpdated) {
        this.bytesUpdated = bytesUpdated;
    }
    /**
     * @return the bytesDeleted
     */
    public long getBytesDeleted() {
        return bytesDeleted;
    }
    /**
     * @param bytesDeleted the bytesDeleted to set
     */
    @LuceneField(converter=Converter.LONG)
    public void setBytesDeleted(long bytesDeleted) {
        this.bytesDeleted = bytesDeleted;
    }
    /**
     * @return the adds
     */
    public long getAdds() {
        return adds;
    }
    /**
     * @param adds the adds to set
     */
    @LuceneField(name="filesAdded", converter=Converter.LONG)
    public void setAdds(long adds) {
        this.adds = adds;
    }
    /**
     * @return the updates
     */
    public long getUpdates() {
        return updates;
    }
    /**
     * @param updates the updates to set
     */
    @LuceneField(name="filesUpdated", converter=Converter.LONG)
    public void setUpdates(long updates) {
        this.updates = updates;
    }
    /**
     * @return the deletes
     */
    public long getDeletes() {
        return deletes;
    }
    /**
     * @param deletes the deletes to set
     */
    @LuceneField(name="filesDeleted", converter=Converter.LONG)
    public void setDeletes(long deletes) {
        this.deletes = deletes;
    }
    
    public Date getEndDate() {
        return endDate;
    }
    
    @LuceneField(name="endDate", converter=Converter.DATE)
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
    
    public long getEndTime() {
        Date date = getEndDate();
        return date == null ? -1 : date.getTime();
    }
    
    public void setEndTime(long time) {
        if (time > 0) {
            setEndDate(new Date(time));
        }
    }
    
    public long getTimestamp() {
        if (timestamp == 0) {
            return searchTime == null ? -1L : searchTime.getTime();
        }
        return timestamp;
    }

    @LuceneField(name="time", converter=Converter.LONG)
    public void setTimestamp(long time) {
        this.timestamp = time;
    }
    
    @LuceneField(name="timeStamp", converter=Converter.DATE)
    public void setSearchTime(Date searchTime) {
        this.searchTime = searchTime;
    }
}
