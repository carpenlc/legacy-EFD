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
package com.solers.delivery.reports.metrics.server;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

import com.solers.delivery.reports.metrics.server.csv.CsvStore;


/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class DataResource extends Resource {

    private static final Logger log = Logger.getLogger(DataResource.class);
    
    private final CsvStore store;
    
    public DataResource(CsvStore store) {
        this.store = store;
        getVariants().add(new Variant(MediaType.TEXT_XML));
    }
    
    @Override
    public void storeRepresentation(Representation entity) throws ResourceException {
        String host = getRequest().getClientInfo().getAddress();
        try {
            String xml = entity.getText();
            store.store(host, xml);
        } catch (IOException ex) {
            log.error("Error storing message from host: "+host, ex);
            throw new ResourceException(ex);
        }
    }

    @Override
    public boolean allowGet() {
        return false;
    }

    @Override
    public boolean allowPut() {
        return true;
    }
}
