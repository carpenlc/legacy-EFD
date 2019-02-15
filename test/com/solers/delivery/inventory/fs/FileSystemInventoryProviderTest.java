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
package com.solers.delivery.inventory.fs;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import junit.framework.TestCase;

import com.solers.delivery.inventory.Inventory;

public class FileSystemInventoryProviderTest extends TestCase {
    FileSystemInventoryProvider p = new FileSystemInventoryProvider();
    
    public void testAcceptDirectories() {
        File f = new File(".");
        assertTrue(f.exists());
        assertTrue(f.isDirectory());
        assertTrue(p.accept(f.toURI()));
    }
    
    public void testAcceptNonexistentPaths() {
        File f = new File(".", "blargle/flargle/nardle/zouss/marbles/in/my/mouth");
        assertFalse(f.exists());
        assertTrue(p.accept(f.toURI()));
        Inventory i = p.newInstance(f.toURI(), new HashMap<String, Object>(0));
        assertNull(i.root());
        assertEquals("mouth", i.getRootName());
    }
    
    public void testRejectFiles() {
        File f = new File(".", "testFile");
        assertFalse(f.exists());
        try {
            try {
                assertTrue(f.createNewFile());
                f.deleteOnExit();
            } catch (IOException ioe) {
                ioe.printStackTrace();
                fail();
            }
            
            assertTrue(f.isFile());
            assertFalse(p.accept(f.toURI()));
        } finally {
            f.delete();
        }
    }
    
    public void testRejectNonFileSchemas() throws URISyntaxException {
        URI smb = new URI("smb://whatever");
        assertEquals("smb", smb.getScheme());
        URI http = new URI("http://whatever");
        assertEquals("http", http.getScheme());
        URI https = new URI("https://whatever");
        assertEquals("https", https.getScheme());
        URI ftp = new URI("ftp://whatever");
        assertEquals("ftp", ftp.getScheme());
        
        URI[] notFiles = new URI[] { smb, http, https, ftp };
        
        for (URI u : notFiles) {
            assertFalse(p.accept(u));
        }
    }
}
