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
package com.solers.delivery.inventory.comparer;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import junit.framework.JUnit4TestAdapter;
import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.solers.delivery.inventory.MockEventHandler;
import com.solers.delivery.inventory.MockInventory;
import com.solers.delivery.inventory.MockNode;
import com.solers.delivery.inventory.Writer;
import com.solers.delivery.inventory.fs.FileNode;
import com.solers.delivery.inventory.fs.FileSystemInventory;
import com.solers.delivery.inventory.index.ReaderFactory;
import com.solers.delivery.inventory.index.WriterFactory;
import com.solers.delivery.inventory.node.Node;

/**
 * @author JGimourginas
 */
public class NodeComparerTest {

    private Comparer comparer = null;
    //file system specific objects
    private File rootDirectory1 = null;
    private File subDirectory1 = null;
    private File file12 = null;
    private File rootDirectory2 = null;

    @Before
    public void setup() {
        //Deque q1 = new FileBackedQueue(5000, 2500, "target/inventories/pagefiles");
        //Deque q2 = new FileBackedQueue(5000, 2500, "target/inventories/pagefiles");
        comparer = new UnorderedInventoryComparer();
    }
    
    @Test
    public void testAttributesEqual() {
        NodeListener attributesEqualHandler = new MockEventHandler() {
            @Override
            public void onStart() { 
                Assert.assertTrue(true);
            }

            @Override
            public void onStop() {
                Assert.assertTrue(true);
            }
        };
        
        Runnable fail = new Runnable() {
            public void run() {
                Assert.fail();
            }
        };
        
        Runnable pass = new Runnable() {
            public void run() {
                Assert.assertTrue(true);
            }
        };
        
        MockNode dir1 = new MockNode("/test/dir", true);
        MockNode dir2 = new MockNode("/test/dir", true);
        dir1.onEvents(fail, fail, fail);
        dir2.onEvents(fail, fail, fail);
        
        /*
         * All events fail - directories should not trigger
         * adds, updates, or removes.
         */
        comparer.compare(new MockInventory(dir1), new MockInventory(dir2), attributesEqualHandler);
        
        dir1.setSize(512);
        dir2.setSize(4096);
        
        /*
         * Ensure that directories of different sizes are not
         * triggering a replication bug.
         */
        comparer.compare(new MockInventory(dir1), new MockInventory(dir2), attributesEqualHandler);
        
        MockNode file1 = new MockNode("/test/dir", false);
        file1.onEvents(pass, fail, fail);
        dir1.onRemove(pass);
        file1.setTimestamp(Long.MAX_VALUE);
        
        /*
         * Comparing a file to a dir of the same name should
         * force an update
         */
        comparer.compare(new MockInventory(file1), new MockInventory(dir1), attributesEqualHandler);
        
        file1.onAdd(fail);
        file1.onRemove(pass);
        dir1.onEvents(pass, fail, fail);
        
        /*
         * Same as above, but in reverse.
         */
        comparer.compare(new MockInventory(dir1), new MockInventory(file1), attributesEqualHandler);
        
        MockNode file2 = new MockNode("/test/dir", false);
        file1.setTimestamp(file2.getTimestamp());
        file2.onEvents(fail, fail, fail);
        
        /*
         * If timestamps are equal, do not send an event
         */
        comparer.compare(new MockInventory(file1), new MockInventory(file2), attributesEqualHandler);
        
        file2.setTimestamp(Long.MAX_VALUE);
        file2.onEvents(pass, fail, fail);
        file1.onEvents(fail, pass, fail);
        
        /*
         * If timestamps are not equal, update
         */
        comparer.compare(new MockInventory(file2), new MockInventory(file1), attributesEqualHandler);
        
    }

    /** NULL TESTS **/

    /**
     * Creates files to use for NULL TESTS section of test cases
     * @throws IOException if files cannot be created
     */
    private void nullTestsFileSystemSetup() throws IOException {
        rootDirectory1 = new File("test/rootDir");
        rootDirectory1.mkdirs();
        rootDirectory1.deleteOnExit();
        new FileSystemInventory(rootDirectory1);
        subDirectory1 = new File(rootDirectory1, "subDir");
        subDirectory1.mkdirs();
        File.createTempFile("first", null, rootDirectory1);
        file12 = File.createTempFile("second", null, subDirectory1);
    }

    /**
     * desired is single file
     * existing is null
     * expect an add(desired) event
     */
    @Test
    public void desiredNodeExistingNull() throws IOException {
        nullTestsFileSystemSetup();
        Node desiredRoot = new FileNode(file12, file12);
        Node existingRoot = null;
        TrackingEventHandler handler = new TrackingEventHandler();
        comparer.compare(new MockInventory(desiredRoot), new MockInventory(existingRoot), handler);
        List<String> events = handler.getEvents();
        assertEquals("START", events.get(0));
        assertEquals("ADD " + desiredRoot.getName(), events.get(1));
        assertEquals("STOP", events.get(2));
    }

    /**
     * desired is directory with 1 sub file
     * existing is null
     * expect 2 add(desired) events
     */
    @Test
    public void desiredDirExistingNull() throws IOException {
        nullTestsFileSystemSetup();
        FileNode desiredRoot = new FileNode(subDirectory1, subDirectory1);
        FileNode existingRoot = null;
        TrackingEventHandler handler = new TrackingEventHandler();
        comparer.compare(new MockInventory(desiredRoot), new MockInventory(existingRoot), handler);
        List<String> events = handler.getEvents();
        assertEquals("START", events.get(0));
        assertEquals("ADD " + desiredRoot.getName(), events.get(1));
       // assertEquals("ADD " + desiredRoot.getChildren().get(0).getPath(), events.get(2));
        assertEquals("STOP", events.get(3));
    }

    /**
     * desired is direcotry with 1 sub dir, 1 sub file
     * sub dir contains 1 sub file
     * existing is null
     * expect 4 add(desired) events
     */
    @Test
    public void desiredTreeExistingNull() throws IOException {
        nullTestsFileSystemSetup();
        FileNode desiredRoot = new FileNode(rootDirectory1, rootDirectory1);
        FileNode existingRoot = null;
        TrackingEventHandler handler = new TrackingEventHandler();
        comparer.compare(new MockInventory(desiredRoot), new MockInventory(existingRoot), handler);
        List<String> events = handler.getEvents();
        assertEquals("START", events.get(0));
        assertEquals("ADD " + desiredRoot.getPath(), events.get(1));
        //assertEquals("ADD " + desiredRoot.getChildren().get(0).getPath(), events.get(2));
        //assertEquals("ADD " + desiredRoot.getChildren().get(1).getPath(), events.get(3));
        //get path of file in sub directory
        //assertEquals("ADD " + desiredRoot.getChildren().get(1).getChildren().get(0).getPath(), events.get(4));
        assertEquals("STOP", events.get(5));
    }

    /**
     * desired is null
     * existing is a single file
     * expect a remove(existing) event
     */
    @Test
    public void desiredNullExistingNode() throws IOException {
        nullTestsFileSystemSetup();
        Node desiredRoot = null;
        Node existingRoot = new FileNode(file12, file12);
        TrackingEventHandler handler = new TrackingEventHandler();
        comparer.compare(new MockInventory(desiredRoot), new MockInventory(existingRoot), handler);
        List<String> events = handler.getEvents();
        assertEquals("START", events.get(0));
        assertEquals("REMOVE " + existingRoot.getPath(), events.get(1));
        assertEquals("STOP", events.get(2));
    }

    /**
     * desired is null
     * existing is directory with one sub file
     * expect 1 remove(existing) event - should remove directory but not individual children as that is implied on
     * directory removal
     */
    @Test
    public void desiredNullExistingDir() throws IOException {
        nullTestsFileSystemSetup();
        Node desiredRoot = null;
        Node existingRoot = new FileNode(subDirectory1, subDirectory1);
        TrackingEventHandler handler = new TrackingEventHandler();
        comparer.compare(new MockInventory(desiredRoot), new MockInventory(existingRoot), handler);
        List<String> events = handler.getEvents();
        assertEquals("START", events.get(0));
        assertEquals("REMOVE " + existingRoot.getPath(), events.get(1));
        assertEquals("STOP", events.get(2));
    }

    /**
     * desired is null
     * existing is directory with sub dir and sub file
     * sub dir has its own sub file
     * expect a single remove of the root
     */
    @Test
    public void desiredNullExistingTree() throws IOException {
        nullTestsFileSystemSetup();
        Node desiredRoot = null;
        Node existingRoot = new FileNode(rootDirectory1, rootDirectory1);
        TrackingEventHandler handler = new TrackingEventHandler();
        comparer.compare(new MockInventory(desiredRoot), new MockInventory(existingRoot), handler);
        List<String> events = handler.getEvents();
        assertEquals("START", events.get(0));
        assertEquals("REMOVE " + existingRoot.getPath(), events.get(1));
        assertEquals("STOP", events.get(2));
    }
    
    /**
     * If a file and a directory have the same attributes, comparer should still fail
     * @throws IOException
     */
    @Test
    public void dirVsFileTest() throws IOException {
        //TODO Implement this test
    }
    
    //TODO: move this test to int - this is really an integration test
    @Test
    public void testInventoryNg() throws IOException {
        File tmp = File.createTempFile("test", "dir");
        File tempDir = new File(tmp.getParent());
        TestCase.assertTrue(tempDir.exists());
        
        File root = new File(tempDir, "testInventoryNg");
        try {
            root.mkdirs();
            TestCase.assertTrue(root.exists() && root.isDirectory());
            
            for (int i = 0; i < 3; ++i) {
                File f = new File(root, "file" + i);
                f.createNewFile();
                File d = new File(root, "dir" + i);
                d.mkdir();
                for (int j = 0; j < 3; ++j) {
                    File f2 = new File(d, "file" + j);
                    f2.createNewFile();
                
                }
            }
            
            File emptyDir = new File(root, "emptyDir");
            emptyDir.mkdir();
            Writer writer = WriterFactory.newInstance(new FileSystemInventory(root));
            File index = new File(tempDir, "test.index");
            writer.write(index);
            Node inventoryRoot = ReaderFactory.newInstance(index).root();
            assertEquals(inventoryRoot.getName(), root.getName());
            
            comparer.compare(new MockInventory(inventoryRoot), new MockInventory(new FileNode(root, root)), new NodeListener() {
                @Override
                public void onAdd(Node node) {
                    TestCase.fail("File systems should be equal; no operations should be invoked: " + node.getPath());
                }
                
                @Override
                public void onRemove(Node node) {
                    TestCase.fail("File systems should be equal; no operations should be invoked: " + node.getPath());
                }
                
                @Override
                public void onUpdate(Node node) {
                    TestCase.fail("File systems should be equal; no operations should be invoked: " + node.getPath());
                }
                
                @Override
                public void onStart() {}
                @Override
                public void onStop() {}
            });
            
            File deleteMe = new File(root, "dir1");
            FileUtils.deleteDirectory(deleteMe);
            
            comparer.compare(new MockInventory(inventoryRoot), new MockInventory(new FileNode(root, root)), new NodeListener() {
                @Override
                public void onAdd(Node node) {
                    TestCase.assertTrue(node.getPath().startsWith("testInventoryNg" + File.separator + "dir1"));
                }
                
                @Override
                public void onRemove(Node node) {
                    TestCase.fail("File systems should be equal; no operations should be invoked: " + node.getPath());
                }
                
                @Override
                public void onUpdate(Node node) {
                    TestCase.fail("File systems should be equal; no operations should be invoked: " + node.getPath());
                }              
                
                @Override
                public void onStart() {}
                @Override
                public void onStop() {}
            });
            
        } finally {
            try {
                FileUtils.deleteDirectory(root);
            } catch (IOException ioe) {
                System.err.println("Delete failed; ignoring it.");
            }
        }
    }

    @After
    public void teardown() {
        try {
            if (rootDirectory1 != null) {
                FileUtils.deleteDirectory(rootDirectory1);
            }
        } catch (IOException e) {
            //oh well - test still passes
        } 
        
        try {
            if (rootDirectory2 != null) {
                FileUtils.deleteDirectory(rootDirectory2);
            }
        } catch (IOException e) {
            //oh well - test still passes
        } 
        
        try {
            FileUtils.deleteDirectory(new File("temp"));
        } catch (IOException e) {
            //oh well - test still passes
        } 
        
        try {
            File file = new File("test");
            file.deleteOnExit();
            FileUtils.deleteDirectory(file);
        } catch (IOException e) {
            //oh well - test still passes
        }
    }

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(NodeComparerTest.class);
    }
    
    static class TrackingEventHandler implements NodeListener {
        private List<String> events = null;

        public TrackingEventHandler() {
            events = new ArrayList<String>();
        }

        /**
         * Processes an Add event given the node to add
         * @param node Node to add
         */
        public void onAdd(Node node) {
            events.add("ADD " + node.getPath());
       }

        /**
         * Processes an Update event given the node with which to update an existing node
         * @param node Node with which to update
         */
        public void onUpdate(Node node) {
            events.add("REFRESH " + node.getPath());
        }

        /**
         * Processes a Remove event given the node to remove
         * @param node Node to remove
         */
        public void onRemove(Node node) {
            events.add("REMOVE " + node.getPath());
        }

        /**
         * Processes a Start event
         */
        public void onStart() {
            events.add("START");
        }

        /**
         * Processes a Stop event
         */
        public void onStop() {
            events.add("STOP");
        }

        /**
         * returns all captured events
         * @return List<String> of captured events
         */
        public List<String> getEvents() {
            return events;
        }
    }

}
