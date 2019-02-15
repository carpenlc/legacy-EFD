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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

import com.solers.delivery.inventory.Inventory;
import com.solers.delivery.inventory.node.FilteredNode;
import com.solers.delivery.inventory.node.Node;
import com.solers.util.Filter;

public class ZipFileInventory implements Inventory {
    private final ZipFile zf;
    private final File source;
    private final ZipFileCacheEntry key;
    private final String rootName;
    private final File temp;
    private Filter<Node> filter = null;
    
    protected static final Logger log = Logger.getLogger(ZipFileInventory.class);
    protected static final int TS_ACCURACY = 2000;
    
    private static final String ARCHIVE_SIZE = "archive.size";
    
    private static final Set<String> properties = Collections.unmodifiableSet(
        new HashSet<String>(Arrays.asList(ARCHIVE_SIZE)));
    
    public ZipFileInventory(File archive, String rootName) {
        try {
            zf = new ZipFile(archive);
            this.source = archive;
        } catch (IOException ioe) {
            throw new IllegalArgumentException("Expected a zip archive", ioe);
        }

        this.rootName = calculateRootName(archive.getName(), rootName);
        //keep the key so that if file timestamp changes, we close the correct entry
        this.key = new ZipFileCacheEntry(archive);
        this.temp = ResourceManager.submit(key, archive, this.rootName);
    }
    
    private String calculateRootName(final String file, final String root) {
        String finalRootName = root;
        if (finalRootName == null || finalRootName.trim().equals("")) {
            finalRootName = file;
            int dot = finalRootName.lastIndexOf('.');
            if (dot > 0) finalRootName = new String(
                finalRootName.substring(0, dot));
        }
        return finalRootName;
    }
    
    
    @Override
    public void close() {    
        ResourceManager.close(key);
        try {
            zf.close();
        } catch (IOException ioe) {
            //don't care
        }
    }

    @Override
    public Object getProperty(String name) {
        if (ARCHIVE_SIZE.equals(name)) {
            return source.length();
        }
        return null;
    }

    @Override
    public String getRootName() {
        return this.rootName;
    }

    @Override
    public Set<String> properties() {
        return properties;
    }

    @Override
    public Node read(final String path) {
        File f = new File(temp, path);
        if (f.exists()) {
            File parent = new File(path).getParentFile();
            return FilteredNode.applyFilter(
              loadNode(f, parent != null ? read(parent.getPath()) : root()), filter);
        }
        return null;
    }

    @Override
    public Node root() {
        return FilteredNode.applyFilter(
            new ZipVirtualNode(this, null, rootName, new File(temp, rootName)),
            filter);
    }

    @Override
    public void useFilter(Filter<Node> filter) {
        this.filter = filter;
    }

    static ZipEntry getEntry(ZipFile source, String entryName) {
        ZipEntry ze = source.getEntry(FilenameUtils.separatorsToUnix(entryName));
        if (ze == null) {
            ze = source.getEntry(FilenameUtils.separatorsToWindows(entryName));
        }
        
        return ze;
    }
    
    Node loadNode(File f, Node parent) {
        if (f.isDirectory()) {
            return new ZipVirtualNode(this, parent, f.getName(), f);
        } else {
            return new ZipFileNode(this, zf, parent, f.getName());
        }
    }
}
