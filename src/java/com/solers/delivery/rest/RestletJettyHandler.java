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
package com.solers.delivery.rest;

import java.util.List;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.ext.jetty.JettyHandler;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class RestletJettyHandler extends JettyHandler {
    
    public RestletJettyHandler(Application application) {
        super(createServer(application), true);
    }
    
    @Override
    protected void doStart() throws Exception {
        
    }

    @Override
    protected void doStop() throws Exception {
        
    }

    private static Server createServer(Application application) {
        Component component = new Component();
        application.setContext(component.getContext().createChildContext());
        component.getDefaultHost().attach(application);
        return new Server(component.getContext().createChildContext(), (List<Protocol>) null, null, Protocol.UNKNOWN_PORT, component);
    }
    
}
