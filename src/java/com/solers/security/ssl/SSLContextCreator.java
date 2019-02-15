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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.PKIXParameters;
import java.util.Set;

import javax.net.ssl.CertPathTrustManagerParameters;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.log4j.Logger;

import com.solers.security.CRLConfigurator;
import com.solers.security.CRLConfigurator.ConfigException;

public class SSLContextCreator {

    private static final String SSLCONTEXT_ALGORITHM = "TLS";
    private static final String KEYSTORE_TYPE = "PKCS12";
    private static final String TRUSTSTORE_TYPE = "JKS";
    
    private final File keystoreFile;
    private final File truststoreFile;
    private final Set<String> cipherSuites;
    private final boolean needAuth;
    private final SSLContext sslContext;
    private final int sslSessionTimeout;
    private final SecureRandom random;
    
    public SSLContextCreator(String keyPassword, String keystorePassword, String truststorePassword, String keystore, String truststore, Set<String> cipherSuites, boolean needAuth, int sslSessionTimeout, SecureRandom random) {
        this.cipherSuites = cipherSuites;
        this.needAuth = needAuth;
        this.keystoreFile = new File(keystore);
        this.truststoreFile = new File(truststore);
        this.sslSessionTimeout = sslSessionTimeout;
        this.random = random;
        
        if (!this.keystoreFile.exists()) throw new SSLContextException(keystore+" does not exist");
        if (!this.truststoreFile.exists()) throw new SSLContextException(truststore+" does not exist");
        
        sslContext = createSSLContext(keyPassword, keystorePassword, truststorePassword);
    }
    
    public void init() {
        SSLContext context = new WrapperSSLContext(sslContext, cipherSuites, needAuth);
        SSLContext.setDefault(context);
    }
    
    protected InputStream getTruststore() throws IOException {
        return new FileInputStream(truststoreFile);
    }
    
    protected InputStream getKeystore() throws IOException {
        return new FileInputStream(keystoreFile);
    }
    
    private SSLContext createSSLContext(String keyPassword, String keystorePassword, String truststorePassword) {
        KeyManager[] keymanagers = getKeyManagers(keyPassword, keystorePassword);
        TrustManager[] trustmanagers = getTrustManagers(truststorePassword);
        
        try {
            SSLContext sslContext = SSLContext.getInstance(SSLCONTEXT_ALGORITHM);
            sslContext.init(keymanagers, trustmanagers, random);
            sslContext.getClientSessionContext().setSessionTimeout(sslSessionTimeout);
            sslContext.getServerSessionContext().setSessionTimeout(sslSessionTimeout);
            return sslContext;
        } catch (KeyManagementException e) {
            throw new SSLContextException("Key management exception: " + e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            throw new SSLContextException("Unsupported algorithm exception: " + e.getMessage(), e);
        }
    }
    
    protected TrustManager[] getTrustManagers(String truststorePassword) {
        try {
            InputStream input = getTruststore();
            try {
                KeyStore truststore = KeyStore.getInstance(TRUSTSTORE_TYPE);
                truststore.load(input, truststorePassword.toCharArray());
                TrustManagerFactory tmfactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());            
                PKIXParameters certPathParameters = CRLConfigurator.loadCertPathParameters(truststore);
                ManagerFactoryParameters mfp = new CertPathTrustManagerParameters(certPathParameters);
                tmfactory.init(mfp);
                return tmfactory.getTrustManagers();
            } finally {
                input.close();
            }
        } catch (InvalidAlgorithmParameterException e) {
            throw new SSLContextException("Initialization of trust manager exception: " + e.getMessage(), e);
        } catch (ConfigException e) {
            throw new SSLContextException("Certification path exception: " + e.getCause().getMessage(), e);
        } catch (KeyStoreException e) {
            throw new SSLContextException("Keystore exception (truststore): " + e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            throw new SSLContextException("Unsupported algorithm exception (truststore): " + e.getMessage(), e);
        } catch (CertificateException e) {
            throw new SSLContextException("Certificate exception (truststore): " + e.getMessage(), e);
        } catch (IOException e) {
            throw new SSLContextException("I/O error reading file (truststore): " + e.getMessage(), e);
        }
    }
    
    protected KeyManager[] getKeyManagers(String keyPassword, String keystorePassword) {
        try {
            InputStream input = getKeystore();
            try  {
                KeyStore keystore = KeyStore.getInstance(KEYSTORE_TYPE);
                keystore.load(input, keystorePassword.toCharArray());
                KeyManagerFactory kmfactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                kmfactory.init(keystore, keyPassword.toCharArray());
                return kmfactory.getKeyManagers();
            } finally {
                input.close();
            }
        } catch (UnrecoverableKeyException e) {
            throw new SSLContextException("Unrecoverable Key exception (keystore): " + e.getMessage(), e);
        } catch (KeyStoreException e) {
            throw new SSLContextException("Keystore exception (keystore): " + e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            throw new SSLContextException("Unsupported algorithm exception (keystore): " + e.getMessage(), e);
        } catch (CertificateException e) {
            throw new SSLContextException("Certificate exception (keystore): " + e.getMessage(), e);
        } catch (IOException e) {
            throw new SSLContextException("I/O error reading file (keystore): " + e.getMessage(), e);
        }
    }
    
   public static class SSLContextException extends RuntimeException {

        private static final long serialVersionUID = 1l;
        private static final Logger log = Logger.getLogger(SSLContextException.class);
       
        public SSLContextException(String message) {
            super(message);
            log.error(message);
        }

        public SSLContextException(String message, Exception e) {
            super(e);
            log.error(message, e);
        }
    }
}
