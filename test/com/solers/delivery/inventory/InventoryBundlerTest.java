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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import junit.framework.JUnit4TestAdapter;
import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.tools.tar.TarInputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.solers.delivery.domain.ContentSet;

/**
 * Tests the InventoryBundler class.  Verifies inventory can be bundled, that the bundle contains the appropriate
 * files, and that backups are created as necessary.
 *
 * @author JGimourginas
 */
public class InventoryBundlerTest extends TestCase {

    private static InventoryBundler bundler = null;
    private static File file1 = null;
    private static File packagedInventoriesSub = null;
    private static File currentInventoriesSub = null;

    /**
     * Set up for entire test class, not a per test case basis.  I.e. tests build on one another.
     */
    @Before
    public void setUp() throws IOException {
        File siteDir = new File(".", "test");
        InventoryFactory.setSiteDirectory(siteDir.getAbsolutePath());
        
        String name = "test";
        ContentSet config = new ContentSet();
        config.setName(name);
        
        //setup directories to use for test
        currentInventoriesSub = InventoryFactory.getOpenLocation(config);
        //currentInventoriesRoot.deleteOnExit();
        packagedInventoriesSub = InventoryFactory.getPackageLocation(config);
        //packagedInventoriesRoot.deleteOnExit();
        //currentInventoriesSub = new File(currentInventoriesRoot, name);
        //packagedInventoriesSub = new File(packagedInventoriesRoot, name);
        //set member vars - usually done by InventorCreationTaskFactory
        //bundler assumes directories have been created, so create them here
        boolean success = currentInventoriesSub.mkdirs();
        if (!success) fail("Couldn't create " + currentInventoriesSub.getAbsolutePath());
        //packaged inventories sub should be created by bundler, so don't create it here
        success = packagedInventoriesSub.mkdirs();
        if (!success) fail("Couldn't create " + packagedInventoriesSub.getAbsolutePath());
        
        //create 2 files that will be bundled
        file1 = new File(currentInventoriesSub, "5.index");
        File file2 = new File(currentInventoriesSub, "4.diff");
        File f3 = new File(currentInventoriesSub, "3.diff");
        File f4 = new File(currentInventoriesSub, "2.diff");
        success = file1.createNewFile();
        success &= file2.createNewFile();
        success &= f3.createNewFile();
        success &= f4.createNewFile();
        
        if (!success) fail("Couldn't create files");
        //write to one of the files just for fun
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(file1));
            bos.write(12345);
            bos.write(12345);
            bos.write(12345);
        } catch (Exception e) {

        } finally {
            IOUtils.closeQuietly(bos);
        }
        //bundler needs directory containing files and place to put packaged file
        bundler = new InventoryBundler(config);
        bundler.setNumInventoryBackups(1);
    }
    
    /**
     * Tear down after entire set of tests are run, but not on a per test cases basis
     */
    @After
    public void tearDown() {
        try {
            File file = new File("test");
            file.deleteOnExit();
            FileUtils.deleteDirectory(file);
        } catch (Exception e) {
            
        }
    }

    @Test
    public void testBundlesNotAvailable() throws IOException {
        assertFalse(bundler.bundlesAvailable());
    }
    
    /**
     * Test to verify the correct actions occur on first bundle operation
     */
    @Test
    public void testFirst() {
        bundler.bundleInventory();
        //sub dir should be created by bundler if it does not exist
        assertTrue(packagedInventoriesSub.exists());
        //packaged file should now reside under sub dir
        assertEquals(4, packagedInventoriesSub.listFiles().length);
        assertTrue(packagedInventoriesSub.listFiles(new FileFilter() { public boolean accept(File f) { return f.getName().endsWith(".bak"); }}).length == 0);
        //verify contents are the same
        verifyPackagedValues("test_5.tar.gz");
    }

    /**
     * Test to verify backup thing works
     */
    @Test
    public void testSecond() {
        testFirst();
        bundler.bundleInventory();
        //Nothing should happen because the inventory has not been updated
        assertEquals(4, packagedInventoriesSub.listFiles().length);
        verifyPackagedValues("test_5.tar.gz");
    }

    /**
     * Test to verify backup thing works
     */
    @Test
    public void testThird() {
        testSecond();
        file1.renameTo(new File(currentInventoriesSub, "6.index"));
        try {
            new File(currentInventoriesSub, "5.diff").createNewFile();
        } catch (IOException ioe) {
            fail(ioe.getMessage());
        }
        
        bundler.bundleInventory();
        //should now contain 5 package files and a backup, making 6 total
        assertEquals(6, packagedInventoriesSub.listFiles().length);
        verifyPackagedValues("test_6.tar.gz");
    }
    
    @Test
    public void testFourth() {
        testThird();
        //again, should not take an action
        bundler.bundleInventory();
        assertEquals(6, packagedInventoriesSub.listFiles().length);
        verifyPackagedValues("test_6.tar.gz");
    }
    
    @Test
    public void testFifth() {
        testFourth();
        boolean success = new File(currentInventoriesSub, "6.index").renameTo(new File(currentInventoriesSub, "7.index"));
        try {
            if (!success) throw new IOException("Failed to rename index file.");
            new File(currentInventoriesSub, "6.diff").createNewFile();
        } catch (IOException ioe) {
            fail(ioe.getMessage());
        }
        
        bundler.bundleInventory();
        //we have set backup count to 1, so only 1 extra file (the new diff)
        //should be accounted for in the counts
        assertEquals(7, packagedInventoriesSub.listFiles().length);
        verifyPackagedValues("test_7.tar.gz");
    }
    
    @Test
    public void testBundlesAvailable() {
        testFifth();
        assertTrue(bundler.bundlesAvailable());
    }

    private void verifyPackagedValues(String archive){
        FileInputStream in = null;
        FileOutputStream fos = null;
        BufferedReader reader1 = null;
        BufferedReader reader2 = null;
        try {
            reader1 = new BufferedReader(new FileReader(file1));
            String lineFromFile = reader1.readLine();
            in = new FileInputStream(new File(packagedInventoriesSub, archive));
            TarInputStream tIn = new TarInputStream(new GZIPInputStream(in));
            int count = 1;
            while((tIn.getNextEntry()) != null) {
                fos= new FileOutputStream(new File("test/temp" + count));
                byte[] buffer = new byte[1024];
                int length = 0;
                while ((length = tIn.read(buffer)) >= 0) {
                    fos.write(buffer, 0, length);
                }
                fos.close();
                fos = null;
                count++;
            }
            reader2 = new BufferedReader(new FileReader(new File("test/temp1")));
            String lineFromZip = reader2.readLine();
            assertEquals(lineFromFile, lineFromZip);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(fos);
            IOUtils.closeQuietly(reader1);
            IOUtils.closeQuietly(reader2);
        }
    }

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(InventoryBundlerTest.class);
    }
}
