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
package com.solers.delivery.rest.connectors;

import org.mortbay.jetty.AbstractConnector;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.ext.jetty.JettyServerHelper;

import com.solers.security.jetty.SSLConnector;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class JettySslHelper extends JettyServerHelper {
    
    public JettySslHelper() {
        this(null);
    }
    
    public JettySslHelper(Server server) {
        super(server);
        getProtocols().add(Protocol.HTTPS);
    }
    
    @Override
    protected AbstractConnector createConnector() {
        try {
            return new SSLConnector();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
