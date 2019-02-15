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
package com.solers.delivery.scripting.input;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import com.solers.delivery.TestUtil;
import com.solers.delivery.scripting.ScriptConfiguration;
import com.solers.delivery.scripting.ScriptConfiguration.ScriptLanguage;
import com.solers.delivery.scripting.input.ScriptInput.DirectoryRecord;
import com.solers.delivery.util.FileSystemUtil;

/**
 * @author mchen
 *
 */
public class UnitScannerTestCase extends TestCase {
    private UnitsScanner scanner;
    private ScriptInput input;
    private File dir;
    private File subDir;
    private static final String TESTDIR="scripts/UnitScannerTestCase";
    
    @Before
    @Override
    public void setUp() {
        this.dir = TestUtil.createDirectory(FileSystemUtil.relativeToEFDHome(TESTDIR).getAbsolutePath());
        this.subDir = TestUtil.createDirectory(FileSystemUtil.relativeToEFDHome(TESTDIR).getAbsolutePath() +"/sub");
        List<ScriptLanguage> list = new ArrayList<ScriptLanguage>();
        ScriptConfiguration config = new ScriptConfiguration(list, "JavaScript");
        this.scanner = new UnitsScanner(config);

        this.input = new ScriptInput();
        this.input.setDirectoryList(new ArrayList<DirectoryRecord>());

        DirectoryRecord record = new DirectoryRecord(TESTDIR, "groovy", "foo,bar,baz");
        this.input.getDirectoryList().add(record);
    }

    @Test
    public void testScanNext() throws IOException {
        //should be empty initially
        this.assertFiles(0);
        
        TestUtil.createFile(this.dir.getAbsolutePath() + File.separator + "file1.groovy");
        
        this.scanner.scanNext(this.input);

        this.assertFiles(1);

        TestUtil.createFile(this.dir.getAbsolutePath() + File.separator + "file2.groovy");
        
        this.scanner.scanNext(this.input);

        //should be 2 and not 3 (since there's already one in the list in the first scan
        this.assertFiles(2);

        TestUtil.createFile(this.dir.getAbsolutePath() + File.separator + "file3.js");
        
        this.scanner.scanNext(this.input);

        //should be 3 since we are accepting all files
        this.assertFiles(3);        

        TestUtil.createFile(this.subDir.getAbsolutePath() + File.separator + "file4.js");
        
        this.scanner.scanNext(this.input);

        //should be 4 since we are accepting files from sub-directories too
        this.assertFiles(4);        

    }

    @Test
    public void testIgnoreDirectories() throws IOException {
        //should be empty initially
        this.assertFiles(0);
        TestUtil.createDirectory(this.dir.getAbsolutePath() + File.separator + "dir1");
        
        this.scanner.scanNext(this.input);

        //should be 0 since it's ignoring directories
        this.assertFiles(0);
    
    }
    
    private void assertFiles(int count){
        for(DirectoryRecord rec :this.input.getDirectoryList()) {
            int size = rec.getFilenames().size();
            assertEquals(count, size);
        }
    }
}
