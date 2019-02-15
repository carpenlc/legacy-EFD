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
package com.solers.delivery.domain;

import java.io.File;
import java.io.IOException;

import com.solers.delivery.transport.gbs.GBSConfigurator;
import com.solers.delivery.util.FileSystemUtil;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class ContentSetValidationTestCase extends BaseValidationTestCase {
    
    public void setUp() {
        System.setProperty("efd.home", ".");
    }
    
    public void testValidContentSet() throws IOException {
        assertValidContentSet(new ContentSet());
    }
    
    public void testValidContentSetInventoryInterval() {
        assertValidInventoryInterval(new ContentSet());
    }
    
    public void testValidConsumerContentSetInventoryInterval() {
        assertValidInventoryInterval(new ConsumerContentSet());
    }
    
    public void testValidContentSetName() {
        assertValidName(new ContentSet());
    }
    
    public void testValidConsumerContentSetName() {
        assertValidName(new ConsumerContentSet());
    }
    
    public void testValidContentSetPath() throws Exception {
        assertValidPath(new ContentSet());
    }
    
    public void testValidConsumerContentSetPath() throws Exception {
        assertValidPath(new ConsumerContentSet());
    }
    
    public void testValidFtpConnection() {
        FtpConnection conn = new FtpConnection();
        ContentSet contentSet = new ContentSet();
        contentSet.setFtpConnection(conn);
        
        assertInvalidProperty(contentSet, "host");
        assertInvalidProperty(contentSet, "port");
        assertInvalidProperty(contentSet, "username");
        assertInvalidProperty(contentSet, "password");
        assertInvalidProperty(contentSet, "directory");
        
        conn.setHost("127.0.0.1");
        assertValidProperty(contentSet, "host");
        assertInvalidProperty(contentSet, "port");
        assertInvalidProperty(contentSet, "username");
        assertInvalidProperty(contentSet, "password");
        assertInvalidProperty(contentSet, "directory");
        
        conn.setPort("21");
        assertValidProperty(contentSet, "host");
        assertValidProperty(contentSet, "port");
        assertInvalidProperty(contentSet, "username");
        assertInvalidProperty(contentSet, "password");
        assertInvalidProperty(contentSet, "directory");
        
        conn.setUsername("test");
        assertValidProperty(contentSet, "host");
        assertValidProperty(contentSet, "port");
        assertValidProperty(contentSet, "username");
        assertInvalidProperty(contentSet, "password");
        assertInvalidProperty(contentSet, "directory");
        
        conn.setPassword("pass");
        assertValidProperty(contentSet, "host");
        assertValidProperty(contentSet, "port");
        assertValidProperty(contentSet, "username");
        assertValidProperty(contentSet, "password");
        assertInvalidProperty(contentSet, "directory");
        
        conn.setDirectory("dir");
        assertValidProperty(contentSet, "host");
        assertValidProperty(contentSet, "port");
        assertValidProperty(contentSet, "username");
        assertValidProperty(contentSet, "password");
        assertValidProperty(contentSet, "directory");
    }
    
    protected void assertValidPath(ContentSet contentSet) throws IOException {
        File efdHome = FileSystemUtil.getEFDHome();
        
        assertInvalidProperty(contentSet, "path");
        
        File invalidPath = new File(efdHome, "child");
        invalidPath.deleteOnExit();
        
        contentSet.setPath(invalidPath.getAbsolutePath());
        assertInvalidProperty(contentSet, "path");
        
        // See comments in ValidFileNameValidatorTestCase
        if (System.getProperty("os.name").contains("Windows")) {
            contentSet.setPath("path:");
            assertInvalidProperty(contentSet, "path");
        }
        
        File validPath = File.createTempFile("testValidPath", "data");
        validPath.deleteOnExit();
        contentSet.setPath(validPath.getAbsolutePath());
        assertValidProperty(contentSet, "path");
        
        validPath.delete();
    }
    
    protected void assertValidName(ContentSet contentSet) {
        assertInvalidProperty(contentSet, "name");
        
        contentSet.setName("");
        assertInvalidProperty(contentSet, "name");
        
        contentSet.setName("foo bar99");
        assertInvalidProperty(contentSet, "name");
        
        contentSet.setName("#foo_bar99");
        assertInvalidProperty(contentSet, "name");
        
        contentSet.setName(".foo_bar99");
        assertInvalidProperty(contentSet, "name");
        
        contentSet.setName("foo");
        assertValidProperty(contentSet, "name");
        
        contentSet.setName("foo-bar99");
        assertValidProperty(contentSet, "name");
        
        contentSet.setName("foo_bar99");
        assertValidProperty(contentSet, "name");

        String eightyCharsName = "12345678901234567890123456789012345678901234567890123456789012345678901234567890";

        contentSet.setName(eightyCharsName);
        assertValidProperty(contentSet, "name");
        
        contentSet.setName(eightyCharsName + "1");
        assertInvalidProperty(contentSet, "name");
    }
    
    protected void assertValidInventoryInterval(ContentSet contentSet) {
        assertInvalidProperty(contentSet, "inventoryInterval");
        
        contentSet.setInventoryInterval(-1);
        assertInvalidProperty(contentSet, "inventoryInterval");
        
        contentSet.setInventoryInterval(0);
        assertInvalidProperty(contentSet, "inventoryInterval");
        
        contentSet.setInventoryInterval(100);
        assertValidProperty(contentSet, "inventoryInterval");
    }
    
    protected void assertValidContentSet(ContentSet contentSet) throws IOException {
        File validPath = File.createTempFile("testValidContentSet", "data");
        validPath.deleteOnExit();
        
        contentSet.setName("valid-name");
        contentSet.setPath(validPath.getAbsolutePath());
        contentSet.setInventoryInterval(1);
        
        assertValid(contentSet);
        
        GBSConfigurator.setGBSEnabled(false);
        contentSet.setSupplier(true);
        contentSet.setSupportsGbsTransport(true);
        
        assertFalse(GBSConfigurator.isGBSEnabled());
        assertInvalid(contentSet);
        
        GBSConfigurator.setGBSEnabled(true);
        assertTrue(GBSConfigurator.isGBSEnabled());
        
        FtpConnection conn = new FtpConnection();
        contentSet.setFtpConnection(conn);
        conn.setHost("127.0.0.1");
        conn.setPort("21");
        conn.setUsername("test");
        conn.setDirectory("dir");
        conn.setPassword("pass");
        
        assertValid(contentSet);
                
        AllowedHost host = new AllowedHost();
        contentSet.addAllowedHost(host);        
        assertInvalid(contentSet);
        
        host.setAlias("alias");
        host.setCommonName("commonName");             
        assertValid(contentSet);
        
        ScheduleExpression expression = new ScheduleExpression("0 * * * * ?");
        contentSet.addScheduleExpression(expression);
        assertValid(contentSet);        
    }
    
    public void testValidGbsConfig() {
        ContentSet contentSet = new ContentSet();
        contentSet.setSupportsGbsTransport(true);
        
        GBSConfigurator.setGBSEnabled(false);
        assertInvalidProperty(contentSet, "supportsGbsTransport");
    
        GBSConfigurator.setGBSEnabled(true);
        assertValidProperty(contentSet, "supportsGbsTransport");
    }
    
    public void testAllowedHost() {
        AllowedHost host = new AllowedHost();
        
        assertInvalid(host);
        assertInvalidProperty(host, "alias");
        assertInvalidProperty(host, "commonName");
        
        host.setAlias("       ");
        assertInvalidProperty(host, "alias");
        
        host.setCommonName("       ");
        assertInvalidProperty(host, "commonName");
        
        host.setAlias("alias");
        host.setCommonName("commonName");
        
        assertValid(host);
        assertValidProperty(host, "alias");
        assertValidProperty(host, "commonName");
    }
}