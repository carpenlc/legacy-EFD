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
package com.solers.delivery.inventory;


import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import com.solers.delivery.inventory.node.AbstractNode;
import com.solers.delivery.inventory.node.Node;

public class MockNode extends AbstractNode {
    private final String path;
    private int accuracy = AbstractNode.DEFAULT_ACCURACY;
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
    
    public MockNode(int accuracy) {
        this.path = "root";
        this.accuracy = accuracy;
    }
    
    public MockNode(String path, boolean directory) {
        this.path = path.replace('/', File.separatorChar).replace('\\', File.separatorChar);
        this.directory = directory;
    }
    
    public MockNode(String path, long size) {
        this(path, false);
        this.setSize(size);
    }

    @Override
    protected int getTimestampAccuracy() {
        return accuracy;
    }
    
    public void setTimestampAccuracy(int accuracy) {
        this.accuracy = accuracy;
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
