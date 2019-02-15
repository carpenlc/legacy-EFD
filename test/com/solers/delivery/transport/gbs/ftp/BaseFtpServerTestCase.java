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
package com.solers.delivery.transport.gbs.ftp;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;

import com.solers.delivery.security.PasswordType;
import com.solers.delivery.transport.gbs.GBSConfigurator;
import com.solers.security.password.MapPasswordProvider;
import com.solers.security.password.PasswordProvider;
import com.solers.security.ssl.SSLContextCreator;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public abstract class BaseFtpServerTestCase {
    
    protected File root;
    protected File ftpDir;
    protected File downloadDir;
    protected PasswordProvider provider;
    protected FTPServerWrapper server;
    protected Properties properties;
    
    @Before
    public void setUp() {
        Set<String> suites = new HashSet<String>();
        suites.add("TLS_RSA_WITH_AES_128_CBC_SHA");
        SSLContextCreator creator = new SSLContextCreator("password", 
            "password", 
            "password",
            "dev/security/portal_keystore.p12",
            "dev/security/portal_truststore.jks",
            suites,
            true,
            5000, new SecureRandom());
        creator.init();
        
        root = new File(".", getClass().getName());
        root.mkdirs();
        ftpDir = new File(root, "server");
        ftpDir.mkdirs();
        downloadDir = new File(root, "download");
        downloadDir.mkdirs();
        
        provider = new MapPasswordProvider();
        provider.setPassword(PasswordType.FTP.key(), "password");
        
        properties = new Properties();
        properties.put("user.name", "efd");
        properties.put("user.dir", ftpDir.getAbsolutePath());
        properties.put("user.archive", downloadDir.getAbsolutePath());
        properties.put("connection-config.anonymousLoginEnabled", "false");
        properties.put("connection.port", String.valueOf(getPort()));
        properties.put("data-connection.idle-time", "10");
        properties.put("data-connection.active.local-port", "0");
        properties.put("data-connection.passive.ports", "20020-21020");
        properties.put("data-connection.implicitSsl", "true");
        properties.put("listener.implicitSsl", "true");
        
        GBSConfigurator config = new GBSConfigurator(properties, provider);
        GBSConfigurator.setGBSEnabled(true);
        
        server = new FTPServerWrapper(config, true);
    }
    
    @After
    public void tearDown() throws IOException {
        server.stop();
        FileUtils.deleteDirectory(root);
    }
    
    protected int getPort() {
        return 9989;
    }
    
}
