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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;

import com.solers.security.EncryptionUtils;

public class DefaultPasswordProvider implements PasswordProvider {

    private final Properties props;
    private final File file;
    private final Base64 base64;
    
    private char [] password;

    public DefaultPasswordProvider(File file) {
        props = new Properties();
        base64 = new Base64();
        this.file = file;
        load();
    }
    
    public DefaultPasswordProvider(char[] password, File file) {
        this(file);
        initialize(password);
    }
    
    public void initialize() {
        PasswordReader passwordReader = new PasswordReader();
        initialize(passwordReader.readPassword());
    }
    
    public void initialize(char[] password) {
        Map<String, String> current = currentPasswords();
        if (this.password != null) {
            props.clear();
        }
        
        this.password = Arrays.copyOf(password, password.length);
        
        for (Map.Entry<String, String> e : current.entrySet()) {
            setPassword(e.getKey(), e.getValue());
        }
        current.clear();
    }
    
    public String getPassword(String type) {
        return decrypt(getProperty(encrypt(type)));
    }

    public void setPassword(String key, String value) {
        setProperty(encrypt(key), encrypt(value));
        save();
    }
    
    protected String getProperty(String key) {
        return props.getProperty(key);
    }
    
    protected void setProperty(String key, String value) {
        props.setProperty(key, value);
    }
    
    protected void removeProperty(String key) {
        props.remove(key);
    }
    
    protected byte [] b64encode(byte [] bytes) {
        return base64.encode(bytes);
    }
    
    protected byte [] b64decode(byte [] bytes) {
        return base64.decode(bytes);
    }

    private void save() {
        try {
            FileWriter writer = new FileWriter(file);
            try {
                props.store(writer, "Encryptyed Password File");
            } finally {
                writer.close();
            }
        } catch (IOException ex) {
            throw new RuntimeException("Could not save properties", ex);
        }
    }

    private void load()  {
        if (!file.exists()) {
            return;
        }
        try {
            FileReader reader = new FileReader(file);
            try {
                props.load(reader);
            } finally {
                reader.close();
            }
        } catch (IOException ex) {
            throw new RuntimeException("Could not read properties", ex);
        }
    }

    private String encrypt(String plaintext) {
        byte [] ciphertext = EncryptionUtils.encrypt(this.password, plaintext.getBytes());
        
        try {
            return new String(base64.encode(ciphertext), "US-ASCII");
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    private String decrypt(String cipherText) {
        if (cipherText == null) {
            return null;
        }
        
        try {
            byte [] bytes = base64.decode(cipherText.getBytes("US-ASCII"));
            byte [] plaintext = EncryptionUtils.decrypt(this.password, bytes);
            return new String(plaintext);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private Map<String, String> currentPasswords() {
        Map<String, String> result = new HashMap<String, String>(props.size());
        if (this.password != null) {
            for (String encryptedKey : props.stringPropertyNames()) {
                String key = decrypt(encryptedKey);
                String value = getPassword(key);
                result.put(key, value);
            }
        }
        return result;
    }
}
