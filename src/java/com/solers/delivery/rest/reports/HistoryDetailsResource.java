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

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

import com.solers.delivery.content.ContentService;
import com.solers.delivery.reports.history.Synchronization;
import com.solers.delivery.reports.history.SynchronizationHistory;
import com.solers.delivery.rest.Utils;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class HistoryDetailsResource extends HistoryResource {
    
    private String detailsId;
    
    public HistoryDetailsResource(ContentService service, SynchronizationHistory history) {
        super(service, history);
    }
    
    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        this.detailsId = Utils.findString(request, response, "detailsId");
    }

    @Override
    protected List<?> data(Date startTime, Date endTime, int max, boolean showAll) {
        Synchronization sync = history.getSynchronization(id, detailsId);
        if (sync == null) {
            getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
        }
        return history.getSynchronizationDetails(id, detailsId, null, null, SynchronizationHistory.PAGE_SIZE);
    }
}
