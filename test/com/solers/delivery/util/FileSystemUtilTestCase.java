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
package com.solers.delivery.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.junit.Test;

import com.solers.delivery.util.FileSystemUtil.Stats;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class FileSystemUtilTestCase extends TestCase {
        
    public void testIsFilenameValidWindows() {
        if (System.getProperty("os.name").contains("Windows")) {
            String bad = "c:\\invalid:";
            assertFalse(FileSystemUtil.isFilenameValid(bad));
            assertFalse(new File(bad).exists());
            
            String good = "c:\\com.solers.delivery.util.FileSystemUtilTestCase";
            assertTrue(FileSystemUtil.isFilenameValid(good));
            assertFalse(new File(good).exists());
        }
    }
    
    public void testPathTree() {
        try {
            String samplePath = new File("/this/is/a/test").getCanonicalPath();
            String[] expected = System.getProperty("os.name").contains("Windows") ?
                new String[] { "C:\\", "C:\\this", "C:\\this\\is", "C:\\this\\is\\a", "C:\\this\\is\\a\\test" }
            :   new String[] { "/", "/this", "/this/is", "/this/is/a", "/this/is/a/test"};
            
            Set<String> set = new HashSet<String>(Arrays.asList(expected));
            String[] result = FileSystemUtil.pathTree(samplePath);
            for (String r : result) {
                set.remove(r);
            }
            
            assertTrue(set.isEmpty());
        } catch (IOException ioe) {
            ioe.printStackTrace();
            fail();
        }
    }
    
    public void testPathTreeCanonicalizationFailover() {
        String samplePath = System.getProperty("os.name").contains("Windows") ?
            "C:\\this\\*\\?\\test"
        :   "/this/*/?/test";
        String[] expected = System.getProperty("os.name").contains("Windows") ?
            new String[] { "C:\\", "C:\\this", "C:\\this\\*", "C:\\this\\*\\?", "C:\\this\\*\\?\\test" }
        :   new String[] { "/", "/this", "/this/*", "/this/*/?", "/this/*/?/test" };
        Set<String> set = new HashSet<String>(Arrays.asList(expected));
        String[] result = FileSystemUtil.pathTree(samplePath);
        for (String r : result) {
            set.remove(r);
        }
        assertTrue(set.isEmpty());
    }
    
    public void testPathIsChild() throws IOException {
        
        String parentDir = new File("/opt/app").getCanonicalPath();
        String[] expectTrue = new String[] {
            new File("/opt/app/child").getCanonicalPath(),
            new File("/opt/app/child/child").getCanonicalPath(),
            parentDir
        };
        String[] expectFalse = new String[] {
            new File("/opt/notapp").getCanonicalPath(),
            new File("/opt/appnot").getCanonicalPath(),
            new File("/opt").getCanonicalPath(),
            new File("/").getCanonicalPath(),
            new File("/var/something/crazy/and/obviously/not/related").getCanonicalPath()
        };
        for (String s : expectTrue) {
            assertTrue(FileSystemUtil.pathIsChild(s, parentDir));
        }
        for (String s : expectFalse) {
            assertFalse(FileSystemUtil.pathIsChild(s, parentDir));
        }
        
        File file = new File(FileSystemUtil.correctPath("code\\java\\efd\\delivery\\testPathIsChild"));
        assertTrue(FileSystemUtil.pathIsChild(FileSystemUtil.correctPath("code\\java\\efd\\delivery\\testPathIsChild"), file.getCanonicalPath()));
        assertTrue(FileSystemUtil.pathIsChild(FileSystemUtil.correctPath("code\\java\\efd\\delivery\\testPathIsChild\\foo"), file.getCanonicalPath()));
        assertFalse(FileSystemUtil.pathIsChild(FileSystemUtil.correctPath("code\\java\\efd\\delivery\\"), file.getCanonicalPath()));
        assertFalse(FileSystemUtil.pathIsChild(FileSystemUtil.correctPath("foo"), file.getCanonicalPath()));
        
        File source = new File("efd/small supplier");
        String efdPath = "efd install";
        
        assertFalse(FileSystemUtil.pathIsChild(source.getCanonicalPath(), efdPath));
    }
    
    public void testCalculateStatistics() {
        Deque<File> stack = new ArrayDeque<File>();
        try {
            File root = new File("temp");
            stack.add(root);
            File subdir1 = new File(root, "subdir1");
            subdir1.mkdirs();
            stack.add(subdir1);
            File file = new File(root, "file1.file");
            file.createNewFile();
            stack.add(file);
            File file2 = new File(subdir1, "file2.file");
            file2.createNewFile();
            stack.add(file2);
            
            Stats s = FileSystemUtil.calculateDirectoryStatistics(root);
            assertTrue(s.getFiles() == 2);
            assertTrue(s.getDirs() == 2);
            assertTrue(s.getSize() == 0);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            fail();
        } finally {
            File f = null;
            while (!stack.isEmpty() && (f = stack.removeLast()) != null) {
                if (!f.delete()) f.deleteOnExit();
            }
        }
    }
    
    @Test
    public void testCorrectPathWindows() {
        if (!System.getProperty("os.name").contains("Windows")) {
            assertTrue(true);
            return;
        }
        
        String[] input = new String[] {
            "c:\\", "c:/", "c:", "c:/blah/blah/", "c:/blah/blah\\", "c:\\blah\\blah"
        };
        
        String[] output = new String[] {
           "C:/", "C:/", "C:/", "C:/blah/blah", "C:/blah/blah", "C:/blah/blah"
        };
        
        assertTrue(input.length == output.length);
        
        for (int i = 0; i < input.length; ++i) {
            assertTrue(output[i]+" did not equal "+FileSystemUtil.correctPath(input[i]), output[i].equals(
                FileSystemUtil.correctPath(input[i])
            ));
        }
    }
    
    @Test
    public void testCorrectPathOther() throws Exception {
        if (System.getProperty("os.name").contains("Windows")) {
            assertTrue(true);
            return;
        }
        
        String[] input = new String[] {
            "./", "\\", "./blah/blah/", "./blah/blah", ".\\blah\\blah"
        };
        
        String[] output = new String[] {
           "./", "/", "./blah/blah", "./blah/blah", "./blah/blah" 
        };
        
        assertTrue(input.length == output.length);
        
        for (int i = 0; i < input.length; ++i) {
            File expected = new File(output[i]);
            File actual = new File(FileSystemUtil.correctPath(input[i]));
            assertEquals(expected.getCanonicalPath(), actual.getCanonicalPath());
        }
    }
    
    @Test
    public void testCorrectPathReflexivity() throws Exception {
        String[] inputs = new String[] {
          "./", "\\", "./blah/blah/", "\\blah\\blah", "blah\\blah", "/blah"  
        };
        
        String[] corrected = new String[inputs.length];
        for (int i = 0; i < inputs.length; ++i) {        	
            corrected[i] = FileSystemUtil.correctPath(inputs[i]);
        }
        
        for (String s : corrected) {
            assertEquals(s, FileSystemUtil.correctPath(s));
        }
    }
    
    @Test
    public void testNullCorrectPath() {
        assertNull(FileSystemUtil.correctPath(null));
        assertEquals("", FileSystemUtil.correctPath(""));
    }
    
    @Test
    public void testRelativeToEFDHome() {
        //test null
        assertEquals(FileSystemUtil.getEFDHome(), FileSystemUtil.relativeToEFDHome(null));
        //test empty string
        assertEquals(FileSystemUtil.getEFDHome(), FileSystemUtil.relativeToEFDHome(""));
        //test string with single whitespace
        assertEquals(FileSystemUtil.getEFDHome(), FileSystemUtil.relativeToEFDHome(" "));
        //test string with multiple whitespace
        assertEquals(FileSystemUtil.getEFDHome(), FileSystemUtil.relativeToEFDHome("  "));

        //expected result
        File file = new File(FileSystemUtil.getEFDHome().getAbsolutePath() + File.separator + "foo\bar\baz.txt");
        //test string with no whitespace
        assertEquals(file, FileSystemUtil.relativeToEFDHome("foo\bar\baz.txt"));
        //test string with trailing whitespace
        assertEquals(file, FileSystemUtil.relativeToEFDHome("foo\bar\baz.txt "));
        //test string with leading whitespace
        assertEquals(file, FileSystemUtil.relativeToEFDHome(" foo\bar\baz.txt"));
        //test string with leading and trailing whitespace
        assertEquals(file, FileSystemUtil.relativeToEFDHome("  foo\bar\baz.txt  "));
        //relative .
        assertEquals(file, FileSystemUtil.relativeToEFDHome("./foo\bar\baz.txt"));
        //absolute from root
        assertEquals(file, FileSystemUtil.relativeToEFDHome("/foo\bar\baz.txt"));
        //user home
        assertEquals(file, FileSystemUtil.relativeToEFDHome("~/foo\bar\baz.txt"));
        //user home
        assertEquals(file, FileSystemUtil.relativeToEFDHome("~foo\bar\baz.txt"));
        //with drive letter
        assertEquals(file, FileSystemUtil.relativeToEFDHome("C:\\foo\bar\baz.txt"));
        //with drive letter without \
        assertEquals(file, FileSystemUtil.relativeToEFDHome("C:foo\bar\baz.txt"));
        //UNC
        assertEquals(file, FileSystemUtil.relativeToEFDHome("\\foo\bar\baz.txt"));
    }
}
