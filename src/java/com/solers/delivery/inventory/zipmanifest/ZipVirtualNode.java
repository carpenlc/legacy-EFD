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
package com.solers.delivery.inventory.zipmanifest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.solers.delivery.inventory.node.AbstractNode;
import com.solers.delivery.inventory.node.Node;

public class ZipVirtualNode extends AbstractNode {
    private final ZipFileInventory inventory;
    private final Node parent;
    private final String name;
    private final File base;
    private List<Node> children;
    
    public ZipVirtualNode(ZipFileInventory inventory, Node parent, String name, File base) {
        this.inventory = inventory;
        this.parent = parent;
        this.name = name;
        this.base = base;
    }

    @Override
    protected int getTimestampAccuracy() {
        return ZipFileInventory.TS_ACCURACY;
    }
    
    @Override
    public List<Node> getChildren() {
        if (children == null) {
            children = new ArrayList<Node>();
            for (File f : base.listFiles()) {
                children.add(inventory.loadNode(f, this));
            }
        }
        
        return children;
    }
    
    @Override
    public Node getChild(String name) {
        File f = new File(base, name);
        if (f.exists()) {
            return inventory.loadNode(f, this);
        }
        
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Node getParent() {
        return parent;
    }

    @Override
    public String getPath() {
        return ((parent != null) ? parent.getPath() + File.separator : "") + getName();
    }

    @Override
    public long getTimestamp() {
        return base.lastModified();
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    @Override
    public InputStream openStream() throws IOException {
        return null;
    }
}
