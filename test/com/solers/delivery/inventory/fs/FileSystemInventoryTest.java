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

import junit.framework.TestCase;

import org.junit.Test;

import com.solers.delivery.inventory.HashProperties;
import com.solers.delivery.inventory.Inventory;
import com.solers.delivery.inventory.TimeProperties;
import com.solers.delivery.inventory.node.Node;
import com.solers.util.Filter;

public class FileSystemInventoryTest extends TestCase {
    @Test
    public void testEnsureCanonicalizedPaths() {
        if (System.getProperty("os.name").contains("Windows")) {
            File tempDir = new File("temp");
            File inv = new File(tempDir, "INVdir");
            try {
                inv.mkdirs();
                File fsinvFile = new File(inv.getAbsolutePath().toLowerCase());
                assertTrue(fsinvFile.getName().equals("invdir"));
                Inventory i = new FileSystemInventory(fsinvFile);
                assertEquals("INVdir", i.root().getName());
            } finally {
                if (!inv.delete()) inv.deleteOnExit();
                if (!tempDir.delete()) tempDir.deleteOnExit();
            }
        } else {
            //this test does not apply to non-Windows boxes
            assertTrue(true);
        }
    }
    
    public void testNullRootRetainsRootName() {
        File f = new File(".", "guaranteed/not/to/exist/123457/19873/root");
        assertFalse(f.exists());
        Inventory i = new FileSystemInventory(f);
        assertNull(i.root());
        assertEquals("root", i.getRootName());
    }
    
    public void testBadPathResilience() {
        String crazyPath = "c:/?[]/\\=+<>:;\",*\0|";
        try {
            File f = new File(crazyPath);
            assertFalse(f.exists());
            f.getCanonicalPath();
            assertFalse("Crazy path string in test must cause canonicalization failure on Windows",
                System.getProperty("os.name").contains("Windows"));
        } catch (IOException ioe) {}
        Inventory inv = new FileSystemInventory(crazyPath);
        assertTrue(inv.root() == null);
    }
    
    public void testProperties() {
        //file system inventory does not support ANY properties at this time
        Inventory i = new FileSystemInventory(".");
        assertTrue(i.properties().size() == 0);
        assertTrue(i.getProperty(TimeProperties.TIMESTAMP) == null);
        assertTrue(i.getProperty(HashProperties.ALGORITHM) == null);
    }
    
    public void testFileSystemMapping() {
        File root = new File("tempDir");
        File file = new File(root, "someFile.txt");
        
        try {
            assertTrue(root.mkdirs());
            assertTrue(file.createNewFile());
            Inventory i = new FileSystemInventory(root);
            assertTrue(i.root().getName().equals(root.getName()));
            assertTrue(i.read(root.getName() + File.separator + file.getName()) != null);
            assertTrue(i.read(root.getName() + File.separator + "fakeFile") == null);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            fail();
        } finally {
            if (!file.delete()) file.deleteOnExit();
            if (!root.delete()) root.deleteOnExit();
        }
    }
    
    public void testFilter() {
        final File root = new File("tempDir");
        final File file = new File(root, "someFile.txt");
        try {
            assertTrue(root.mkdirs());
            assertTrue(file.createNewFile());
            Inventory i = new FileSystemInventory(root);
            assertTrue(i.root() != null);
            assertTrue(i.root().getName().equals(root.getName()));
            assertTrue(i.root().getChildren().get(0).getName().equals(file.getName()));
            
            i.useFilter(new Filter<Node>() {
                @Override
                public boolean accept(Node object) {
                    return !file.getName().equals(object.getName());
                }
            });
            
            assertTrue(i.root() != null);
            assertTrue(i.root().getChildren().isEmpty());
            assertTrue(i.read(root.getName() + File.separator + file.getName()) == null);
            
            i.useFilter(new Filter<Node>() {
                @Override
                public boolean accept(Node object) {
                    return false;
                }
            });
            
            assertTrue(i.root() == null);
            assertTrue(i.read(root.getName()) == null);
            assertTrue(i.read(root.getName() + File.separator + file.getName()) == null);
            
            i.useFilter(new Filter<Node>() {
                @Override
                public boolean accept(Node object) {
                    return !root.getName().equals(object.getName());
                }
            });
            
            assertTrue(i.root() == null);
            assertTrue(i.read(root.getName() + File.separator + file.getName()) == null);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            fail();
        } finally {
            if (!file.delete()) file.deleteOnExit();
            if (!root.delete()) root.deleteOnExit();
        }
    }
    
    public void testDisallowFileInventories() throws IOException {
        File test = new File("disallowFileInventories");
        assertFalse(test.exists());
        assertTrue(test.createNewFile());
        try {
            new FileSystemInventory(test);
            fail("Cannot accept files as inventory roots.");
        } catch (IllegalArgumentException iae) {
            //ok
        } finally {
            if (!test.delete()) test.deleteOnExit();
        }
    }
    
    public void testRootNameContract() {
        final String name = "rootNameContract";
        File test = new File(name);
        try {
            assertTrue(test.mkdir());
            Inventory i = new FileSystemInventory(test);
            assertNotNull(i.root());
            assertEquals(name, i.root().getName());
            i.useFilter(new Filter<Node>(){
                @Override
                public boolean accept(Node object) {
                    return false;
                }
            });
            assertNull(i.root());
            assertEquals(name, i.getRootName());
        } finally {
            if (!test.delete()) test.deleteOnExit();
        }
    }
}
