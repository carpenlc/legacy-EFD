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
package com.solers.delivery.inventory.comparer;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import junit.framework.TestCase;

import com.solers.delivery.inventory.Inventory;
import com.solers.delivery.inventory.Writer;
import com.solers.delivery.inventory.fs.FileSystemInventory;
import com.solers.delivery.inventory.index.ReaderFactory;
import com.solers.delivery.inventory.index.WriterFactory;
import com.solers.delivery.inventory.node.Node;
import com.solers.util.SoftCache;

public class UnorderedInventoryComparerTestCase extends TestCase {

    File rootDir = new File("testCompare");
    File contentRoot = new File(rootDir, "content");
    File index = new File(rootDir, "inventory.index");
    File bogusParent = new File(contentRoot, "file");
    File bogusFile = new File(bogusParent, "child");
    Inventory ver1 = null;
    Inventory ver2 = null;
    
    static class ProcessNodeListener implements NodeListener {
        boolean started = false;
        boolean toggled = false;
        @Override
        public void onAdd(Node node) {
            fail();
        }
        @Override
        public void onRemove(Node node) {
            fail();
        }
        @Override
        public void onUpdate(Node node) {
            fail();
        }
        @Override
        public void onStart() {
            assertFalse(started);
            started = true;
        }
        @Override
        public void onStop() {
            assertTrue(started);
            started = false;
            toggled = true;
        }
    }
    
    public void testEmptyIterator() {
        Iterator<? extends Node> iter = new UnorderedInventoryComparer.EmptyIterator<Node>();
        assertFalse(iter.hasNext());
        try {
            iter.next();
            fail("Must throw NoSuchElementException to preserve Iterator contract.");
        } catch (Exception nsee) {}
        
        try {
            iter.remove();
            fail("Must not allow remove operations.");
        } catch (Exception nsee) {}
    }
    
    public void testDefaultConstructor() {
        UnorderedInventoryComparer uic = new UnorderedInventoryComparer();
        assertEquals(true, uic.recurseAdds);
        assertEquals(false, uic.recurseRemoves);
    }
    
    public void testBooleanConstructor() {
        boolean a = false;
        boolean r = true;
        UnorderedInventoryComparer uic = new UnorderedInventoryComparer(a, r);
        assertEquals(a, uic.recurseAdds);
        assertEquals(r, uic.recurseRemoves);
    }
    
    public void testHeapSize() {
        int heap = (int) Math.random() * Integer.MAX_VALUE;
        UnorderedInventoryComparer uic = new UnorderedInventoryComparer();
        uic.setHeapSize(heap);
        
        assertEquals(heap, uic.heapSize);
    }
    
    public void testNullComparison() {
        UnorderedInventoryComparer uic = new UnorderedInventoryComparer();
        ProcessNodeListener nl = new ProcessNodeListener();
        uic.compare(null, null, nl);
        assertTrue(nl.toggled);
    }
    
    public void testNullDesiredInventory() {
        createTestScenario();
        try {
            Comparer c = new UnorderedInventoryComparer(true, false);
            c.compare(null, ver1, new NodeListener() {
                boolean started = false;
                int updateCount = 0;
                int addCount = 0;
                int delCount = 0;
                
                @Override
                public void onAdd(Node node) {
                    addCount++;
                }

                @Override
                public void onRemove(Node node) {
                    delCount++;
                }

                @Override
                public void onStart() {
                    assertFalse(started);
                    started = true;
                }

                @Override
                public void onStop() {
                    assertTrue(started);
                    started = false;
                    assertEquals(0, addCount);
                    assertEquals(0, updateCount);
                    assertEquals(1, delCount);
                }

                @Override
                public void onUpdate(Node node) {
                    updateCount++;
                }
            });
        } finally {
            cleanup();
        }
    }
    
    public void testNullExistingInventory() {
        createTestScenario();
        try {
            Comparer c = new UnorderedInventoryComparer(true, false);
            c.compare(ver1, null, new NodeListener() {
                boolean started = false;
                int updateCount = 0;
                int addCount = 0;
                int delCount = 0;
                
                @Override
                public void onAdd(Node node) {
                    addCount++;
                }

                @Override
                public void onRemove(Node node) {
                    delCount++;
                }

                @Override
                public void onStart() {
                    assertFalse(started);
                    started = true;
                }

                @Override
                public void onStop() {
                    assertTrue(started);
                    started = false;
                    assertEquals(2, addCount);
                    assertEquals(0, updateCount);
                    assertEquals(0, delCount);
                }

                @Override
                public void onUpdate(Node node) {
                    updateCount++;
                }
            });
        } finally {
            cleanup();
        }       
    }
    
    public void testSoftCacheReconstructor() {
        createTestScenario();
        try {
            SoftCache.Reconstructor<String, Node> reconstructor = new UnorderedInventoryComparer.InventoryReconstructor(ver1);
            String nodeKey = contentRoot.getName() + File.separator + bogusParent.getName();
            Node n = ver1.read(nodeKey);
            assertNotNull(n);
            Node r = reconstructor.rebuild(nodeKey);
            assertEquals(n, r);
        } finally {
            cleanup();
        }
    }
    
    public void testFileToDirectoryUpdate() {
        createTestScenario();
        try {
            Comparer c = new UnorderedInventoryComparer(true, false);
            c.compare(ver2, ver1, new NodeListener() {
                int updateCount = 0;
                int addCount = 0;
                boolean parentUpdated = false;
                boolean childAdded = false;
                boolean parentIsFile = true;
                
                @Override
                public void onAdd(Node node) {
                    addCount++;
                    if (node.getName().equals(bogusFile.getName())) {
                        childAdded = true;
                    }
                }

                @Override
                public void onRemove(Node node) {
                    fail("Did not expect removal of node. (need update)");
                }

                @Override public void onStart() {
                    assertEquals(0, updateCount);
                    assertEquals(0, addCount);
                    assertFalse(parentUpdated);
                    assertTrue(parentIsFile);
                    assertFalse(childAdded);
                }
                @Override public void onStop() {
                    assertEquals(1, updateCount);
                    assertEquals(1, addCount);
                    assertTrue(parentUpdated);
                    assertFalse(parentIsFile);
                    assertTrue(childAdded);
                }

                @Override
                public void onUpdate(Node node) {
                    updateCount++;
                    if (node.getName().equals(bogusParent.getName())) {
                        parentUpdated = true;
                        parentIsFile = !node.isDirectory();
                    }
                }
                
            });           
        } finally {
            cleanup();
        }       
    }
    
    public void testDirectoryToFileUpdate() {
        createTestScenario();
        try {
            Comparer c = new UnorderedInventoryComparer(true, false);
            c.compare(ver1, ver2, new NodeListener() {
                int updateCount = 0;
                boolean parentUpdated = false;
                boolean parentIsFile = false;
                
                @Override
                public void onAdd(Node node) {
                    fail("Did not expect addition of node. (need update)");
                }

                @Override
                public void onRemove(Node node) {
                    fail("Did not expect removal of node. (need update)");
                }

                @Override public void onStart() {
                    assertEquals(0, updateCount);
                    assertFalse(parentUpdated);
                    assertFalse(parentIsFile);
                }
                @Override public void onStop() {
                    assertEquals(1, updateCount);
                    assertTrue(parentUpdated);
                    assertTrue(parentIsFile);
                }

                @Override
                public void onUpdate(Node node) {
                    updateCount++;
                    if (node.getName().equals(bogusParent.getName())) {
                        parentUpdated = true;
                        parentIsFile = !node.isDirectory();
                    }
                }
                
            });
        } finally {
            cleanup();
        }
    }
    
    private void createTestScenario() {
        assertTrue(contentRoot.mkdirs());
        try {
            assertTrue(bogusParent.createNewFile());
        } catch (IOException ioe) {
            ioe.printStackTrace();
            fail("Failed to construct test file tree.");
        }
        
        Writer w = WriterFactory.newInstance(new FileSystemInventory(contentRoot));
        try {
            w.write(index);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            fail("Failed to write inventory.");
        }
        
        try {
            assertTrue(bogusParent.delete());
            assertTrue(bogusParent.mkdirs());
            assertTrue(bogusFile.createNewFile());
        } catch (IOException ioe) {
            ioe.printStackTrace();
            fail("Failed to write secondary inventory.");
        }
        
        //verify work on initial inventory
        try {
            ver1 = ReaderFactory.newInstance(index);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            fail("Could not read constructed inventory.");
        }
        
        String bogusChildPath = contentRoot.getName() + File.separator + bogusParent.getName();
        assertEquals(contentRoot.getName(), ver1.root().getName());
        assertFalse(ver1.read(bogusChildPath).isDirectory());
        
        //verify file system is represented as we desire in inventory representation
        ver2 = new FileSystemInventory(contentRoot);
        assertEquals(contentRoot.getName(), ver2.root().getName());
        assertTrue(ver2.read(bogusChildPath).isDirectory());
        assertNotNull(ver2.read(bogusChildPath + File.separator + bogusFile.getName()));
    }
    
    private void cleanup() {
        ver1.close();
        ver2.close();
        if (!bogusFile.delete()) bogusFile.deleteOnExit();
        if (!bogusParent.delete()) bogusParent.deleteOnExit();
        if (!index.delete()) index.deleteOnExit();
        if (!contentRoot.delete()) contentRoot.deleteOnExit();
        if (!rootDir.delete()) rootDir.deleteOnExit();
    }
}
