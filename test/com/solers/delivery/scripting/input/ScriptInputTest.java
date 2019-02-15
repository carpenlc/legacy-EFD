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
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.io.FilenameUtils;
import org.junit.Test;

import com.solers.delivery.util.FileSystemUtil;

/**
 * @author mchen
 *
 */
public class ScriptInputTest extends TestCase {
    private final static String VALID_XML = "<input><directoryList><directory location=\"./scripts\" arguments=\"foo bar\"/></directoryList></input>";

    @Test
    public void testBaseRecord() {
        ScriptInput.BaseRecord record = new ScriptInput.BaseRecord("foo", "bar", "baz");
        
        File expectedFile = new File(FilenameUtils.normalize(FileSystemUtil.getEFDHome().getAbsolutePath() + File.separator + "foo"));
        
        assertEquals(".getLocation() should return the same path value", expectedFile, record.getLocation());
        assertEquals(".getLanguage() should return \"bar\"", "bar", record.getLanguage());
        assertEquals(".getArguments() should return \"baz\"", "baz", record.getArguments());
    }

    @Test
    public void testDirectoryRecord() {
        ScriptInput.DirectoryRecord record = new ScriptInput.DirectoryRecord("foo", "bar", "baz");
        File expectedFile = new File(FilenameUtils.normalize(FileSystemUtil.getEFDHome().getAbsolutePath() + File.separator + "foo"));
        
        assertEquals(".getLocation() should return path relative to efd.home", expectedFile, record.getLocation());
        assertEquals(".getLanguage() should return \"bar\"", "bar", record.getLanguage());
        assertEquals(".getArguments() should return \"baz\"", "baz", record.getArguments());

        assertNotNull(record.getFilenames());
        assertTrue(record.getFilenames() instanceof List);
        assertEquals(".getFilenames() should return an empty list", 0, record.getFilenames().size());
        assertEquals(".getGroupId() should return the default id 0", 0, record.getGroupId());

        List<String> list = new ArrayList<String>();
        list.add("foo");
        record.addFilenames(list);
        
        assertEquals("List size should be 1", 1, record.getFilenames().size());
        assertEquals(".hasFilename() should return true", true, record.hasFilename("foo"));
        assertEquals(".hasFilename() should return false", false, record.hasFilename("bar"));
        
        record.setGroupId(100);
        assertEquals(".getGroupId() should return 100", 100, record.getGroupId());
    }
    
    @Test
    public void testFromXML() {
        try {
            ScriptInput.fromXml("");
            fail("Passing in empty string to .fromXml() should throw an IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            //do nothing. expected.
        }

        try {
            ScriptInput.fromXml(" ");
            fail("Passing in a string with only spaces to .fromXml() should throw an IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            //do nothing. expected.
        }
    
        try {
            ScriptInput.fromXml(null);
            fail("Passing in null to .fromXml() should throw an IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            //do nothing. expected.
        }
        
        try {
            ScriptInput.fromXml("foobar");
            fail("Passing in invalid xml to .fromXml() should throw an IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            //do nothing. expected.
        }

        File expectedFile = new File(FilenameUtils.normalize(FileSystemUtil.getEFDHome().getAbsolutePath() + File.separator + "scripts"));
        ScriptInput input = ScriptInput.fromXml(VALID_XML);
        Object[] list = input.getDirectoryList().toArray();
        assertEquals("should have 1 directory in the list.", 1, list.length);

        ScriptInput.DirectoryRecord record = (ScriptInput.DirectoryRecord)list[0];
        assertEquals("language should be null since it's not set", null, record.getLanguage());
        assertEquals("arguments should be the same as those specified in the xml", "foo bar", record.getArguments());
        assertEquals("group id should be the default value of 0", 0, record.getGroupId());
        assertEquals("location should be relative to efd.home", expectedFile, record.getLocation());
    }
    
    @Test
    public void testReadFromFile() {
        try {
            ScriptInput.readFromFile(null);
            fail("Passing null to .readFromFile() should have thrown a FileNotFoundException");
        } catch (FileNotFoundException e) {
            //do nothing. expected.
        }

        try {
            ScriptInput.readFromFile("");
            fail("Passing null to .readFromFile() should have thrown a FileNotFoundException");
        } catch (FileNotFoundException e) {
            //do nothing. expected.
        }
    }
}
