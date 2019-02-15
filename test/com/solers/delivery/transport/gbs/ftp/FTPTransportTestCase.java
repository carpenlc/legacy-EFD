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
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import com.solers.delivery.domain.FtpConnection;
import com.solers.delivery.transport.gbs.TransportException;
import com.solers.delivery.transport.gbs.TransportType;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class FTPTransportTestCase extends BaseFtpServerTestCase {
    
    @Test
    public void testActiveFtpsWithImplicitSsl() throws Exception {
        FtpConnection connection = createBaseConnection();
        connection.setPassive(false);
        connection.setImplicit(true);
        
        sendFile(connection);
    }
    
    @Test
    public void testActiveFtpsWithExplicitSsl() throws Exception {
        properties.put("data-connection.implicitSsl", "false");
        properties.put("listener.implicitSsl", "false");
        
        FtpConnection connection = createBaseConnection();
        connection.setPassive(false);
        connection.setImplicit(false);
        
        sendFile(connection);
    }
    
    @Test
    public void testPassiveFtpsWithExplicitSsl() throws Exception {
        properties.put("data-connection.implicitSsl", "false");
        properties.put("listener.implicitSsl", "false");
        
        FtpConnection connection = createBaseConnection();
        connection.setPassive(true);
        connection.setImplicit(false);
        
        sendFile(connection);
    }
    
    @Test
    public void testPassiveFtpsWithImplicitSsl() throws Exception {
        FtpConnection connection = createBaseConnection();
        connection.setPassive(true);
        connection.setImplicit(true);
        
        sendFile(connection);
    }
    
    private FtpConnection createBaseConnection() throws UnknownHostException {
        FtpConnection connection = new FtpConnection();
        connection.setUsername("efd");
        connection.setPassword("password");
        connection.setPort(String.valueOf(getPort()));
        connection.setHost(InetAddress.getLocalHost().getHostName());
        connection.setDirectory("");
        return connection;
    }
    
    private void sendFile(FtpConnection connection) throws IOException, TransportException {
        Assert.assertEquals(0, downloadDir.listFiles().length);
        server.start();
        
        String filename = getClass().getSimpleName();
        String content = filename + "content";
        
        File file = new File(".", filename);
        file.deleteOnExit();
        FileUtils.writeStringToFile(file, content);
        
        FTPTransport transport = new FTPTransport(TransportType.FTPS, connection);
        transport.init();
        transport.upload(file);
        
        Assert.assertEquals(1, downloadDir.listFiles().length);
        Assert.assertEquals(filename, downloadDir.listFiles()[0].getName());
        Assert.assertEquals(content, FileUtils.readFileToString(downloadDir.listFiles()[0]));
    }
    
}
