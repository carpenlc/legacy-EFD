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
package com.solers.delivery.inventory.fs;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.JUnit4TestAdapter;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.solers.delivery.inventory.node.Node;

/**
 * @author JGimourginas
 */
public class FileNodeTest {

    private File rootDirectory = null;
    private File subDirectory = null;
    private File firstLevelFile = null;
    private File secondLevelFile = null;
    private FileNode rootDirectoryNode = null;
    private FileNode subDirectoryNode = null;
    private FileNode firstLevelFileNode = null;
    private FileNode secondLevelFileNode = null;

    @Before
    public void setup() throws IOException {
        rootDirectory = new File("test/rootDir");
        rootDirectory.mkdirs();
        rootDirectory.deleteOnExit();
        subDirectory = new File(rootDirectory, "subDir");
        subDirectory.mkdirs();
        firstLevelFile = File.createTempFile("first", null, rootDirectory);
        secondLevelFile = File.createTempFile("second", null, subDirectory);
        rootDirectoryNode = new FileNode(rootDirectory, rootDirectory);
        subDirectoryNode = new FileNode(subDirectory,  rootDirectory);
        firstLevelFileNode = new FileNode(firstLevelFile, rootDirectory);
        secondLevelFileNode = new FileNode(secondLevelFile, rootDirectory);
    }

    @Test
    public void getName() {
        assertEquals(rootDirectory.getName(), rootDirectoryNode.getName());
        assertEquals(subDirectory.getName(), subDirectoryNode.getName());
        assertEquals(firstLevelFile.getName(), firstLevelFileNode.getName());
        assertEquals(secondLevelFile.getName(), secondLevelFileNode.getName());
    }

    @Test
    public void getChildren() {
        //file nodes should return empty list, not null
        assertEquals(new ArrayList<Node>(), firstLevelFileNode.getChildren());
        assertEquals(new ArrayList<Node>(), secondLevelFileNode.getChildren());
        //test root directory
        testDirChildren(rootDirectory, rootDirectoryNode);
        //test sub directory
        testDirChildren(subDirectory, subDirectoryNode);
    }

    private void testDirChildren(File dir, FileNode dirNode) {
        List<Node> nodeChildren = dirNode.getChildren();
        File[] fileChildren = dir.listFiles();
        int i = 0;
        for (Node node : nodeChildren) {
            assertEquals(fileChildren[i++].getName(), node.getName());
        }
    }

    @Test
    public void getSize() {
        assertEquals(firstLevelFile.length(), firstLevelFileNode.getSize());
        assertEquals(secondLevelFile.length(), secondLevelFileNode.getSize());
        assertEquals(0, rootDirectoryNode.getSize());
        assertEquals(0, subDirectoryNode.getSize());
    }

    @Test
    public void getTimestamp() {
        assertEquals(firstLevelFile.lastModified(), firstLevelFileNode.getTimestamp());
        assertEquals(secondLevelFile.lastModified(), secondLevelFileNode.getTimestamp());
        assertEquals(rootDirectory.lastModified(), rootDirectoryNode.getTimestamp());
        assertEquals(subDirectory.lastModified(), subDirectoryNode.getTimestamp());
    }

    @Test
    public void getPath() {
        assertEquals("rootDir" + File.separator + firstLevelFile.getName(), firstLevelFileNode.getPath());
        assertEquals("rootDir" + File.separator + "subDir" + File.separator + secondLevelFile.getName(), secondLevelFileNode.getPath());
        assertEquals("rootDir", rootDirectoryNode.getPath());
        assertEquals("rootDir" + File.separator + "subDir", subDirectoryNode.getPath());
    }

    @Test
    public void canHaveChildren() {
        assertEquals(firstLevelFile.isDirectory(), firstLevelFileNode.isDirectory());
        assertEquals(secondLevelFile.isDirectory(), secondLevelFileNode.isDirectory());
        assertEquals(rootDirectory.isDirectory(), rootDirectoryNode.isDirectory());
        assertEquals(subDirectory.isDirectory(), subDirectoryNode.isDirectory());
    }

    @After
    public void teardown() {
        try {
            File file = new File("test");
            file.deleteOnExit();
            FileUtils.deleteDirectory(file);
        } catch (Exception e) {
            //oh well - test still passes
        }
    }

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(FileNodeTest.class);
    }
}
