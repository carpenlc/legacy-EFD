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
package com.solers.delivery.inventory.diff;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import com.solers.delivery.inventory.Inventory;
import com.solers.delivery.inventory.Writer;
import com.solers.delivery.inventory.comparer.Comparer;
import com.solers.delivery.inventory.comparer.UnorderedInventoryComparer;
import com.solers.delivery.inventory.fs.FileSystemInventory;
import com.solers.delivery.inventory.index.ReaderFactory;
import com.solers.delivery.inventory.index.WriterFactory;
import com.solers.delivery.inventory.index.diff.DifferenceFileEventHandler;
import com.solers.delivery.inventory.index.diff.DifferenceFileReader;
import com.solers.delivery.inventory.index.diff.DifferenceNode;
import com.solers.delivery.inventory.node.Node;

public class DifferenceFileEventHandlerTestCase extends TestCase {
    File rootDir = new File("testCompare");
    File contentRoot = new File(rootDir, "content");
    File index = new File(rootDir, "inventory.index");
    File bogusParent = new File(contentRoot, "file");
    File bogusFile = new File(bogusParent, "child");
    File difference = new File(rootDir, "difference.diff");
    Inventory ver1 = null;
    Inventory ver2 = null;
    
    public void testFileToDirDifferencing() {
        createTestScenario();
        DifferenceFileReader dfr = null;
        try {
            DifferenceFileEventHandler dfeh = new DifferenceFileEventHandler(difference);
            Comparer c = new UnorderedInventoryComparer(true, false);
            c.compare(ver2, ver1, dfeh);
            assertEquals(3, dfeh.getDifferenceCount());
            dfr = new DifferenceFileReader(difference);
            Node n = dfr.read(contentRoot.getName());
            assertNotNull(n);
            assertEquals(1, n.getChildren().size());
            n = n.getChildren().get(0);
            assertEquals(bogusParent.getName(), n.getName());
            assertTrue(n instanceof DifferenceNode);
            DifferenceNode dn = (DifferenceNode) n;
            assertTrue(dn.isChanged());
            assertTrue(dn.isDirectory());
            assertEquals(1, n.getChildren().size());
            n = dn.getChildren().get(0);
            assertFalse(n.isDirectory());
            assertEquals(bogusFile.getName(), n.getName());
            assertFalse(n.isDirectory());
            assertTrue(n.getChildren().isEmpty());
        } finally {
            if (dfr != null) dfr.close();
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
        if (!difference.delete()) difference.deleteOnExit();
        if (!contentRoot.delete()) contentRoot.deleteOnExit();
        if (!rootDir.delete()) rootDir.deleteOnExit();
    }
}
