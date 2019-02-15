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
package com.solers.delivery.content.supplier;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;

import com.solers.delivery.content.supplier.InventoryDifferencePruner.Policy;
import com.solers.delivery.domain.ContentSet;
import com.solers.delivery.inventory.InventoryFactory;
import com.solers.util.unit.TimeIntervalUnit;

public class InventoryDifferencePrunerTest extends TestCase {
    private static final String ext = ".diff";
    
    File runtime;
    File workDir;
    String name;
    
    public void setUp() {
        runtime = new File(".", "tempDir");
        tearDown();
        InventoryFactory.setSiteDirectory(runtime.getAbsolutePath());
        
        name = "test";
        ContentSet cs = new ContentSet();
        cs.setName(name);
        workDir = InventoryFactory.getOpenLocation(cs);
        
        assertTrue("Cannot ensure clean test environment", workDir.mkdirs());
        
    }
    
    public void tearDown() {
        InventoryFactory.setSiteDirectory(null);
        try {
            FileUtils.deleteDirectory(runtime);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void testFileCountPolicy() throws IOException {
        ContentSet config = new ContentSet();
        config.setName(name);
        
        RandomAccessFile raf = null;
        long baseName = System.currentTimeMillis();
        InventoryDifferencePruner p = new InventoryDifferencePruner(config);
        Policy policy = Policy.FILECOUNT;
        try {
            assertTrue(workDir.listFiles().length == 0);
            assertTrue(p.prune(0, policy));
            
            assertTrue(new File(workDir, String.valueOf(baseName) + ext).createNewFile());
            assertTrue(workDir.listFiles().length == 1);
            assertTrue(p.prune(1, policy));
            assertTrue(workDir.listFiles().length == 1);
            assertTrue(p.prune(0, policy));
            assertTrue(workDir.listFiles().length == 0);
            
            assertTrue(new File(workDir, String.valueOf(baseName) + ext).createNewFile());
            assertTrue(new File(workDir, String.valueOf(baseName + 1) + ext).createNewFile());
            assertTrue(new File(workDir, String.valueOf(baseName + 2) + ext).createNewFile());
            assertTrue(new File(workDir, String.valueOf(baseName + 3) + ext).createNewFile());
            assertTrue(workDir.listFiles().length == 4);
            assertTrue(p.prune(4, policy));
            assertTrue(workDir.listFiles().length == 4);
            assertTrue(p.prune(2, policy));
            assertTrue(workDir.listFiles().length == 2);
            boolean expectedFiles = true;
            for (File f : workDir.listFiles()) {
                expectedFiles &= (f.getName().equals(String.valueOf(baseName + 2) + ext) || f.getName().equals(String.valueOf(baseName + 3) + ext));
            }
            assertTrue("Pruning should leave the latest (not earliest) files.", expectedFiles);
            
            if (System.getProperty("os.name").contains("Windows")) {
                File lockedFile = new File(workDir, String.valueOf(baseName + 2) + ext);
                assertTrue(lockedFile.exists());
                raf = new RandomAccessFile(lockedFile, "rw");
                assertFalse("Test case only valid if file is locked.", lockedFile.delete());
                assertFalse("Pruning should return false if file deletion fails.", p.prune(0, policy));
                assertTrue("Pruning should short circuit when a deletion fails.", workDir.listFiles().length == 2);
                raf.close();
            }
        } finally {
            if (raf != null) raf.close();
            FileUtils.deleteDirectory(runtime);
        }
    }
    
    public void testDurationPolicy() throws IOException {
        
        boolean ready = workDir.mkdirs();
        ready |= workDir.exists() && workDir.listFiles().length == 0;
        assertTrue("Cannot ensure clean test environment", ready);
        
        ContentSet config = new ContentSet();
        config.setName(name);
        
        RandomAccessFile raf = null;
        long baseName = System.currentTimeMillis();
        InventoryDifferencePruner p = new InventoryDifferencePruner(config);
        Policy policy = Policy.DURATION;
        long hour = TimeIntervalUnit.MILLISECONDS.convert(1, TimeIntervalUnit.HOURS);
        try {
            assertTrue(workDir.listFiles().length == 0);
            assertTrue(p.prune(0, policy));
            
            assertTrue(new File(workDir, String.valueOf(baseName) + ext).createNewFile());
            assertTrue(new File(workDir, String.valueOf(baseName + hour) + ext).createNewFile());
            assertTrue(workDir.listFiles().length == 2);
            assertTrue(p.prune(hour, policy));
            assertTrue(workDir.listFiles().length == 2);
            assertTrue(p.prune(0, policy));
            assertTrue("A single difference file has a duration of 0.", workDir.listFiles().length == 1);
            
            assertTrue(new File(workDir, String.valueOf(baseName) + ext).createNewFile());
            assertTrue(new File(workDir, String.valueOf(baseName + (2 * hour)) + ext).createNewFile());
            assertTrue(new File(workDir, String.valueOf(baseName + (3 * hour)) + ext).createNewFile());
            //we now have a 3-hour time span
            assertTrue(workDir.listFiles().length == 4);
            assertTrue(p.prune((3 * hour), policy));
            assertTrue(workDir.listFiles().length == 4);
            assertTrue(p.prune((1 * hour), policy));
            assertTrue(workDir.listFiles().length == 2);
            
            boolean expectedFiles = true;
            for (File f : workDir.listFiles()) {
                expectedFiles &= (f.getName().equals(String.valueOf(baseName + (2 * hour)) + ext) || f.getName().equals(String.valueOf(baseName + (3 * hour)) + ext));
            }
            assertTrue("Pruning should leave the latest (not earliest) files.", expectedFiles);
            
            if (System.getProperty("os.name").contains("Windows")) {
                File lockedFile = new File(workDir, String.valueOf(baseName + (2 * hour)) + ext);
                assertTrue(lockedFile.exists());
                raf = new RandomAccessFile(lockedFile, "rw");
                assertFalse("This test case is only valid if the locked file is really locked...", lockedFile.delete());
                assertFalse("Pruning should return false if file deletion fails.", p.prune(0, policy));
                assertTrue("Pruning should short circuit when a deletion fails.", workDir.listFiles().length == 2);
                raf.close();
            }
        } finally {
            if (raf != null) raf.close();
            FileUtils.deleteDirectory(runtime);
        } 
    }
}
