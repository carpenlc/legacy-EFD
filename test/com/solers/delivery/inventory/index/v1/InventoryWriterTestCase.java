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
package com.solers.delivery.inventory.index.v1;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class InventoryWriterTestCase extends TestCase {
    
    public void setUp() {
        try {
            FileUtils.forceDelete(new File("InventoryWriterTestCase"));
        } catch (IOException ex) {
            
        }
    }
    
    public void testCreation() throws IOException {
        try {
            new InventoryWriter(null);
            fail();
        } catch (IllegalArgumentException expected) {
            
        }
        
        try {
            File dir = new File("InventoryWriterTestCase");
            assertFalse(dir.exists());
            new InventoryWriter(dir);
            fail();
        } catch (IllegalArgumentException expected) {
            
        }
        
        try {
            File file = new File("InventoryWriterTestCase");
            file.createNewFile();
            file.deleteOnExit();
            assertTrue(file.exists());
            assertFalse(file.isDirectory());
            new InventoryWriter(file);
            fail();
        } catch (IllegalArgumentException expected) {
            
        }
        
    }

}
