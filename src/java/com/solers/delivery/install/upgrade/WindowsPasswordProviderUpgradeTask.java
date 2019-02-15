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

import net.sourceforge.jdpapi.DataProtector;

import org.apache.commons.codec.binary.Base64;

import com.solers.delivery.install.PasswordProviderConsole;
import com.solers.security.password.DPAPIPasswordProvider;
import com.solers.security.password.PasswordProvider;
import com.solers.util.IOConsole;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class WindowsPasswordProviderUpgradeTask extends PasswordProviderUpgradeTask {
    
    @Override
    protected PasswordProvider createProvider(char[] password, File passwordFile) {
        return new DPAPIPasswordProvider(password, passwordFile);
    }
    
    @Override
    protected String decrypt(String message, char [] password) {
        Base64 base64 = new Base64();
        DataProtector dpapi = new DataProtector();
        byte [] data = base64.decode(message.getBytes());
        return dpapi.unprotect(data);
    }
    
    @Override
    protected void readMasterPassword() {
        char [] masterPassword = PasswordProviderConsole.readPassword(IOConsole.DEFAULT, "Master", true);
        setPassword(masterPassword);
    }
    
    static {
        System.loadLibrary("jdpapi-native-1.0.1");
    }
}
