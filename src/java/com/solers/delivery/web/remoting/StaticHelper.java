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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.directwebremoting.WebContextFactory;

import com.solers.delivery.domain.FileFilter;
import com.solers.delivery.event.ContentEvent.ContentEventAction;
import com.solers.delivery.transport.gbs.GBSConfigurator;
import com.solers.delivery.reports.history.SynchronizationHistory;
import com.solers.delivery.web.util.SecurityBannerConfig;
import com.solers.util.unit.FileSizeUnit;
import com.solers.util.unit.TimeIntervalUnit;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class StaticHelper {
    
    private final SecurityBannerConfig bannerConfig;
    private final Map<String, String> timeIntervals;
    private final Map<String, String> fileSizeUnits; 
    private final Map<String, String> filterTypes;
    private final List<String> contentEventActions;
    
    public StaticHelper(SecurityBannerConfig bannerConfig) {
        this.bannerConfig = bannerConfig;
        timeIntervals = new LinkedHashMap<String, String>(TimeIntervalUnit.values().length - 1);
        fileSizeUnits = new LinkedHashMap<String, String>(FileSizeUnit.values().length);
        filterTypes = new LinkedHashMap<String, String>(FileFilter.Pattern.values().length);
        contentEventActions = new ArrayList<String>(3);

        // These are added manually so milliseconds is skipped
        timeIntervals.put(TimeIntervalUnit.SECONDS.toString(), TimeIntervalUnit.SECONDS.getDisplayName());
        timeIntervals.put(TimeIntervalUnit.MINUTES.toString(), TimeIntervalUnit.MINUTES.getDisplayName());
        timeIntervals.put(TimeIntervalUnit.HOURS.toString(), TimeIntervalUnit.HOURS.getDisplayName());
        timeIntervals.put(TimeIntervalUnit.DAYS.toString(), TimeIntervalUnit.DAYS.getDisplayName());
        
        for (FileSizeUnit u : FileSizeUnit.values()) {
            fileSizeUnits.put(u.toString(), u.getDisplayName());
        }
        
        for (FileFilter.Pattern p : FileFilter.Pattern.values()) {
            filterTypes.put(p.toString(), p.getDisplayName());
        }
        
        // These are only used by the consumer so we 
        // skip the "supplier" and "none" actions
        contentEventActions.add(ContentEventAction.ADD.value());
        contentEventActions.add(ContentEventAction.DELETE.value());
        contentEventActions.add(ContentEventAction.UPDATE.value());
    }

    public Map<String, String> getTimeIntervals() {
        return timeIntervals;
    }

    public Map<String, String> getFileSizeUnits() {
        return fileSizeUnits;
    }

    public Map<String, String> getFilterTypes() {
        return filterTypes;
    }
    
    public List<String> getContentEventActions() {
        return contentEventActions;
    }
    
    public Map<String, Object> getData() {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("banner", bannerConfig);
        result.put("gbsEnabled", GBSConfigurator.isGBSEnabled());
        result.put("pageSize", SynchronizationHistory.PAGE_SIZE);
        result.put("timeout", getSession().getMaxInactiveInterval());
        result.put("fileSizeUnits", fileSizeUnits);
        result.put("timeIntervals", timeIntervals);
        result.put("filterTypes", filterTypes);
        result.put("contentEventActions", contentEventActions);
        return result;
    }
    
    private HttpSession getSession() {
        return WebContextFactory.get().getSession();
    } 
}
