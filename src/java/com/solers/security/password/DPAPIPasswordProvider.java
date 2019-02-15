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

import net.sourceforge.jdpapi.DataProtector;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class DPAPIPasswordProvider extends DefaultPasswordProvider {

    private static final String MASTER_KEY = "master";
    
    public DPAPIPasswordProvider(char [] password, File file) {
        super(password, file);
    }
    
    public DPAPIPasswordProvider(File file) {
        super(file);
    }

    @Override
    public void initialize() {
        super.initialize(getMasterPassword());
    }

    @Override
    public void initialize(char[] password) {
        removeProperty(MASTER_KEY);
        super.initialize(password);
        setMasterPassword(password);
    }

    private void setMasterPassword(char [] password) {
        DataProtector dpapi = new DataProtector();
        byte [] encryptedBytes = dpapi.protect(String.valueOf(password));
        String encrypted = new String(b64encode(encryptedBytes));
        setProperty(MASTER_KEY, encrypted);
    }
    
    private char[] getMasterPassword() {
        DataProtector dpapi = new DataProtector();
        String encrypted = getProperty(MASTER_KEY);
        return dpapi.unprotect(b64decode(encrypted.getBytes())).toCharArray();
    }
    
    static {
        System.loadLibrary("jdpapi-native-1.0.1");
    }
}
