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
package com.solers.delivery.tools;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import com.solers.delivery.install.NativePasswordProviderConsole;
import com.solers.delivery.install.PasswordProviderConsole;
import com.solers.delivery.security.PasswordType;
import com.solers.delivery.util.password.UserPasswordEncoder;
import com.solers.security.password.MapPasswordProvider;
import com.solers.security.password.PasswordProvider;
import com.solers.util.IOConsole;
import com.solers.util.db.DerbySqlExecutor;

public class UnlockAdminAccount {
    
    public static void main(String [] args) {
        ClTools.initializeJsafeProvider();
        try {
            File securityDirectory = getSecurityDirectory(args);                      
            int port = getPort(args);
            PasswordProvider provider = getProvider(args, securityDirectory);
            String username = getUsername(args);
            String newPassword = getNewPassword();
            unlockAdminAccount(securityDirectory, port, provider, username, newPassword);
        } catch (UsageException ex) {
            usage(ex.getMessage());
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }
    
    private static void unlockAdminAccount(File securityDirectory, int port, PasswordProvider provider, String username, String pw) {
        DerbySqlExecutor tool = null;
        Statement stmt = null;
        ResultSet result = null;
        String url = String.format("jdbc:derby://127.0.0.1:%d/content_delivery;ssl=peerAuthentication", port);
        Properties p = new Properties();
        p.setProperty("user", "sa");
        p.setProperty("password", provider.getPassword(PasswordType.ROOT_DATABASE.key()));
        ClTools.initializeSSL(provider, securityDirectory);
        try {
            tool = new DerbySqlExecutor(url, p);
            tool.execute("SET SCHEMA EFD");
            Connection conn = tool.getConnection();            
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            String sql = String.format("select id from users where adminrole=1 and failedlogins >= 3 and enabled=0 and username='%s'", username);                        
            result = stmt.executeQuery(sql);            
            if (result.first()) {                
                long id = result.getLong(1);            
                tool.execute(String.format("insert into password (password, createddate, users) values ('%s', CURRENT_TIMESTAMP, %d)", pw, id));
                tool.execute(String.format("update users set initialpassword=0, enabled=1, failedlogins=0 where id=%d", id));
            } else {
                System.out.println("Unable to unlock the admin account.");
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                if (result != null) {
                    result.close();
                }
            } catch (SQLException ex) {
                System.err.println(ex.getMessage());
            }
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException ex) {
                System.err.println(ex.getMessage());
            }
            try {
                if (tool != null) {
                    tool.close();
                }
            } catch (SQLException ex) {
                System.err.println(ex.getMessage());
            }            
        }         
    }
    
    private static File getSecurityDirectory(String [] args) throws UsageException {
        File result = new File(args[0]);                        
        if (!result.exists()) {
            throw new UsageException("Security directory does not exist");
        }
        return result;
    }
    
    private static int getPort(String [] args) throws UsageException {
        try {
            return Integer.parseInt(args[1]);
        } catch(NumberFormatException ex) {
            throw new UsageException("Port is invalid");
        }
    }
    
    private static PasswordProvider getProvider(String [] args, File securityDirectory) {
        if (args[2].equals("test")) {
            MapPasswordProvider p = new MapPasswordProvider();
            p.setDefaultPassword("password");
            return p;
        }
        
        PasswordProviderConsole providerConsole = null;
        if (args.length == 4) {
            providerConsole = new NativePasswordProviderConsole();
        } else {
            providerConsole = new PasswordProviderConsole();
        }
        
        File passwordFile = new File(securityDirectory, "master_pwd.properties");
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
    
    private static String getUsername(String [] args) {
        if (args.length == 4 && args[2] != null && args[2].length() > 0) {
            return args[3].trim();
        } else {
            return args[2].trim();
        }
    }
    
    private static String getNewPassword() {
        char [] pw = PasswordProviderConsole.readPassword(IOConsole.DEFAULT, "new", true);
        String newPassword = String.valueOf(pw);
        String hashedPw = new UserPasswordEncoder().encodePassword(newPassword);
        return hashedPw;
    }
    
    private static void usage(String message) {
        System.err.println(message);
        System.err.println("Usage: java "+UnlockAdminAccount.class.getName()+" <security directory> <port> [native] [username]");
    }
}
