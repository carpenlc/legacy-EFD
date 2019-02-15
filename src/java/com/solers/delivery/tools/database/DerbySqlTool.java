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
package com.solers.delivery.tools.database;

import java.io.File;
import java.sql.SQLException;
import java.util.Properties;

import com.solers.delivery.install.NativePasswordProviderConsole;
import com.solers.delivery.install.PasswordProviderConsole;
import com.solers.delivery.security.PasswordType;
import com.solers.delivery.tools.ClTools;
import com.solers.security.password.MapPasswordProvider;
import com.solers.security.password.PasswordProvider;
import com.solers.util.IOConsole;
import com.solers.util.db.DerbySqlExecutor;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class DerbySqlTool {
    
    public static void main(String [] args) {                      
        ClTools.initializeJsafeProvider();
        
        int port = -1;
        PasswordProvider provider = null;
        
        File securityDirectory = new File(args[0]);
        if (!securityDirectory.exists()) {
            usage("Security directory does not exist");
            return;
        }
        
        try {
            port = Integer.parseInt(args[1]);
        } catch(NumberFormatException ex) {
            usage("Port is invalid");
            return;
        }
        
        File passwordFile = new File(securityDirectory, "master_pwd.properties");
        if (args.length == 3 && args[2] != null && args[2].length() > 0) {
            if (args[2].equals("test")) {
                MapPasswordProvider p = new MapPasswordProvider();
                p.setDefaultPassword("password");
                provider = p;
            } else {
                provider = getPasswordProvider(new NativePasswordProviderConsole(), passwordFile);
            }
        } else {
            provider = getPasswordProvider(new PasswordProviderConsole(), passwordFile);
        }
        ClTools.initializeSSL(provider, securityDirectory);
        
        String url = String.format("jdbc:derby://127.0.0.1:%d/content_delivery;ssl=peerAuthentication", port);
        Properties p = new Properties();
        p.setProperty("user", "sa");
        p.setProperty("password", provider.getPassword(PasswordType.ROOT_DATABASE.key()));
        loop(url, p);
    }
    
    private static void loop(String jdbcUrl, Properties jdbcProperties) {
        DerbySqlExecutor executor = null;
        try {
            executor = new DerbySqlExecutor(jdbcUrl, jdbcProperties);
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        
        if (executor == null) {
            return;
        }
        
        try {
            executor.execute("SET SCHEMA EFD");
            executor.loop();
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        
    }
    
    private static PasswordProvider getPasswordProvider(PasswordProviderConsole providerConsole, File passwordFile) {
        char [] masterPassword = IOConsole.DEFAULT.readPassword("Master", false);
        boolean verified = false;
        
        while (!verified) {
            verified = providerConsole.verifyMasterPassword(passwordFile.getAbsolutePath(), masterPassword, IOConsole.DEFAULT);
            if (!verified) {
                IOConsole.DEFAULT.println("Incorrect master password");
                masterPassword = IOConsole.DEFAULT.readPassword("Master", false);
            }
        }
        
        return providerConsole.getInstance(masterPassword, passwordFile);
    }
    
    private static void usage(String msg) {
        System.err.println(msg);
        System.err.println("Usage: java "+DerbySqlTool.class.getName()+" <security directory> <port> [native]");
    }
}
