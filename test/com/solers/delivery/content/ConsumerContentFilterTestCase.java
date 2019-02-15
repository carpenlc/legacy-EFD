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
package com.solers.delivery.content;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import com.solers.delivery.domain.ConsumerContentSet;
import com.solers.delivery.domain.FileFilter;
import com.solers.delivery.domain.FileFilter.Pattern;
import com.solers.delivery.inventory.node.AbstractNode;
import com.solers.delivery.inventory.node.Node;
import com.solers.util.unit.FileSizeUnit;


public class ConsumerContentFilterTestCase extends TestCase {
    
    public void testNoFilter() {
        ConsumerContentFilter f = new ConsumerContentFilter(create(Long.MAX_VALUE, FileSizeUnit.GIGABYTES));
        MockNode n = new MockNode("anything", false);
        assertTrue(f.accept(n));
        n.setDirectory(true);
        assertTrue(f.accept(n));
    }
    
    public void testBeginsWith() {
        FileFilter filefilter = create("BEGIN", Pattern.BEGINS, false);
        MockNode[] nodes = new MockNode[] {
            new MockNode("beg", false),
            new MockNode("BEGIN", false),
            new MockNode("BEGINblah", false),
            new MockNode("andNowForSomethingCompletelyDifferent", false),
            new MockNode("blahBEGIN", false),
            new MockNode("blahBEGINblah", false)
        };
        boolean[] results = {
            true,
            false,
            false,
            true,
            true,
            true
        };
        
        testFileFilter(filefilter, nodes, results);
    }

    public void testContains() {
        FileFilter f = create("CONT", Pattern.CONTAINS, false);
        MockNode[] nodes = new MockNode[] {
            new MockNode("CONT", false),
            new MockNode("cont", false),
            new MockNode("blahCONTblah", false),
            new MockNode("blahblahCONT", false),
            new MockNode("CONTblahblah", false)
        };
        boolean[] results = {
            false,
            true,
            false,
            false,
            false
        };
        testFileFilter(f, nodes, results);
    }
    
    public void testEndsWith() {
        FileFilter f = create("END", Pattern.ENDS, false);
        MockNode[] nodes = new MockNode[] {
            new MockNode("END", false),
            new MockNode("end", false),
            new MockNode("ENDblahblah", false),
            new MockNode("blahblahEND", false),
            new MockNode("blahENDblah", false)
        };
        boolean[] results = {
            false,
            true,
            true,
            false,
            true
        };
        testFileFilter(f, nodes, results);
    }
    
    public void testRegularExpression() {
        FileFilter f = create("^(A|B).*?$", Pattern.REGEX, false);
        MockNode[] nodes = new MockNode[] {
            new MockNode("Absolutely!", false),
            new MockNode("Brilliant!", false),
            new MockNode("Chilling!", false),
            new MockNode("aA", false),
            new MockNode("bB", false),
            new MockNode("abABab", false),
        };
        boolean[] results = {
            false,
            false,
            true,
            true,
            true,
            true
        };
        testFileFilter(f, nodes, results);
    }
    
    public void testMaxSizeRestriction() {
        ConsumerContentSet ccs = create(100, FileSizeUnit.BYTES);
        MockNode[] nodes = new MockNode[] {
            new MockNode("mock", 0),
            new MockNode("mock", 1),
            new MockNode("mock", 10),
            new MockNode("mock", 50),
            new MockNode("mock", 99),
            new MockNode("mock", 100),
            new MockNode("mock", 101),
            new MockNode("mock", 123468L)
        };
        boolean[] results = new boolean[] {
            true,
            true,
            true,
            true,
            true,
            true,
            false,
            false
        };
        doTests("Max size restriction ", ccs, nodes, results);
    }
    
    private void testFileFilter(FileFilter filefilter, MockNode[] nodes, boolean[] exclusiveResults) {        
        assertFalse(filefilter.isInclusive());
        ConsumerContentSet ccs = create(Long.MAX_VALUE, FileSizeUnit.GIGABYTES);
        addFilter(ccs, filefilter);
        assertEquals(1, ccs.getFileFilters().size());
        
        doTests("Exclusive test ", ccs, nodes, exclusiveResults);
        filefilter.setInclusive(true);
        
        boolean[] inclusiveResults = new boolean[exclusiveResults.length];
        for (int i = 0; i < exclusiveResults.length; ++i) {
            inclusiveResults[i] = !exclusiveResults[i];
        }
        
        doTests("Inclusive test ", ccs, nodes, inclusiveResults);
    }
    
    private void doTests(String testName, ConsumerContentSet ccs, MockNode[] nodes, boolean[] results) {
        
        assertNotNull(nodes);
        assertNotNull(results);
        assertEquals(nodes.length, results.length);

        ConsumerContentFilter f = new ConsumerContentFilter(ccs);
        
        for (int i = 0; i < nodes.length; ++i) {
            assertEquals(testName + i, results[i], f.accept(nodes[i]));
        }
    }
    
    private void addFilter(ConsumerContentSet ccs, FileFilter f) {
        List<FileFilter> filters = ccs.getFileFilters();
        if (filters == null || filters.isEmpty()) filters = new ArrayList<FileFilter>();
        filters.add(f);
        ccs.setFileFilters(filters);        
    }
    
    private ConsumerContentSet create(long maxFileSize, FileSizeUnit unit) {
        ConsumerContentSet ccs = new ConsumerContentSet();
        ccs.setMaxFileSize(maxFileSize);
        ccs.setMaxFileSizeUnit(unit);
        return ccs;
    }
    
    private FileFilter create(String pattern, Pattern type, boolean inclusive) {
        FileFilter f = new FileFilter();
        f.setPattern(pattern);
        f.setPatternType(type);
        f.setInclusive(inclusive);
        return f;
    }
    
    private static class MockNode extends AbstractNode {
        private final String path;
        private long timestamp = System.currentTimeMillis();
        private long size = 0;
        private boolean directory = false;
        
        private final Runnable noop = new Runnable() { 
            public void run() {
                //no operation
            }
        };
        
        private Runnable addEvent = noop;
        private Runnable removeEvent = noop;
        private Runnable updateEvent = noop;
        
        public MockNode(String path, boolean directory) {
            this.path = path.replace('/', File.separatorChar).replace('\\', File.separatorChar);
            this.directory = directory;
        }
        
        public MockNode(String path, long size) {
            this(path, false);
            this.setSize(size);
        }

        @Override
        public List<Node> getChildren() {
            return Collections.emptyList();
        }

        @Override
        public String getName() {
            int lastSep = path.lastIndexOf(File.separator);
            if (lastSep < 0) return path;
            return path.substring(path.lastIndexOf(File.separator));
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        protected long nodeSize() {
            return size;
        }

        @Override
        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public boolean isDirectory() {
            return directory;
        }
        
        public Node getParent() {
            return new MockNode(new File(path).getParent(), directory);
        }
        
        public void setTimestamp(long ts) {
            this.timestamp = ts;
        }
        
        public void setSize(long size) {
            this.size = size;
        }
        
        public void setDirectory(boolean directory) {
            this.directory = directory;
        }
        
        public void onAdd(Runnable r) {
            addEvent = (r != null) ? r : noop;
        }
        
        public void onRemove(Runnable r) {
            removeEvent = (r != null) ? r : noop;
        }
        
        public void onUpdate(Runnable r) {
            updateEvent = (r != null) ? r : noop;
        }
        
        public void onEvents(Runnable addEvent, Runnable removeEvent, Runnable updateEvent) {
            onAdd(addEvent);
            onRemove(removeEvent);
            onUpdate(updateEvent);
        }
        
        public void add() {
            addEvent.run();
        }
        
        public void remove() {
            removeEvent.run();
        }
        
        public void update() {
            updateEvent.run();
        }
        
        @Override
        public InputStream openStream() {
            return null;
        }
    }
}
