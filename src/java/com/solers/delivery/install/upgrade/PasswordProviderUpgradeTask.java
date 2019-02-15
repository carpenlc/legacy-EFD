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
package com.solers.delivery.install.upgrade;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.ArrayUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.rsa.jsafe.crypto.CryptoJ;
import com.rsa.jsafe.crypto.JSAFE_InvalidUseException;
import com.rsa.jsafe.provider.JsafeJCE;
import com.solers.delivery.security.PasswordType;
import com.solers.security.password.DefaultPasswordProvider;
import com.solers.security.password.PasswordProvider;
import com.solers.util.IOConsole;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class PasswordProviderUpgradeTask extends Task {
    
    private static final int KEY_ITERATIONS = 1000;
    private static final int SALT_SIZE = 8;
    private static final String DEFAULT_PBE_ALGORITHM = "PBEWithMD5AndTripleDES";
    
    private String fileName;
    private char[] password;

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    protected void setPassword(char[] password) {
        this.password = password;
    }

    @Override
    public void execute() throws BuildException {
        Map<String, String> passwords = getCurrentPasswords();
        
        File passwordFile = new File(fileName);
        passwordFile.delete();
        
        initializeProvider();
        PasswordProvider provider = createProvider(password, passwordFile);
        for (Map.Entry<String, String> e: passwords.entrySet()) {
            provider.setPassword(e.getKey(), e.getValue());
        }
        removeProvider();
    }
    
    protected void removeProvider() {
        try {
            Security.removeProvider(new JsafeJCE().getName());
            CryptoJ.setMode(CryptoJ.NOT_INITIALIZED);
        } catch (JSAFE_InvalidUseException ex) {
            throw new BuildException(ex);
        }
    }
    
    protected void initializeProvider() {
        try {
            CryptoJ.setMode(CryptoJ.FIPS140_MODE);
            Security.insertProviderAt(new JsafeJCE(), 1);
        } catch (JSAFE_InvalidUseException ex) {
            throw new BuildException(ex);
        }
    }
    
    protected PasswordProvider createProvider(char [] password, File passwordFile) {
        return new DefaultPasswordProvider(password, passwordFile);
    }

    protected Map<String, String> getCurrentPasswords() {
        Properties properties = readProperties();
        readMasterPassword();
        Map<String, String> result = new HashMap<String, String>();
        for (String encryptedKey : properties.stringPropertyNames()) {
            String key = decrypt(encryptedKey, password);
            String value = decrypt(properties.getProperty(encryptedKey), password);
            result.put(key, value);
        }
        return result;
    }
    
    protected Properties readProperties() {
        Properties properties = new Properties();
        File passwordFile = new File(fileName);
        try {
            FileReader reader = new FileReader(passwordFile);
            try {
                properties.load(reader);
            } finally {
                reader.close();
            }
        } catch (IOException ex) {
            throw new BuildException("Could not read properties", ex);
        }
        return properties;
    }
    
    protected void readMasterPassword() {
        File passwordFile = new File(fileName);
        char [] masterPassword = IOConsole.DEFAULT.readPassword("master", false);
        boolean verified = false;
        
        while (!verified) {
            verified = verifyMasterPassword(passwordFile, masterPassword, IOConsole.DEFAULT);
            if (!verified) {
                IOConsole.DEFAULT.println("Incorrect master password");
                masterPassword = IOConsole.DEFAULT.readPassword("master", false);
            }
        }
        setPassword(masterPassword);
    }
    
    protected String decrypt(String cipherText, char[] password) {
        if (cipherText == null) {
            return null;
        }
        
        Base64 base64 = new Base64();
        try {
            byte [] bytes = base64.decode(cipherText.getBytes("US-ASCII"));
            byte [] plaintext = crypt(Cipher.DECRYPT_MODE, bytes, password);
            return new String(plaintext);
        } catch (UnsupportedEncodingException ex) {
            throw new BuildException(ex);
        } catch (GeneralSecurityException ex) {
            throw new BuildException(ex);
        }
    }
    
    private String encrypt(String plaintext, char [] password) {
        Base64 base64 = new Base64();
        try {
            byte [] ciphertext = crypt(Cipher.ENCRYPT_MODE, plaintext.getBytes(), password);
            return new String(base64.encode(ciphertext), "US-ASCII");
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        } catch (GeneralSecurityException ex) {
            throw new BuildException(ex);
        }
    }
    
    private byte [] crypt(int mode, byte [] message, char [] password) throws GeneralSecurityException {
        PBEKeySpec keySpec = new PBEKeySpec(password);
        try {
            PBEParameterSpec parameterSpec = new PBEParameterSpec(salt(), KEY_ITERATIONS);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DEFAULT_PBE_ALGORITHM);
            SecretKey key = keyFactory.generateSecret(keySpec);
            
            Cipher cipher = Cipher.getInstance(DEFAULT_PBE_ALGORITHM);
            cipher.init(mode, key, parameterSpec);
            return cipher.doFinal(message);
        } finally {
            keySpec.clearPassword();
        }
    }
    
    private byte[] salt() {
        return ArrayUtils.subarray(System.class.getName().getBytes(), 0 , SALT_SIZE);
    }
    
    private boolean verifyMasterPassword(File pFile, char[] masterPassword, IOConsole console) {
        if (checkPassword(masterPassword, console)) {
            Properties properties = readProperties();
            String value = properties.getProperty(encrypt(PasswordType.DATABASE.key(), masterPassword));
            return value != null;
        } else {
            console.println("Cannot retreive the Password File: " + pFile.getAbsolutePath());
        }
        return false;
    }
    
    private boolean checkPassword(char[] password, IOConsole console) {
        String pw = new String(password);
        boolean valid = (pw != null && pw.trim().length() > 0);
        if (!valid) {
            console.println("Password cannot be null or empty!");
        }
        return valid;
    }
}
