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

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;

import org.restlet.Client;
import org.restlet.engine.http.StreamClientHelper;

/**
 * Override the default Client Helper to use our pre-confired SSL Context
 * 
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class SslClientHelper extends StreamClientHelper {
    
    public SslClientHelper() {
        this(null);
    }
    
    public SslClientHelper(Client client) {
        super(client);
    }

    @Override
    protected SocketFactory createSecureSocketFactory() throws IOException, GeneralSecurityException {
        return SSLContext.getDefault().getSocketFactory();
    }
    
}
