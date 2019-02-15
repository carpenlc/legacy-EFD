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
package com.solers.delivery.rest.reports;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.Variant;

import com.solers.delivery.content.ContentService;
import com.solers.delivery.domain.ContentSet;
import com.solers.delivery.reports.history.SynchronizationHistory;
import com.solers.delivery.rest.Utils;
import com.solers.delivery.rest.converter.HistoryConverter;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class HistoryResource extends Resource {
    
    private static final Logger logger = Logger.getLogger(HistoryResource.class);
    
    private final ContentService service;
    private final HistoryConverter converter;
    protected final SynchronizationHistory history;
    
    protected Long id;
    
    public HistoryResource(ContentService service, SynchronizationHistory history) {
        this.service = service;
        this.history = history;
        this.converter = new HistoryConverter();
    }

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        getVariants().add(new Variant(MediaType.TEXT_XML));
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
        this.id = Utils.findId(request, response);
    }
    
    @Override
    public Representation represent(Variant variant) {
        ContentSet cs = service.get(id);
        if (cs == null) {
            getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
        }
        
        Date startTime = getDate("startTime");
        Date endTime = getDate("endTime");
        int max = getInt("max");
        boolean showAll = getBoolean("showAll", false);
        
        if (checkMax(max)) {
            List<?> data = data(startTime, endTime, max, showAll);
            if (data != null) {
                if (max > 0) {
                    return converter.to(variant, data.subList(0, max));
                } else {
                    return converter.to(variant, data);
                }
            }
        }
        
        return null;
    }
    
    protected List<?> data(Date startTime, Date endTime, int max, boolean showAll) {
        return history.getSynchronizations(id, startTime, endTime, showAll, SynchronizationHistory.PAGE_SIZE);
    }
    
    /**
     * Find a date value in the request parameters
     * @param key
     * @return
     */
    protected Date getDate(String key) {
        String value = getString(key);
        if (value != null) {
            try {
                long timestamp = Long.parseLong(value);
                return new Date(timestamp);
            } catch (NumberFormatException ex) {
                logger.warn("Error converting date", ex);
            }
        }
        return null;
    }
    
    /**
     * Find a string value in the request parameters
     * @param key
     * @return
     */
    protected String getString(String key) {
        return getQuery().getFirstValue(key);
    }
    
    /**
     * Find an int value in the request parameters
     * @param key
     * @return -2 if malformed or -1 unspecified
     */
    protected int getInt(String key) {
        String value = getString(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ex) {
                logger.warn("Error paring int value: "+key, ex);
                return -2;
            }
        }
        return -1;
    }
    
    protected boolean getBoolean(String key, boolean defaultValue) {
        String value = getString(key);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }
    
    protected boolean checkMax(int max) {
        if (max == -2) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "max number of results parameter 'max' must be given");
            return false;
        }
        return true;
    }
}
