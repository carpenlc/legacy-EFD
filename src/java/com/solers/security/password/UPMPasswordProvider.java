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
package com.solers.security.password;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.Provider;
import java.security.spec.KeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERBMPString;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.pkcs.AuthenticatedSafe;
import org.bouncycastle.asn1.pkcs.ContentInfo;
import org.bouncycastle.asn1.pkcs.Pfx;
import org.bouncycastle.asn1.pkcs.SafeBag;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.solers.security.password.MapPasswordProvider;
import com.solers.security.password.PasswordProvider;
import com.solers.security.password.PasswordReader;
import java.util.logging.Level;
import org.apache.log4j.Logger;

/**
 * This class is based heavily on {@link org.bouncycastle.jce.provider.JDKPKCS12KeyStore#engineLoad(java.security.KeyStore.LoadStoreParameter)}
 * 
 * Please note that my understanding of the format of this file is limited and I only arrived at the solution through a combination
 * of guesswork, intuition and brute force.  Please take what I say below with a large grain of salt.
 * 
 * This class parses PKCS12 keystore containing data stored in PKCS #12 Secret Bags (ASN1 type: 1.2.840.113549.1.12.10.1.5).
 * 
 * Each bag contains a password and an identifier for that password.  The password is stored as a DESEDE encrypted String.
 * 
 * The encryption key was originally derived using the the OpenSSL EVP_BytesToKey function.  
 * Luckily, I was able to find a Java implementation of this function.
 * 
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 * 
 * @see http://www.openssl.org/docs/crypto/EVP_BytesToKey.html
 * @see http://olabini.com/blog/tag/evp_bytestokey/
 */
public class UPMPasswordProvider implements PasswordProvider {
    
    private static final Logger log = Logger.getLogger(UPMPasswordProvider.class);
    
    private final Provider provider;
    private final File file;
    
    private PasswordProvider delegate;
    
    public UPMPasswordProvider(String path) {
        this.provider = new BouncyCastleProvider();
        if (path.startsWith("file:/")) {
            try {
                file = new File(new URI(path));
            } catch (URISyntaxException ex) {
                throw new RuntimeException("Invalid path specified: "+path, ex);
            }
        } else {
            file = new File(path);
        }
    }
  
    @Override
    public String getPassword(String key) {
        return delegate.getPassword(key);
    }

    @Override
    public void setPassword(String key, String password) {
        throw new UnsupportedOperationException(getClass().getName()+" does not support writing to the UPM keystore");
    }
    
    @PostConstruct
    public void initialize() /* throws IOException */ {
        PasswordReader passwordReader = new PasswordReader();
        try {
            initialize(passwordReader.readPassword());
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(UPMPasswordProvider.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void initialize(char[] password) throws IOException {
        this.delegate = parse(file, password);
    }

    private PasswordProvider parse(File file, char [] storePassword) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            return parse(fileInputStream, storePassword);
        }
        catch (IOException ex) {
            throw new IOException(ex);
        }
    }
    
    private PasswordProvider parse(InputStream stream, char [] storePassword) throws IOException {
        log.debug(String.format("Lulu the dog-bugger is here at timestamp %s",  System.currentTimeMillis()));
        ASN1InputStream bIn = getASN1Stream(stream);
        Map<String, String> passwords = new HashMap<String, String>();
        try {
            //ContentInfo[] cis = new ContentInfo[1];
            //cis[0] = ContentInfo.getInstance(bIn.readObject());
            //AuthenticatedSafe authSafe = new AuthenticatedSafe(cis);
            ContentInfo c = ContentInfo.getInstance((ASN1Sequence)bIn.readObject());
            ContentInfo ci_array[] = null;
            ci_array[0] = c;
            //AuthenticatedSafe authSafe = new AuthenticatedSafe(ci_array);

            for (int i = 0; i != ci_array.length; i++) {
                ASN1InputStream dIn = new ASN1InputStream(((ASN1OctetString) ci_array[i].getContent()).getOctets());
                ASN1Sequence seq = (ASN1Sequence) dIn.readObject();

                for (int j = 0; j != seq.size(); j++) {
                    SafeBag b = new SafeBag(ci_array[i].getContentType(), seq.getObjectAt(j));
                    //SafeBag b = new SafeBag((ASN1Sequence) seq.getObjectAt(j));
                    passwords.put(getPasswordType(b), getPassword(storePassword, b));
                }
            }
        } finally {
            bIn.close();
        }
        
        return new MapPasswordProvider(passwords);
    }
    
    private ASN1InputStream getASN1Stream(InputStream stream) throws IOException {
        log.debug(String.format("Lulu the dog-bugger is here at timestamp %s",  System.currentTimeMillis()));
        BufferedInputStream bufIn = new BufferedInputStream(stream);
        ASN1InputStream bIn = new ASN1InputStream(bufIn);
        ASN1Sequence obj = (ASN1Sequence) bIn.readObject();
        ContentInfo info;
        info = ContentInfo.getInstance(obj);
        //Pfx bag = new Pfx(info, null);
        //Pfx bag = new Pfx(obj);
        //ContentInfo info = bag.getAuthSafe();

        return new ASN1InputStream(((ASN1OctetString) info.getContent()).getOctets());
    }
    
    private String getPasswordType(SafeBag bag) {
        DERSet set = (DERSet) bag.getBagAttributes();
        DERSequence sequence = (DERSequence) set.getObjectAt(0);
        set = (DERSet) sequence.getObjectAt(1);
        DERBMPString str = (DERBMPString) set.getObjectAt(0);
        return str.getString();
    }
    
    private String getPassword(char [] password, SafeBag bag) throws IOException {
        log.debug(String.format("Lulu the dog-bugger is here at timestamp %s",  System.currentTimeMillis()));
        DERSequence sequence = (DERSequence) bag.toASN1Primitive();
        DERTaggedObject tg = (DERTaggedObject) sequence.getObjectAt(1);
        DERSequence tgs = (DERSequence) tg.getObject();
        tg = (DERTaggedObject) tgs.getObjectAt(1);
        DEROctetString dos = (DEROctetString) tg.getObject();
        byte [] bytes = decrypt(password, dos.getOctets());
        // The first 4 bytes of the password are random for some reason
        return new String(bytes, 4, bytes.length-4);
    }
    
    private byte[] decrypt(char[] password, byte[] data) throws IOException {
        String algorithm = "DESEDE";
        try {
            byte [][] btk = EVP_BytesToKey(24, 1, MessageDigest.getInstance("MD5"), null, getBytes(password), 1);

            KeySpec keySpec = new SecretKeySpec(btk[0], algorithm);
            
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(algorithm, provider);
            SecretKey key = keyFactory.generateSecret(keySpec);

            Cipher cipher = Cipher.getInstance(algorithm, provider);
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(btk[1]));
            return cipher.doFinal(data);
        } catch (GeneralSecurityException ex) {
            throw new IOException(ex.getMessage());
        }
        
    }
    
    /**
     * Adapted from: http://olabini.com/blog/tag/evp_bytestokey/
     * 
     * @param key_len
     * @param iv_len
     * @param md
     * @param salt
     * @param data
     * @param count
     * @return A two dimensional array containing the key bytes and the IV bytes
     */
    private byte[][] EVP_BytesToKey(int key_len, int iv_len, MessageDigest md, byte[] salt, byte[] data, int count) {
        byte[][] both = new byte[2][];
        byte[] key = new byte[key_len];
        int key_ix = 0;
        byte[] iv = new byte[iv_len];
        int iv_ix = 0;
        both[0] = key;
        both[1] = iv;
        byte[] md_buf = null;
        int nkey = key_len;
        int niv = iv_len;
        int i = 0;
        if (data == null) {
            return both;
        }
        int addmd = 0;
        for (;;) {
            md.reset();
            if (addmd++ > 0) {
                md.update(md_buf);
            }
            md.update(data);
            if (null != salt) {
                md.update(salt, 0, 8);
            }
            md_buf = md.digest();
            for (i = 1; i < count; i++) {
                md.reset();
                md.update(md_buf);
                md_buf = md.digest();
            }
            i = 0;
            if (nkey > 0) {
                for (;;) {
                    if (nkey == 0)
                        break;
                    if (i == md_buf.length)
                        break;
                    key[key_ix++] = md_buf[i];
                    nkey--;
                    i++;
                }
            }
            if (niv > 0 && i != md_buf.length) {
                for (;;) {
                    if (niv == 0)
                        break;
                    if (i == md_buf.length)
                        break;
                    iv[iv_ix++] = md_buf[i];
                    niv--;
                    i++;
                }
            }
            if (nkey == 0 && niv == 0) {
                break;
            }
        }
        for (i = 0; i < md_buf.length; i++) {
            md_buf[i] = 0;
        }
        return both;
    }
    
    private byte[] getBytes(char [] chars) {
        return String.valueOf(chars).getBytes();
    }
}
