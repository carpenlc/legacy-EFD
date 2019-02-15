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
/**
 * 
 */
package com.solers.delivery;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;

/**
 * @author mchen
 *
 */
public final class TestUtil {

    /**
     * Deletes file if exists, create a new file, and sets it to be deleted on exit
     * @param filePath - full path to of file to be created
     * @return newly created file set to be deleted on exit
     * @throws IOException if there is a problem creating the file
     */
    public static File createFile(String filePath) throws IOException {
        File file = createActualFile(filePath);
        file.createNewFile();
        file.deleteOnExit();
        Assert.assertTrue(file.exists());
        return file;
    }
    
    /**
     * Deletes directory if exists, create a new directory, and sets it to be deleted on exit.
     * Does not support nested directory.
     * 
     * @param filePath - full path to of directory to be created
     * @return newly created directory set to be deleted on exit
     * @throws IOException if there is a problem creating the directory
     */
    public static File createDirectory(String dirPath) {
        File file = createActualFile(dirPath);
        file.deleteOnExit();
        file.mkdir();
        Assert.assertTrue(file.exists());
        return file;
    }

    private static File createActualFile(String filePath) {
        File file = new File(filePath);
        
        if (file.exists()) {
            deleteFile(file);
        }
        
        return file;
    }
    
    private static void deleteFile(File dir) {
        for(File file : dir.listFiles()) {
            if (file.isDirectory()) {
                deleteFile(file);
            } else {
                Assert.assertTrue(file.delete());
            }
        }
    }
}
