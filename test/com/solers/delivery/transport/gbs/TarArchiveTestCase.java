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
package com.solers.delivery.transport.gbs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.io.FileOutputStream;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;

import com.solers.delivery.transport.gbs.poll.ExtractProgress;
import com.solers.delivery.event.ContentEvent.ContentEventAction;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class TarArchiveTestCase extends TestCase {
    private TarArchive archive;
    private TarArchive archiveWithFiles;
    private String fileName1 = "testExtractFilesToPath-1";
    private String fileName2 = "testExtractFilesToPath-2";
    private String content1 = "here are 17 bytes";
    private String content2 = "This is 16 bytes";
    
    public void setUp() throws IOException {
        System.setProperty("efd.home", ".");
        
        File file = new File("testGetContentSet.tar.gz");
        file.deleteOnExit();        
        archive = new TarArchive(file);
        List<GbsFile> files = Collections.emptyList();
        archive.addFilesToArchive(files, "ContentSetName", "1-1234");  
        
        archiveWithFiles = new TarArchive(file);
        
        
        File f1 = getFile(fileName1, content1.getBytes());       
        File f2 = getFile(fileName2, content2.getBytes());
        
        List<GbsFile> gbsfiles = Arrays.asList(new GbsFile(f1, f1.getName()), 
        		                            new GbsFile(f2, f2.getName()));
        archiveWithFiles.addFilesToArchive(gbsfiles, "ContentSetName", "1-1234"); 
    }
    
    public void testGetContentSet() throws IOException {    
        assertEquals("ContentSetName", archive.getContentSet());
    }
    
    public void testGetSyncId() throws IOException {     
        assertEquals("1-1234", archive.getSyncKey());
    }
    
    public void testExtractFilesToPath() throws IOException {    	
        MockProgress c = new MockProgress();
        File dir = new File("testExtractFilesToPath-output");
        dir.mkdirs();
        
        archiveWithFiles.extractFilesToPath(dir.getAbsolutePath(), c);
        
        File [] contents = dir.listFiles();
        
        assertEquals(2, contents.length);        
        assertContains(fileName1, contents);
        assertContains(fileName2, contents);
        assertTrue(c.success.contains(fileName1));
        assertTrue(c.success.contains(fileName2));        
        FileUtils.deleteDirectory(dir);
    }
    
    public void testGetName() {
    	assertEquals("testGetContentSet.tar.gz", archive.getName());    	
    }
    
    public void testGetFileNames() {
       List<String> nofiles = archive.getFileNames();
       assertEquals(0, nofiles.size());
       
       List<String> fileNames = archiveWithFiles.getFileNames();       
       assertEquals(2, fileNames.size());
       assertTrue(fileNames.contains(fileName1));
       assertTrue(fileNames.contains(fileName2));
    }
    
    public void testGetSize() {
        assertEquals(0, archive.getSize());        
        assertEquals((content1.length() + content2.length()), archiveWithFiles.getSize());
    }
    
    private File getFile(String path, byte[] content) throws IOException {
        File file = new File(path);
        file.createNewFile();      
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(content);
        fos.close();
        file.deleteOnExit();
        return file;
    }
    
    protected void assertContains(String name, File [] files) {
        for (File f : files) {
            if (f.getName().equals(name)) {
                return;
            }
        }
        fail();
    }
    
    class MockProgress implements ExtractProgress {
        List<String> success = new ArrayList<String>();
        List<String> skipped = new ArrayList<String>();
        @Override
        public void skipped(String path, long size) {
            skipped.add(path);
        }
        
        @Override
        public void success(String path, long size, ContentEventAction action) {
            success.add(path);
        }
    }
}
