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
package com.solers.delivery.reports.metrics;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.data.Response;
import org.restlet.data.Status;

import com.solers.delivery.content.ContentService;
import com.solers.delivery.domain.ContentSet;
import com.solers.delivery.reports.history.Synchronization;
import com.solers.delivery.reports.history.SynchronizationHistory;
import com.solers.delivery.rest.RestfulService;
import com.solers.delivery.rest.converter.HistoryConverter;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class MetricsTask implements Runnable {
    
    private static final Logger log = Logger.getLogger(MetricsTask.class);
    
    private final File dataDirectory;
    private final HistoryConverter converter;
    private final SynchronizationHistory history;
    private final ContentService contentService;
    private final RestfulService metricsService;
    private final boolean enabled;
    
    public MetricsTask(File dataDirectory, ContentService contentService, SynchronizationHistory history, RestfulService metricsService, boolean enabled) {
        this.dataDirectory = dataDirectory;
        this.contentService = contentService;
        this.history = history;
        this.metricsService = metricsService;
        this.enabled = enabled;
        this.converter = new HistoryConverter();
        
        if (!dataDirectory.exists()) {
            log.info(dataDirectory.getAbsolutePath()+" does not exist, creating..");
            dataDirectory.mkdirs();
        }
        
        log.info(enabled ? "Enabled" : "Disabled");
    }
    
    @Override
    public void run() {
        if (enabled) {
            try {
                log.info("Running");
                if (sendXml()) {
                    updateLastRuntime();
                }
                log.info("Complete");
            } catch (RuntimeException ex) {
                log.error("Unexpected error sending metrics", ex);
            }
        }
    }
    
    private boolean sendXml() {
        String xml = getXml();
        boolean result = true;
        if (xml != null) {
            Response response = metricsService.put(xml, MediaType.TEXT_XML, "data/");
            result = response.getStatus().equals(Status.SUCCESS_OK);
        }
        return result;
    }
    
    private void updateLastRuntime() {
        File file = getLastRuntimeFile();
        try {
            FileUtils.writeStringToFile(file, String.valueOf(System.currentTimeMillis()));
        } catch (IOException ex) {
            log.error("Error writing lastRuntime", ex);
        }
    }
    
    private String getXml() {
        Date lastRuntime = getLastRuntime();
        StringBuilder consumers = new StringBuilder();
        StringBuilder suppliers = new StringBuilder();
        int count = 0;
        for (ContentSet set : contentService.getContentSets()) {
            List<Synchronization> data = history.getSynchronizationsAfter(set.getId(), lastRuntime, false, SynchronizationHistory.PAGE_SIZE);
            count = count + data.size();
            if (!data.isEmpty()) {
                try {
                    String xml = converter.to(MediaType.TEXT_XML, data).getText();
                    if (set.isSupplier()) {
                        suppliers.append(xml);
                    } else {
                        consumers.append(xml);
                    }
                } catch (IOException ex) {
                    log.error("Error converting history to XML", ex);
                }
            }
        }
        if (consumers.length() == 0 && suppliers.length() == 0) {
            log.info("No synchronizations to send");
            return null;
        }
        
        StringBuilder buffer = new StringBuilder("<metrics>");
        if (consumers.length() > 0) {
            buffer.append("<consumer>").append(consumers).append("</consumer>");
        }
        if (suppliers.length() > 0) {
            buffer.append("<supplier>").append(suppliers).append("</supplier>");
        }
        
        log.info("Sending "+count+" synchronizations");
        return buffer.append("</metrics>").toString();
    }
    
    private Date getLastRuntime() {
        File file = getLastRuntimeFile();
        Date result = null;
        if (file.exists()) {
            try {
                String time = FileUtils.readFileToString(file);
                result = new Date(Long.parseLong(time));
            } catch (IOException ex) {
                log.error("Error reading lastRuntime", ex);
            } catch (NumberFormatException ex) {
                log.error("Error reading lastRuntime", ex);
            }
        }
        return result;
    }
    
    private File getLastRuntimeFile() {
        return new File(dataDirectory, "lastRuntime");
    }
}
