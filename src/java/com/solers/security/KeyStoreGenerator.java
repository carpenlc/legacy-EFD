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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Collection;

import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.commons.codec.binary.Base64;
//import org.apache.log4j.Logger;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.PasswordFinder;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class KeyStoreGenerator {
    
    //private static final Logger log = Logger.getLogger(KeyStoreGenerator.class);
    
    private final File privateKey;
    private final File certChain;
    
    /**
     * 
     * @param privateKey Private key in PEM format.  Can be either un-encrypted or PKCS#8 encrypted
     * @param certChain X.509 Certificate chain file in PEM format
     */
    public KeyStoreGenerator(File privateKey, File certChain) {
        this.privateKey = privateKey;
        this.certChain = certChain;
    }
    
    /**
     * Generate and save a PKCS12 keystore to {@code destination} using
     * the specified private key and certificate chain
     * @param destination
     * @param keyStorePassword
     * @param keyPassword
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public void saveKeyStore(File destination, char [] keyStorePassword, char [] keyPassword) throws GeneralSecurityException, IOException {
        char [] keystorePassword = keyStorePassword;
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, keystorePassword);
        keyStore.setKeyEntry("key", getPrivateKey(keyPassword), keyPassword, getCertificates());
        FileOutputStream output = new FileOutputStream(destination);
        try {
            keyStore.store(output, keystorePassword);
        } finally {
            output.close();
        }
    }
    
    /**
     * Generate and save a JKS trust store to {@code destination} using
     * the specified certificate chain
     * @param destination
     * @param trustStorePassword
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public void saveTrustStore(File destination, char [] trustStorePassword) throws GeneralSecurityException, IOException {
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(null, trustStorePassword);
        
        int count = 0;
        for (Certificate cert : getCertificates()) {
            trustStore.setCertificateEntry("Cert "+(++count), cert);
        }
        
        FileOutputStream output = new FileOutputStream(destination);
        try {
            trustStore.store(output, trustStorePassword);
        } finally {
            output.close();
        }
    }
    
    private PrivateKey getPrivateKey(char [] keyPassword) throws GeneralSecurityException, IOException {
        String firstLine = firstLine(privateKey);
        
        if (firstLine.contains("RSA PRIVATE KEY")) {
            return readUnencryptedKey(keyPassword);
        }
        if (firstLine.contains("ENCRYPTED PRIVATE KEY")) {
            return readEncryptedKey(keyPassword);
        }
        throw new IllegalArgumentException(firstLine+" is not understood");
    }
    
    private Certificate [] getCertificates() throws GeneralSecurityException, IOException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        
        FileInputStream input = new FileInputStream(certChain);
        try {
            Collection<? extends Certificate> certs = factory.generateCertificates(input);
            for (Certificate cert : certs) {
                validate(cert);
            }
            return certs.toArray(new Certificate[] {});
        } finally {
            input.close();
        }
    }
    
    private PrivateKey readUnencryptedKey(char [] keyPassword) throws IOException {
        //log.debug(String.format("Lulu the dog-bugger is here at timestamp %s",  System.currentTimeMillis()));
        FileReader fileReader = new FileReader(privateKey);
        PEMParser r = new PEMParser(fileReader);
        
        //PemReader r = new PemReader(fileReader, new DefaultPasswordFinder(keyPassword));
        PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder().build(keyPassword);
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
        try {
            Object object = r.readObject();
            KeyPair pair;
            if (object instanceof PEMEncryptedKeyPair) {
               pair = converter.getKeyPair(((PEMEncryptedKeyPair) object).decryptKeyPair(decProv));  
               return pair.getPrivate();
            }
            else if (object instanceof PrivateKey) {
                return converter.getPrivateKey((PrivateKeyInfo) object);
            }
            else {
                return converter.getKeyPair((PEMKeyPair) object).getPrivate();
            }
        } catch (IOException ex) {
            throw new IOException("The private key could not be decrypted", ex);
        } finally {
            r.close();
            fileReader.close();
        }
    }
    
    //private PrivateKey readUnencryptedKey(char [] keyPassword) throws IOException {
     //   FileReader fileReader = new FileReader(privateKey);
     //   PEMReader r = new PEMReader(fileReader, new DefaultPasswordFinder(keyPassword));
     //   try {
     //       KeyPair pair = (KeyPair) r.readObject();
     //       return pair.getPrivate();
     //   } catch (IOException ex) {
      //      throw new IOException("The private key could not be decrypted", ex);
     //   } finally {
     //       r.close();
     //       fileReader.close();
     //   }
  //  }
    
    private PrivateKey readEncryptedKey(char [] keyPassword) throws GeneralSecurityException, IOException {
        EncryptedPrivateKeyInfo info = new EncryptedPrivateKeyInfo(readKeyData());
        KeySpec secretKeySpec = new PBEKeySpec(keyPassword);
        SecretKey key = SecretKeyFactory.getInstance(info.getAlgName()).generateSecret(secretKeySpec);
        
        KeyFactory kf = KeyFactory.getInstance("RSA");
        try {
            PKCS8EncodedKeySpec spec = info.getKeySpec(key);
            return kf.generatePrivate(spec);
        } catch (InvalidKeyException ex) {
            throw new IOException("The private key could not be decrypted", ex);
        }
    }
    
    /**
     * @return Base64 decoded key data from {@code privateKey}
     * @throws IOException
     */
    private byte [] readKeyData() throws IOException {
        StringBuilder builder = new StringBuilder();
        
        // KRJ 2016-08-31: Converted reader creation to "try with resources"
        // based on an HP Fortify recommendation
        
        try (BufferedReader reader = new BufferedReader(new FileReader(privateKey))) {
            String line = reader.readLine();
            while (line != null) {
                if (!line.startsWith("-----BEGIN") && !line.startsWith("-----END")) {
                    builder.append(line);
                }
                line = reader.readLine();
            }
        } catch (IOException ex) {
            throw new IOException("Error reading from file: " + ex.getMessage(), ex);
        }
        
        return Base64.decodeBase64(builder.toString().getBytes());
    }
    
    /**
     * @param file
     * @return The first line of text from {@code file}
     * @throws IOException
     */
    private String firstLine(File file) throws IOException {
        
        // KRJ 2016-08-31: Converted reader creation to "try with resources"
        // based on an HP Fortify recommendation
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
        
            return reader.readLine();
        } catch (IOException ex) {
            throw new RuntimeException("Error reading from file: " + ex.getMessage(), ex);
        }
    }
    
    /**
     * Check the validity of {@code cert}
     * @param cert
     * @throws CertificateExpiredException
     * @throws CertificateNotYetValidException
     */
    private void validate(Certificate cert) throws CertificateExpiredException, CertificateNotYetValidException {
        X509Certificate xCert = (X509Certificate) cert;
        try {
            xCert.checkValidity();
        } catch (CertificateExpiredException ex) {
            throw new CertificateExpiredException("Certificate: " + xCert.getSubjectX500Principal().getName() + " has expired: " + ex.getMessage());
        } catch (CertificateNotYetValidException ex) {
            throw new CertificateNotYetValidException("Certificate: " + xCert.getSubjectX500Principal().getName() + " is not yet valid: " + ex.getMessage());
        }
    }
    
    protected static class DefaultPasswordFinder implements PasswordFinder {

        private final char [] password;
        
        private DefaultPasswordFinder(char [] password) {
            this.password = password;
        }
        
        @Override
        public char[] getPassword() {
            return Arrays.copyOf(password, password.length);
        }

    }
    
    static {
        Security.addProvider(new BouncyCastleProvider());
    }
}
