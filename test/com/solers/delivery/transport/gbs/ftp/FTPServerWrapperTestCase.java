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

import java.io.ByteArrayInputStream;
import java.net.InetAddress;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.ftp.FTPSSocketFactory;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:kevin.conaway@solers.com, "Kevin Conaway</a>
 */
public class FTPServerWrapperTestCase extends BaseFtpServerTestCase {
    
    @Test
    public void testImplicitSsl() throws Exception {
        Assert.assertEquals(0, downloadDir.listFiles().length);
        
        server.start();
        
        FTPSClient client = new FTPSClient(true);
        client.connect(InetAddress.getLocalHost(), getPort());
        try {
            client.login("efd", "password");
            client.execPBSZ(0);
            client.pwd();
            client.enterLocalPassiveMode();
            client.setSocketFactory(new FTPSSocketFactory(SSLContext.getDefault()));
            client.setServerSocketFactory(SSLServerSocketFactory.getDefault());
            client.setFileType(FTPSClient.BINARY_FILE_TYPE);
            client.storeFile("fileName", new ByteArrayInputStream("foobar".getBytes()));
        } finally {
            client.logout();
            client.disconnect();
        }
        
        Assert.assertEquals(1, downloadDir.listFiles().length);
        Assert.assertEquals("fileName", downloadDir.listFiles()[0].getName());
        Assert.assertEquals("foobar", FileUtils.readFileToString(downloadDir.listFiles()[0]));
    }
    
}
