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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import com.solers.delivery.reports.history.ReportDetail;
import com.solers.delivery.reports.history.Synchronization;
import com.solers.delivery.reports.history.SynchronizationHistory;
import com.solers.util.Page;
import com.solers.util.PaginatedList;

/**
 * <p>Ajax helper class for the history pages</p>
 * 
 * <p>This class is not thread safe</p>
 * 
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class SynchronizationHistoryHelper {
    
    private static final Logger log = Logger.getLogger(SynchronizationHistoryHelper.class);
    
    private final SynchronizationHistory history;
    private final long lookupTimeout;
    private final Map<String, String> lookupCache;

    /**
     * @param lookupTimeout Timeout for hostname lookup. Specified in milliseconds
     * @param hostsTTL Cache time for content set host entries.  Specified in milliseconds 
     */
    public SynchronizationHistoryHelper(SynchronizationHistory history, long lookupTimeout, long hostsTTL) {
        this.history = history;
        this.lookupTimeout = lookupTimeout;
        this.lookupCache = new HashMap<String, String>();
    }
    
    /**
     * <p>Resolve the hostname of {@code ip}</p>
     * 
     * <p>If {@code ip} cannot be resolved, {@code ip} is returned</p>
     * 
     * <p>Resolved hostnames are cached for the duration of
     * this object (which should be tied to the users session)</p>
     * 
     * <p>Since this is designed to be called via AJAX, lookups timeout after 
     * {@code lookupTimeout} milliseconds so that response time does not suffer</p>
     * 
     * @param ip
     * @return Resolved hostname of {@code ip} or {@code ip} if
     * no resolution is possible.
     */
    public String resolve(String ip) {
        String result = null;
        
        if (lookupCache.containsKey(ip)) {
            result = lookupCache.get(ip);
        } else {
            result = lookup(ip);
            lookupCache.put(ip, result);
        }
        
        return result;
    }
    
    protected String lookup(final String ip) {
        ExecutorService exec = Executors.newSingleThreadExecutor();
        String result = ip;
        Callable<String> callable = new Callable<String>() {
            public String call() throws UnknownHostException {
                return InetAddress.getByName(ip).getHostName();
            }
        };
        FutureTask<String> future = new FutureTask<String>(callable);
        exec.submit(future);
        
        try {
            result = future.get(lookupTimeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            log.warn("Timed out resolving: "+ip);
        } catch (InterruptedException ex) {
            log.error("Interrupted");
            Thread.currentThread().interrupt();
        } catch (ExecutionException ex) {
            log.error("Error resolving: "+ip, ex);
        }
        return result;
    }
    
    public Page<Synchronization> getSynchronizations(Long contentSetId, Date startTime, Date endTime, boolean showEmptyRecords, int startIndex, int pageSize) {
        PaginatedList<Synchronization> synchronizations = history.getSynchronizations(contentSetId, startTime, endTime, showEmptyRecords, pageSize);
        return new Page<Synchronization>(synchronizations.getSize(), synchronizations.subList(startIndex));
    }
    
    public Page<ReportDetail> getSynchronizationDetails(Long contentSetId, String synchronizationId, String action, String path, int startIndex, int pageSize) {
        PaginatedList<ReportDetail> data = history.getSynchronizationDetails(contentSetId, synchronizationId, action, path, pageSize);
        return new Page<ReportDetail>(data.getSize(), data.subList(startIndex));
    }
}
