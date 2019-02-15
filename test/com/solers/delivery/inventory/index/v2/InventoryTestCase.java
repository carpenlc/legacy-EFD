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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;

import com.solers.delivery.inventory.Inventory;
import com.solers.delivery.inventory.fs.FileSystemInventory;
import com.solers.delivery.inventory.node.Node;

public class InventoryTestCase extends TestCase {
    
    File root;
    long modifiedTime;
    
    public void setUp() {
        root = new File("InventoryTestCase");
        root.mkdirs();
        root.deleteOnExit();
        
        //collect a modified time supported by the system, which is system-dependent
        //due to rounding errors.
        modifiedTime = System.currentTimeMillis();
        File temp = new File("timestamp");
        try {
            assertTrue(temp.createNewFile());
            assertTrue(temp.setLastModified(modifiedTime));
            modifiedTime = temp.lastModified();
            assertTrue(temp.delete());
        } catch (IOException ioe) {
            fail();
        }
        
    }
    
    public void tearDown() {
        try {
            FileUtils.deleteDirectory(root);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public void testEmptyNode() throws IOException {
        assertTrue(root.exists());
        assertTrue(root.isDirectory());
        Inventory i = generateAndReadInventory("testEmptyNode");
        Node rootNode = i.root();
        
        assertEquals(root.getName(), rootNode.getName());
        assertTrue(rootNode.isDirectory());
        assertTrue(rootNode.getChildren().isEmpty());
        
        //test for case where reading empty parent causes arithmetic exception
        String dummy = root.getName() + File.separator + "doesNotExist123682";
        try {
            assertTrue(i.read(dummy) == null);
        } catch (ArithmeticException ae) {
            fail();
        }
    }
    
    public void testNodeWithFileChildren() throws IOException {
        List<String> filenames = Arrays.asList(new String [] {"testNodeWithFileChildren1", "testNodeWithFileChildren2", "testNodeWithFileChildren3"});
        for (String filename : filenames) {
            create(root, filename);
        }
        
        Inventory i = generateAndReadInventory("testNodeWithFileChildren");
        Node rootNode = i.root();
        
        assertEquals(root.getName(), rootNode.getName());
        assertTrue(rootNode.isDirectory());
        assertEquals(3, rootNode.getChildren().size());
        
        for (Node child : rootNode.getChildren()) {
            assertTrue(filenames.contains(child.getName()));
            assertFalse(child.isDirectory());
            assertEquals(new File(root, child.getName()).getPath(), child.getPath());
            assertEquals(modifiedTime, child.getTimestamp());
            assertEquals(1, child.getSize());
        }
        
    }
    
    public void testNodeWithDirectoryChildren() throws Exception {
        File sub = new File(root, "subDirectory");
        sub.mkdirs();
        List<String> filenames = Arrays.asList("child1", "child2");
        
        for (String s : filenames) {
            create(sub, s);
        }
  
        Inventory i = generateAndReadInventory("testNodeWithDirectoryChildren");
        Node rootNode = i.root(); 
        
        assertEquals(root.getName(), rootNode.getName());
        assertTrue(rootNode.isDirectory());
        assertEquals(1, rootNode.getChildren().size());
        
        Node childNode = rootNode.getChildren().get(0);
        
        assertEquals(sub.getName(), childNode.getName());
        assertEquals(sub.getPath(), childNode.getPath());
        assertEquals(2, childNode.getChildren().size());
        
        for (Node child : childNode.getChildren()) {
            assertTrue(filenames.contains(child.getName()));
            assertFalse(child.isDirectory());
            assertEquals(new File(sub, child.getName()).getPath(), child.getPath());
            assertEquals(modifiedTime, child.getTimestamp());
            assertEquals(1, child.getSize());
        }
    }
    
    public void testReadSubPath() throws IOException {
        File sub = new File(root, "subDirectory");
        sub.mkdirs();
        List<String> filenames = Arrays.asList("child1", "child2");
        
        for (String s : filenames) {
            create(sub, s);
        }
        
        Node node = generateAndReadInventory("testReadSubPath", root.getName()+"\\subDirectory");
        
        assertNotNull(node);
        assertEquals(sub.getName(), node.getName());
        assertEquals(sub.getPath(), node.getPath());
        assertEquals(2, node.getChildren().size());
        for (Node child : node.getChildren()) {
            assertTrue(filenames.contains(child.getName()));
            assertFalse(child.isDirectory());
            assertEquals(new File(sub, child.getName()).getPath(), child.getPath());
            assertEquals(modifiedTime, child.getTimestamp());
            assertEquals(1, child.getSize());
        }
        
        node = generateAndReadInventory("testReadSubPath", root.getName()+"/subDirectory");
        
        assertNotNull(node);
        assertEquals(sub.getName(), node.getName());
        assertEquals(sub.getPath(), node.getPath());
        assertEquals(2, node.getChildren().size());
        for (Node child : node.getChildren()) {
            assertTrue(filenames.contains(child.getName()));
            assertFalse(child.isDirectory());
            assertEquals(new File(sub, child.getName()).getPath(), child.getPath());
            assertEquals(modifiedTime, child.getTimestamp());
            assertEquals(1, child.getSize());
        }
    }
    
    private Inventory generateAndReadInventory(final String indexName) throws IOException {
        InventoryWriter writer = new InventoryWriter(new FileSystemInventory(root));
        File index = new File(indexName);
        
        writer.write(index);
        
        final InventoryReader r = new InventoryReader(index);
        Runtime.getRuntime().addShutdownHook(new Thread() { public void run() { r.close(); new File(indexName).delete(); }});
        return r;
    }
    
    private Node generateAndReadInventory(final String indexName, String path) throws IOException {
        InventoryWriter writer = new InventoryWriter(new FileSystemInventory(root));
        File index = new File(indexName);
        
        writer.write(index);
        
        final InventoryReader r = new InventoryReader(index);
        Runtime.getRuntime().addShutdownHook(new Thread() { public void run() { r.close(); new File(indexName).delete(); }});
        return r.read(path);
    }
    
    private File create(File root, String filename) throws IOException {
        File result = new File(root, filename);
        result.deleteOnExit();
        result.createNewFile();
        
        FileOutputStream writer = new FileOutputStream(result);
        writer.write(new byte [] {2});
        writer.close();
        
        result.setLastModified(modifiedTime);
        assertEquals("Setting file timestamps must work.", modifiedTime, result.lastModified());
        
        return result;
    }
    
}
