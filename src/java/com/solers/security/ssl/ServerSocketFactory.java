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

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.rmi.server.RMIServerSocketFactory;
import java.util.Set;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

public class ServerSocketFactory extends SSLServerSocketFactory implements RMIServerSocketFactory {

    private SSLServerSocketFactory factory;
    private Set<String> allowedCipherSuites;
    boolean needAuth;
    
    protected ServerSocketFactory(SSLServerSocketFactory factory, Set<String> allowedCipherSuites, boolean needAuth) {
        this.factory = factory;
        this.allowedCipherSuites = allowedCipherSuites;
        this.needAuth = needAuth;
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return factory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return factory.getSupportedCipherSuites();
    }

    @Override
    public ServerSocket createServerSocket(int port) throws IOException {
        SSLServerSocket serverSocket = (SSLServerSocket)factory.createServerSocket(port);
        serverSocket.setEnabledCipherSuites(CipherSuiteUtil.getEnabledCipherSuites(serverSocket.getSupportedCipherSuites(), allowedCipherSuites));
        serverSocket.setNeedClientAuth(this.needAuth);
       return serverSocket;
    }

    @Override
    public ServerSocket createServerSocket(int port, int backlog) throws IOException {
        SSLServerSocket serverSocket = (SSLServerSocket)factory.createServerSocket(port, backlog);
        serverSocket.setEnabledCipherSuites(CipherSuiteUtil.getEnabledCipherSuites(serverSocket.getSupportedCipherSuites(), allowedCipherSuites));
        serverSocket.setNeedClientAuth(this.needAuth);
        return serverSocket;
    }

    @Override
    public ServerSocket createServerSocket(int port, int backlog, InetAddress ifAddress) throws IOException {
        SSLServerSocket serverSocket = (SSLServerSocket)factory.createServerSocket(port, backlog, ifAddress);
        serverSocket.setEnabledCipherSuites(CipherSuiteUtil.getEnabledCipherSuites(serverSocket.getSupportedCipherSuites(), allowedCipherSuites));
        serverSocket.setNeedClientAuth(this.needAuth);
        return serverSocket;
    }
}
