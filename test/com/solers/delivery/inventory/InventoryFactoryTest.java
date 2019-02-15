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
package com.solers.delivery.inventory;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;

import com.solers.delivery.domain.ContentSet;
import com.solers.delivery.inventory.fs.FileSystemInventoryProvider;
import com.solers.delivery.inventory.index.ReaderFactory;
import com.solers.delivery.inventory.index.diff.DifferenceFileReader;
import com.solers.delivery.inventory.plugin.InventoryPlugin;
import com.solers.delivery.inventory.plugin.PluginException;

public class InventoryFactoryTest extends TestCase {
    
    public void setUp() {
        try {
            InventoryPlugin.register(FileSystemInventoryProvider.class);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    /**
     * Inventory file names should match their LKG.
     */
    public void testInventoryCreationNamingConvention() {
        ContentSet cs = new ContentSet();
        cs.setName("creationNamingConvention");
        File temp = new File("temp");
        try {
            InventoryFactory.setSiteDirectory(temp.getAbsolutePath());
        } catch (Exception e) {}
        File invdir = new File(temp, "inventory");
        File inv = null;
        Inventory i = null;
        try {
            invdir.mkdirs();
            cs.setPath(invdir.getAbsolutePath());
            InventoryFactory.createInventory(cs);
            inv = InventoryFactory.getInventoryFile(cs);
            File[] files = temp.listFiles();
            assertTrue(files != null);
            assertTrue(files.length == 2);
            for (File f : files) {
                if (f.isFile()) inv = f;
            }
            String namePart = inv.getName().substring(0, inv.getName().indexOf('.'));
            long timestamp = Long.parseLong(namePart);
            i = ReaderFactory.newInstance(inv);
            long invstamp = (Long) i.getProperty(TimeProperties.TIMESTAMP);
            assertTrue(timestamp == invstamp);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            fail();
        } catch (PluginException pe) {
            pe.printStackTrace();
            fail();
        } finally {
            if (i != null) i.close();
            if (inv != null) {
                if (!inv.delete())
                    inv.deleteOnExit();
            }
            invdir.delete();
            temp.delete();
            try {
                FileUtils.deleteDirectory(temp);
            } catch (IOException e) {}
        }
    }
    
    /**
     * Difference files should be named for their inventory source.
     */
    public void testScanDiffNamingConvention() {
        File temp = new File("temp");
        try {
            InventoryFactory.setSiteDirectory(temp.getAbsolutePath());
        } catch (Exception e) {}
        File invdir = new File(temp, "inventory");
        File file = new File(invdir, "somefile.txt");
        File diff = null;
        File inv1 = null;
        try {
            invdir.mkdirs();
            ContentSet cs = new ContentSet();
            cs.setPath(invdir.getAbsolutePath());
            cs.setName("scanDiffNamingConvention");
            InventoryFactory.createInventory(cs);
            inv1 = InventoryFactory.getInventoryFile(cs);
            File[] files = temp.listFiles();
            assertTrue(files != null && files.length == 2);
            for (File f : files)
                if (f.isFile()) inv1 = f;
            String created = inv1.getName().substring(0, inv1.getName().indexOf('.'));
            
            file.createNewFile();
            diff = InventoryFactory.scanDifferences(cs);
            assertTrue(diff != null);
            String diffSource = diff.getName().substring(0, diff.getName().indexOf('.'));
            assertTrue(diffSource.equals(created));
        } catch (IOException ioe) {
            ioe.printStackTrace();
            fail();
        } catch (PluginException pe) {
            pe.printStackTrace();
            fail();
        } finally {
            if (inv1 != null)
                if (!inv1.delete())
                    inv1.deleteOnExit();
            if (file != null)
                if (!file.delete())
                    file.deleteOnExit();
            if (diff != null)
                if (!diff.delete())
                    diff.deleteOnExit();
            invdir.delete();
            temp.delete();
            try {
                FileUtils.deleteDirectory(temp);
            } catch (IOException e) {}
        }
    }
    
    /**
     * When an inventory is created from applying a difference, it should take the name
     * of the LKG inside the difference file.
     */
    public void testApplyDiffNamingConvention() {
        File temp = new File("temp");
        try {
            InventoryFactory.setSiteDirectory(temp.getAbsolutePath());
        } catch (Exception e) {}
        File invdir = new File(temp, "inventory");
        File file = new File(invdir, "somefile.txt");
        DifferenceFileReader dfr = null;
        File dataDir = null; 
        File diff = null;
        File inv1 = null;
        File inv2 = null;
        Inventory i1 = null;
        try {
            invdir.mkdirs();
            ContentSet cs = new ContentSet();
            cs.setPath(invdir.getAbsolutePath());
            cs.setName("applyDiffNamingConvention");
            InventoryFactory.createInventory(cs);
            dataDir = InventoryFactory.getInventoryFile(cs).getParentFile();
            File[] files = dataDir.listFiles();
            assertNotNull(files);
            assertEquals(1, files.length);
            for (File f : files) {
                if (f.isFile()) inv1 = f;
            }
            
            file.createNewFile();
            
            try {
                Thread.sleep(500);
            } catch (InterruptedException ie) {}
            diff = InventoryFactory.scanDifferences(cs);
            
            try {
                Thread.sleep(500);
            } catch (InterruptedException ie) {}
            InventoryFactory.applyDifferences(cs, diff);
            files = dataDir.listFiles();
            assertNotNull(files);
            assertEquals(3, files.length);
            
            i1 = InventoryFactory.getInventory(cs);
            long latestTimestamp = (Long) i1.getProperty(TimeProperties.TIMESTAMP);
            i1.close();
            inv2 = InventoryFactory.getInventoryFile(cs);
            long invNamestamp = Long.parseLong(inv2.getName().substring(0, inv2.getName().indexOf('.')));
            
            dfr = new DifferenceFileReader(diff);
            long diffTimestamp = (Long) dfr.getProperty(TimeProperties.TIMESTAMP);
            dfr.close();
            
            assertEquals(latestTimestamp, diffTimestamp);
            assertEquals(invNamestamp, diffTimestamp);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            fail();
        } catch (PluginException pe) {
            pe.printStackTrace();
            fail();
        } finally {
            if (i1 != null) i1.close();
            if (dfr != null) dfr.close();
            if (inv1 != null)
                if (!inv1.delete())
                    inv1.deleteOnExit();
            if (inv2 != null)
                if (!inv2.delete())
                    inv2.deleteOnExit();
            if (file != null)
                if (!file.delete())
                    file.deleteOnExit();
            if (diff != null)
                if (!diff.delete())
                    diff.deleteOnExit();
            invdir.delete();
            temp.delete();
            try {
                FileUtils.deleteDirectory(temp);
            } catch (IOException e) {}
        }
    }
}
