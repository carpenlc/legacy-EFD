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
package com.solers.delivery.tools;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

public class ClToolsTestCase extends TestCase {
    //for EFD-118
    public void testEmptyParametersAreDetected() {
        String[] params = { "" };
        assertTrue(ClTools.containsEmptyArguments(params));
        params = new String[] { " " };
        assertTrue(ClTools.containsEmptyArguments(params));
        params = new String[] { "file.txt", " " };
        assertTrue(ClTools.containsEmptyArguments(params));
        params = new String[] { "file.txt" };
        assertFalse(ClTools.containsEmptyArguments(params));
        params = new String[] { "file.txt", "--verbose" };
        assertFalse(ClTools.containsEmptyArguments(params));
    }
    
    public void testEmptyLogic() {
        assertTrue(ClTools.isEmpty("    "));
        assertTrue(ClTools.isEmpty("\t"));
        assertTrue(ClTools.isEmpty("\n\t"));
        assertTrue(ClTools.isEmpty("\r\t"));
        assertTrue(ClTools.isEmpty("\t     "));
        assertFalse(ClTools.isEmpty("\t       \n\n\n     a"));
        assertFalse(ClTools.isEmpty("aoeu"));
    }
    
    public void testExtractNotExistingFile() {
        String[][] allArgs = new String[][] {
            new String[] { "" },
            new String[] { "  " },
            new String[] { },
            new String[] { "blah" }
        };
        boolean[] isNull = new boolean[] {
            true,
            true,
            true,
            false
        };
        
        assertEquals(allArgs.length, isNull.length);
        
        for (int i = 0; i < allArgs.length; ++i) {
            String[] args = allArgs[i];
            boolean expectedNull = isNull[i];
            if (expectedNull) {
                assertNull(ClTools.extractFile(false, args));
            } else {
                assertNotNull(ClTools.extractFile(false, args));
            }
        }
        
        String[] differentPos = new String[] { "", "blah" };
        assertNull(ClTools.extractFile(false, differentPos));
        assertNotNull(ClTools.extractFile(false, 1, differentPos));
    }
    
    public void testExtractExistingFile() throws IOException {
        final String existingName = "extractExistingFile";
        final String notExistingName = "blahBlah";
        final File existingFile = new File(existingName);
        try {
            assertFalse(existingFile.exists());
            assertTrue(existingFile.createNewFile());
            assertTrue(existingFile.exists());
            assertFalse(new File(notExistingName).exists());
            
            String[] args1 = new String[] { existingName };
            assertNotNull(ClTools.extractExistingFile(args1));
            
            String[] args2 = new String[] { notExistingName };
            assertNull(ClTools.extractExistingFile(args2));
            
            String[] args3 = new String[] { "", existingName };
            assertNull(ClTools.extractExistingFile(0, args3));
            assertNotNull(ClTools.extractExistingFile(1, args3));
            
            String[] args4 = new String[] { "", notExistingName };
            assertNull(ClTools.extractExistingFile(0, args4));
            assertNull(ClTools.extractExistingFile(1, args4));
            
            String[] args5 = new String[] { existingName, notExistingName };
            assertNotNull(ClTools.extractExistingFile(0, args5));
            assertNull(ClTools.extractExistingFile(1, args5));
            
            assertNull(ClTools.extractExistingFile(-1, args5));
            assertNull(ClTools.extractExistingFile(3274, args5));
            assertNull(ClTools.extractExistingFile(1, (String[]) null));
        } finally {
            if (!existingFile.delete()) existingFile.deleteOnExit();
        }
    }
}
