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
package com.solers.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileFilter;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CRL;
import java.security.cert.CertSelector;
import java.security.cert.CertStore;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.LDAPCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXParameters;
import java.security.cert.X509CertSelector;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.solers.security.ssl.SSLContextCreator.SSLContextException;

/**
 * The CRLConfigurator handles security configuration concerns.  For example, enabling OCSP
 * will not function unless certificate revocation is also enabled, but these two
 * properties exist in different property buckets.  Also, Security properties have
 * a special meaning between set and unset, so this is guarded as well. 
 */
public final class CRLConfigurator {
    private static final Logger log = Logger.getLogger(CRLConfigurator.class);
    private static final String OCSP_ENABLE = "ocsp.enable";

    private static final String CRLDP_ENABLE = "com.sun.security.enableCRLDP";
    private static final String REVOCATION_ENABLE = "com.sun.net.ssl.checkRevocation";
    
    private static final String EFD_REVOCATION_STORES = "certificateRevocation.crlStores";
    private static final String EFD_REVOCATION_ENABLE = "certificateRevocation.enable";
    private static final String EFD_CRLDP_PROP = "certificateRevocation.dynamic";
    
    private static final String LDAP_IDENT = "ldap://";
    
    private CRLConfigurator() {
        //do not create
    }
    
    /**
     * Toggle whether OCSP shall be enabled.  By setting this to true, certificate
     * revocation will be implicitly enabled
     * @param enabled
     */
    public static void setOcspEnabled(String enable) {
        boolean enabled = Boolean.parseBoolean(enable);
        if (enabled) log.info("OCSP has been enabled (certificate revocation has also been enabled).");
        
        Security.setProperty(OCSP_ENABLE, String.valueOf(enabled));
        // OCSP will not work unless certificate revocation is also enabled
        if (enabled && !isRevocationEnabled()) {
            setRevocationEnabled(Boolean.toString(true));
        }
    }
    
    public static boolean isAnyRevocationEnabled() {
        boolean revocation = isRevocationEnabled();
        boolean ocsp = Boolean.parseBoolean(System.getProperty(OCSP_ENABLE));
        return revocation || ocsp;
    }
    
    public static boolean isRevocationEnabled() {
        return Boolean.parseBoolean(System.getProperty(EFD_REVOCATION_ENABLE));
    }
    
    public static void setRevocationEnabled(String enabled) {
        boolean isEnabled = Boolean.parseBoolean(enabled);
        if (isEnabled) log.info("Certificate revocation has been enabled.");
        System.setProperty(REVOCATION_ENABLE, Boolean.toString(isEnabled));
    }
    
    public static void setCRLDPEnabled(String enable) {
        boolean enabled = Boolean.parseBoolean(enable);
        if (enabled) log.info("CRLDP has been enabled.");
        System.setProperty(CRLDP_ENABLE, Boolean.toString(enabled));
    }
    
    /**
     * Check to see if EFD CRLDP property is set.
     */
    public static boolean isCRLDPEnabled() {
        return Boolean.parseBoolean(System.getProperty(EFD_CRLDP_PROP));
    }
    
    public static void setSecurityProperty(String property, String value) {
        if (value == null || "".equals(value.trim())) return;
        log.info("Security property " + property + " has been overridden.");
        Security.setProperty(property, value);
    }
    
    public static PKIXParameters loadCertPathParameters(KeyStore trusted) throws ConfigException {
        //This must be set *before* we build the SSL context, so manually invoke here
        //just in case.
        if (isCRLDPEnabled() && isRevocationEnabled()) {
            setCRLDPEnabled(Boolean.toString(true));
        }
        
        try {
            PKIXParameters p = new PKIXParametersWrapper(trusted, new X509CertSelector()); 
            //This turns on revocation by default.  We need it to be OFF if no revocation
            //method is specified/enabled, otherwise all network connections will fail.
            p.setRevocationEnabled(isAnyRevocationEnabled());
            return p;
        } catch (KeyStoreException e) {
            throw new ConfigException(e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new ConfigException(e);
        }
    }
    
    private static CertStore loadLDAPStore(String host, int port) throws ConfigException {
        LDAPCertStoreParameters ldapParams = new LDAPCertStoreParameters(host, port);
        try {
            return CertStore.getInstance("LDAP", ldapParams);
        } catch (InvalidAlgorithmParameterException e) {
            throw new ConfigException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new ConfigException(e);
        }
    }
    
    public static class ConfigException extends Exception {
        private static final long serialVersionUID = 1L;

        public ConfigException(Throwable t) {
            super(t);
        }
    }
    
    private static class PKIXParametersWrapper extends PKIXBuilderParameters {
        private Map<String, CertStore> certStoreMap;
        private Map<String, Long> modificationDateMap;
        
        PKIXParametersWrapper(KeyStore keystore, CertSelector targetConstraints) 
                                 throws KeyStoreException, InvalidAlgorithmParameterException {
            super(keystore, targetConstraints);
            certStoreMap = new ConcurrentHashMap<String, CertStore>();
            modificationDateMap = new ConcurrentHashMap<String, Long>();
        }
        
        @Override
        public List<CertStore> getCertStores() {
            List<CertStore> stores = new ArrayList<CertStore>();
            
            try {
                //We will only parse CRL stores if dynamic is turned off and revocation
                //is enabled.  This meets requirement that user must manually configure
                //the fail over capability.
                if (!isCRLDPEnabled() && isRevocationEnabled()) {
                     return getCertStoresFromPath();
                }
            } catch (ConfigException e) {
                throw new SSLContextException("Certification path exception: " + e.getCause().getMessage(), e);
            }
            return stores;
        }
        
        private List<CertStore> getCertStoresFromPath() throws ConfigException {           
            String pathProperty = System.getProperty(EFD_REVOCATION_STORES);           
            List<CertStore> stores = new ArrayList<CertStore>();           
            if (pathProperty != null) {
                String[] pathElements = pathProperty.split("\\,");
                for (String path : pathElements) {                  
                    log.info("Adding CRL location: " + path);
                    CertStore store = null;
                    if (path.startsWith(LDAP_IDENT)) {
                        String ldapString = path.substring(LDAP_IDENT.length());
                        String[] ldapData = ldapString.split("\\:");
                        int port = 389;
                        if (ldapData.length > 1) { 
                            port = Integer.parseInt(ldapData[1]);
                        }
                        String host = ldapData[0];                        
                        store = loadLDAPStore(host, port);
                    } else {
                        store = loadCertStore(path);
                    }
                    if (store != null) {
                        stores.add(store);
                    }
                }
            }                      
            return stores;
        }
        
        private CertStore loadCertStore(String path) throws ConfigException {            
            File f = new File(path);                       
            if (!f.isDirectory() && !f.exists()) {
                log.info("Could not locate CRL directory: " + path);
                return null;
            }       
            
            FileInputStream fis = null;
            
            try {
                CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                File[] certs = f.listFiles(new HiddenFileFilter());
                if (storeChanged(f, certs)) {
                    Set<CRL> certCollection = new HashSet<CRL>(certs.length);
                    for (File cert : certs) {            
                        log.info("Adding CRL file: " + cert.getAbsolutePath());
                        fis = new FileInputStream(cert);
                        CRL c = certFactory.generateCRL(fis);
                        certCollection.add(c);
                        modificationDateMap.put(cert.getPath(), cert.lastModified());
                        fis.close();
                    }

                    CollectionCertStoreParameters certStoreParams = new CollectionCertStoreParameters(certCollection);
                    CertStore store = CertStore.getInstance("Collection", certStoreParams);                    
                    certStoreMap.put(f.getPath(), store);
                    return store;
                } else {                  
                    return certStoreMap.get(f.getPath());
                }
            } catch (Exception e) { 
                try {
                    if (fis != null) {                 
                        fis.close();                    
                    }
                } catch (java.io.IOException ioe) {
                    log.error("Failed to close cert store file.");
                }
                
                if (e instanceof RuntimeException) throw (RuntimeException) e;
                throw new ConfigException(e);
            }
        }
        
        private boolean storeChanged(File storeDir, File[] files) {             
            if (!certStoreMap.containsKey(storeDir.getPath())) {
                return true;
            } else {
                for (File file : files) {                    
                    Long lastModified = modificationDateMap.get(file.getPath());
                    if (lastModified == null || lastModified != file.lastModified()) {
                        return true;
                    }                    
                }
            }
            return false;            
        }
        
        private static class HiddenFileFilter implements FileFilter {
            public boolean accept(File file) {
                return !file.isHidden();
            }
        }
    }
}