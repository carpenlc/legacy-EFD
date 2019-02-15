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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Set;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.security.auth.x500.X500Principal;

import org.apache.log4j.Logger;

public class ClientSocketFactory extends SSLSocketFactory implements TimeoutSocketFactory {

    protected static final Logger log = Logger.getLogger(ClientSocketFactory.class);
    
    private final SSLSocketFactory socketfactory;
    private final Set<String> allowedCipherSuites;

    protected ClientSocketFactory(SSLSocketFactory factory, Set<String> allowedCipherSuites) {
        this.socketfactory = factory;
        this.allowedCipherSuites = allowedCipherSuites;
    }
    
    /* KRJ 2016-10-25 
    
        Converted all SSLSocket creations to "try with resources"
        in response to an HP Fortify recommendation
    
    */

    @Override
    public Socket createSocket(String host, int port, InetAddress localAddress, int localPort, int timeout) throws IOException {
        		
        if (timeout == 0) {
            return socketfactory.createSocket(host, port, localAddress, localPort);
        } else {
            try (SSLSocket socket = (SSLSocket)this.socketfactory.createSocket()) {
                SocketAddress localaddr = new InetSocketAddress(localAddress, localPort);
                SocketAddress remoteaddr = new InetSocketAddress(host, port);
                socket.bind(localaddr);
                socket.connect(remoteaddr, timeout);
                return customizeSocket(socket);
            }
            catch (IOException ex) {
                throw new IOException ("Could not create SSL Socket", ex);
            }
        }
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress clientHost, int clientPort) throws IOException {
        try (SSLSocket socket = (SSLSocket)this.socketfactory.createSocket(host, port, clientHost, clientPort)) {
            return customizeSocket(socket);
        }
        catch (IOException ex) {
            throw new IOException ("Could not create SSL Socket", ex);
        }
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        try (SSLSocket socket = (SSLSocket)this.socketfactory.createSocket(host, port)) {
            return customizeSocket(socket);
        }
        catch (IOException ex) {
            throw new IOException ("Could not create SSL Socket", ex);
        }
    }
    
    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        try (SSLSocket socket = (SSLSocket)this.socketfactory.createSocket(host, port)) {
            return customizeSocket(socket);
        }
        catch (IOException ex) {
            throw new IOException ("Could not create SSL Socket", ex);
        }
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        try (SSLSocket socket = (SSLSocket)this.socketfactory.createSocket(address, port, localAddress, localPort)) {
            return customizeSocket(socket);
        }
        catch (IOException ex) {
            throw new IOException ("Could not create SSL Socket", ex);
        }
    }
    
    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        try (SSLSocket socket = (SSLSocket)this.socketfactory.createSocket(s, host, port, autoClose)) {
            return customizeSocket(socket);
        }
        catch (IOException ex) {
            throw new IOException ("Could not create SSL Socket", ex);
        }
    }
    
    @Override
    public Socket createSocket() throws IOException {
        try (SSLSocket socket = (SSLSocket)this.socketfactory.createSocket()) {
            return customizeSocket(socket);
        }
        catch (IOException ex) {
            throw new IOException ("Could not create SSL Socket", ex);
        }
    }

    private Socket customizeSocket(SSLSocket socket) {
        socket.setEnabledCipherSuites(CipherSuiteUtil.getEnabledCipherSuites(socket.getSupportedCipherSuites(), allowedCipherSuites));
        socket.addHandshakeCompletedListener(new VerifyHostnameListener());
        return (Socket)socket;
    }
    
    @SuppressWarnings("unused")
    private void logCertificateChain(javax.security.cert.X509Certificate[] certs) {
        log.debug("Server certificate chain:");
        for (int i = 0; i < certs.length; i++) {
            log.info("X509Certificate[" + i + "]=" + certs[i]);
        }
    }

    /* This is for checking server certificates against host names to prevent man-in-the-middle attacks */
    /* Low is not checking, Medium is checking but only logging that the hosts do not match, High is enforcing hosts match */
    public enum VerifyHostToCredentials {
        NEVER, LOG_ONLY, ALWAYS
    };

    private static class SSLSocketException extends RuntimeException {

        private static final long serialVersionUID = 1l;

        public SSLSocketException(String message) {
            super(message);
        }

        public SSLSocketException(Exception e) {
            super(e);
        }
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return this.socketfactory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return this.socketfactory.getSupportedCipherSuites();
    }
    
    protected static class VerifyHostnameListener implements HandshakeCompletedListener {

        private final VerifyHostToCredentials strictness = VerifyHostToCredentials.LOG_ONLY;
        
        @Override
        public void handshakeCompleted(HandshakeCompletedEvent event) {
            verifyHostname(event.getSession());
        }
        
        private void verifyHostname(SSLSession sslSession) {
            if (this.strictness.equals(VerifyHostToCredentials.NEVER)) {
                return;
            }
     
            String hostname = sslSession.getPeerHost();
            try {
                InetAddress.getByName(hostname);
            } catch (UnknownHostException uhe) {
                handleException(new UnknownHostException("Could not resolve SSL sessions Server hostname: " + hostname));
            }

            try {
                Certificate[] certs = sslSession.getPeerCertificates();
                // this.logCertificateChain(certs);
                String dn = ((X509Certificate) certs[0]).getSubjectX500Principal().getName(X500Principal.RFC2253);
                String cn = getCN(dn);
                if (!hostname.equalsIgnoreCase(cn)) {
                    handleException(new SSLPeerUnverifiedException("Hostname invalid: expected '" + hostname + "', received '" + cn + "'"));
                }
            } catch (SSLPeerUnverifiedException e) {
                handleException(e);
            } catch (ArrayIndexOutOfBoundsException e) {
                handleException(new SSLPeerUnverifiedException("No server certificates found!"));
            } catch (ClassCastException e) {
                handleException(new SSLPeerUnverifiedException("Server certificate not of type X509"));
            }
        }
        
        private void handleException(IOException e) throws SSLSocketException {
            SSLSocketException e1 = new SSLSocketException(e);
            if (strictness.equals(VerifyHostToCredentials.LOG_ONLY)) {
                log.warn(e1);
            } else {
                throw e1;
            }
        }
        
        /* TODO: Parse better using RFC 2253 */
        private String getCN(String dn) {
            int i = 0;
            i = dn.indexOf("CN=");
            if (i == -1) {
                return null;
            }
            dn = dn.substring(i + 3); // get the remaining DN without CN=
            char[] dncs = dn.toCharArray();
            for (i = 0; i < dncs.length; i++) {
                if (dncs[i] == ',' && i > 0 && dncs[i - 1] != '\\') {
                    break;
                }
            }
            return dn.substring(0, i);
        }
    }
}
