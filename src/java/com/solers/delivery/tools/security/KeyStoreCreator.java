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
package com.solers.delivery.tools.security;

import java.io.File;

import com.solers.delivery.install.PasswordProviderConsole;
import com.solers.delivery.security.PasswordType;
import com.solers.delivery.util.FileSystemUtil;
import com.solers.security.KeyStoreGenerator;
import com.solers.util.IOConsole;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public final class KeyStoreCreator {
    
    public static void main(String [] args) {
        main(IOConsole.DEFAULT);
    }
    
    public static void main(IOConsole console) {
        String keyPassword = new String(PasswordProviderConsole.readPassword(console, PasswordType.PKI_KEY.prompt(), false));
        String keyStorePassword = new String(PasswordProviderConsole.readPassword(console, PasswordType.PKI_KEYSTORE.prompt(), false));
        String trustStorePassword = new String(PasswordProviderConsole.readPassword(console, PasswordType.PKI_TRUSTSTORE.prompt(), false));
        
        File privateKey = readPath(console, "Enter path to private key (PEM format)");
        File certChain = readPath(console, "Enter path to cert chain (PEM format)");
        
        KeyStoreGenerator generator = new KeyStoreGenerator(privateKey, certChain);
        
        try {
            generator.saveKeyStore(new File("portal_keystore.p12"), keyStorePassword.toCharArray(), keyPassword.toCharArray());
            System.out.println("Created portal_keystore.p12");
            generator.saveTrustStore(new File("portal_truststore.jks"), trustStorePassword.toCharArray());
            System.out.println("Created portal_truststore.jks");
        } catch (Exception ex) {
            System.err.println("An error occurred: "+ex.getMessage());
        }
    }
    
    private static File readPath(IOConsole console, String prompt) {
        File result = null;
        boolean valid = false;
        do {
            String input = console.readLine(prompt);
            if (!empty(input)) {
                result = new File(input).getAbsoluteFile();
                if (result.exists()) {
                    if (result.isFile()) {
                        if (FileSystemUtil.isReadable(result)) {
                            valid = true;
                        } else {
                            console.println("Cannot read path");
                        }
                    } else {
                        console.println("Path must be a file");
                    }
                } else {
                    console.println("Path must exist");
                }
            }
        } while (!valid);
        return result;
    }
    
    private static boolean empty(String input) {
        return input == null || input.length() == 0;
    }
}
