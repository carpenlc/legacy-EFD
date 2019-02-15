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
package com.solers.delivery.inventory.index.v2;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;

import com.solers.delivery.inventory.Inventory;
import com.solers.delivery.inventory.Writer;
import com.solers.delivery.inventory.fs.FileSystemInventory;
import com.solers.delivery.inventory.index.ReaderFactory;
import com.solers.delivery.inventory.node.Node;
import com.solers.util.Filter;

public class InventoryReaderTestCase extends TestCase {
    public void setUp() {
        try {
            FileUtils.forceDelete(new File("testCreation"));
        } catch (IOException ex) {
            
        }
    }
    
    public void testCreation() throws IOException {
        try {
            new InventoryReader((File)null);
            fail();
        } catch (IllegalArgumentException expected) {
            
        }
        
        try {
            File file = new File("testCreation");
            assertFalse(file.exists());
            new InventoryReader(file);
            fail();
        } catch (IllegalArgumentException expected) {
            
        }
        
        try {
            File file = new File("testCreation");
            file.setReadable(false);
            assertFalse(file.canRead());
            new InventoryReader(file);
            fail();
        } catch (IllegalArgumentException expected) {
            
        }
        
        try {
            File file = new File("testCreation");
            file.createNewFile();
            file.deleteOnExit();
            assertTrue(file.exists());
            new InventoryReader(file);
            fail();
        } catch (IOException expected) {
            
        }
    }
    
    public void testFilter() {
        File root = new File("root");
        root.mkdir();
        final File file = new File(root, "file");
        File inv = new File("testFilter.index");
        Inventory i = null;
        try {
            file.createNewFile();
            Writer w = new com.solers.delivery.inventory.index.v2.InventoryWriter(new FileSystemInventory(root));
            w.write(inv);
            i = ReaderFactory.newInstance(inv);
            
            Node parent = i.read("root");
            assertTrue(parent.getChildren().get(0).getName().equals(file.getName()));
            String path = root.getName() + File.separator + file.getName();
            assertTrue(i.read(path) != null);
            i.useFilter(new Filter<Node>() {
                @Override
                public boolean accept(Node object) {
                    return !file.getName().equals(object.getName());
                }
            });
            
            parent = i.read("root");
            assertTrue(parent != null);
            assertTrue(i.read(path) == null);
            assertTrue(i.read("root" + File.separator + "aCOtOFtoO") == null);
            assertTrue(parent.getChildren().isEmpty());
            
            i.useFilter(new Filter<Node>() {
                @Override
                public boolean accept(Node object) {
                    return true;
                }
            });
            
            assertTrue(i.root() != null);
            assertTrue(i.read(path) != null);
        } catch (IOException ioe) {
            fail();
        } finally {
            if (i != null) i.close();
            file.delete();
            root.delete();
            inv.delete();
        }
    }
    
    public void testRootNameContract() throws IOException {
        final String name = "rootNameContract";
        final File test = new File(name);
        final File index = new File("rootNameContract.index");
        Inventory i = null;
        try {
            assertTrue(test.mkdir());
            Writer w = new com.solers.delivery.inventory.index.v2.InventoryWriter(new FileSystemInventory(test));
            w.write(index);
            i = ReaderFactory.newInstance(index);
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
            if (i != null) i.close();
            if (!index.delete()) index.deleteOnExit();
            if (!test.delete()) test.deleteOnExit();
        }
    }
}
