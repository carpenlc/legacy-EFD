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
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

import com.solers.delivery.inventory.node.AbstractNode;
import com.solers.delivery.inventory.node.Node;

public class ZipFileNode extends AbstractNode {
    private static final Logger log = Logger.getLogger(ZipFileNode.class);
    
    private final ZipFile zf;
    private final Node parent;
    private final String name;
    private final ZipEntry ze;
    private final long size;

    public ZipFileNode(ZipFileInventory inventory, ZipFile zf, Node parent, String name) {
        this.zf = zf;
        this.parent = parent;
        this.name = name;
        
        this.ze = ZipFileInventory.getEntry(
            zf, getPath().substring(inventory.getRootName().length() + 1));
        
        long size = 0;
        try {
            size = Long.parseLong(ze.getComment());
        } catch (NumberFormatException nfe) {
            size = 0;
            log.warn(zf + "!/" + ze.getName() + " has unparseable size information");
        } finally {
            this.size = size;
        }
    }
    
    @Override
    protected int getTimestampAccuracy() {
        return ZipFileInventory.TS_ACCURACY;
    }
    
    @Override
    public List<Node> getChildren() {
        return null;
    }
    
    @Override
    public Node getChild(String name) {
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
        return parent.getPath() + File.separator + getName();
    }

    @Override
    public long getTimestamp() {
        return ze.getTime();
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public InputStream openStream() throws IOException {
        return zf.getInputStream(ze);
    }

    @Override
    public long getSize() {
        return size;
    }
}
