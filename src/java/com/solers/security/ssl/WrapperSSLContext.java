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
package com.solers.security.ssl;

import java.security.KeyManagementException;
import java.security.SecureRandom;
import java.util.Set;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLContextSpi;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class WrapperSSLContext extends SSLContext {

    public WrapperSSLContext(SSLContext context, Set<String> cipherSuites, boolean needAuth) {
        super(new WrapperSSLContextSpi(context, cipherSuites, needAuth), context.getProvider(), context.getProtocol());
    }
    
    private static class WrapperSSLContextSpi extends SSLContextSpi {
        
        private final SSLContext context;
        private final Set<String> cipherSuites;
        private final boolean needAuth;
        
        public WrapperSSLContextSpi(SSLContext context, Set<String> cipherSuites, boolean needAuth) {
            this.context = context;
            this.cipherSuites = cipherSuites;
            this.needAuth = needAuth;
        }
        
        @Override
        protected SSLServerSocketFactory engineGetServerSocketFactory() {
            return new ServerSocketFactory(context.getServerSocketFactory(), cipherSuites, needAuth);
        }

        @Override
        protected SSLSocketFactory engineGetSocketFactory() {
            return new ClientSocketFactory(context.getSocketFactory(), cipherSuites);
        }

        @Override
        protected SSLEngine engineCreateSSLEngine() {
            return context.createSSLEngine();
        }

        @Override
        protected SSLEngine engineCreateSSLEngine(String peerHost, int peerPort) {
            return context.createSSLEngine(peerHost, peerPort);
        }

        @Override
        protected SSLSessionContext engineGetClientSessionContext() {
            return context.getClientSessionContext();
        }

        @Override
        protected SSLParameters engineGetDefaultSSLParameters() {
            return context.getDefaultSSLParameters();
        }

        @Override
        protected SSLSessionContext engineGetServerSessionContext() {
            return context.getServerSessionContext();
        }

        @Override
        protected SSLParameters engineGetSupportedSSLParameters() {
            return context.getSupportedSSLParameters();
        }

        @Override
        protected void engineInit(KeyManager[] arg0, TrustManager[] arg1, SecureRandom arg2) throws KeyManagementException {
            throw new UnsupportedOperationException("WrapperSSLContext cannot be reinitialized");
        }
    }
}