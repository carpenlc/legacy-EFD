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
package com.solers.delivery;

import java.io.File;
import java.util.Properties;

import com.solers.delivery.security.PasswordType;
import com.solers.delivery.tools.ClTools;
import com.solers.security.password.MapPasswordProvider;
import com.solers.util.db.DerbySqlExecutor;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class SetupDevelopmentUsers {
    
    public static void main(String [] args) throws Exception {
        int port = 9010;
        File securityDirectory = new File(".", "dev/security");
        MapPasswordProvider provider = new MapPasswordProvider();
        provider.setDefaultPassword("password");
        
        String url = String.format("jdbc:derby://127.0.0.1:%d/content_delivery;ssl=peerAuthentication", port);
        Properties p = new Properties();
        p.setProperty("user", "sa");
        p.setProperty("password", provider.getPassword(PasswordType.ROOT_DATABASE.key()));
        ClTools.initializeSSL(provider, securityDirectory);
        
        DerbySqlExecutor tool = new DerbySqlExecutor(url, p);
        
        tool.execute("SET SCHEMA EFD");
        try {
            tool.execute("update password set password='f474356109f0612bee5789608c671b6cba843e8a' where id=1");
            tool.execute("insert into users (enabled, username, lastlogin, failedlogins, adminrole, initialpassword, firstname, lastname) values ( 1, 'user', CURRENT_TIMESTAMP, 0, 0, 0, 'default', 'user')");
            tool.execute("insert into password (password, createddate, users, admincreated) values ('f474356109f0612bee5789608c671b6cba843e8a', CURRENT_TIMESTAMP, 2, 1)");
        } finally {
            tool.close();
        }
        System.out.println("Complete");
    }
    
}
