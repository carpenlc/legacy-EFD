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
package com.solers.delivery.perftools;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import com.solers.delivery.inventory.Inventory;
import com.solers.delivery.inventory.Writer;
import com.solers.delivery.inventory.fs.FileNode;
import com.solers.delivery.inventory.fs.FileSystemInventory;
import com.solers.delivery.inventory.index.ReaderFactory;
import com.solers.delivery.inventory.index.WriterFactory;
import com.solers.delivery.inventory.node.Node;

public class InventoryPerfTool {
    private final PrintStream out;
    
    public InventoryPerfTool(PrintStream out) {
        this.out = out;
    }
    
    private static void recurse(Node root) {
        if (!root.isDirectory()) return;
        for (Node f : root.getChildren()) {
            recurse(f);
        }
    }
    
    public static void main(String[] args) throws IOException {
        InventoryPerfTool p = new InventoryPerfTool(System.out);
        String targetDir = args[0];
        p.run(targetDir);
    }
    
    public void run(String targetDir) throws IOException {
        File root = new File(targetDir);
        
        long controlStart = System.currentTimeMillis();
        recurse(new FileNode(root, root));
        long controlStop = System.currentTimeMillis();
        
        out.println("Control run: " + (controlStop - controlStart) + " milliseconds");
        
        File index = File.createTempFile("test", ".index");
        Writer i = WriterFactory.newInstance(new FileSystemInventory(root));
        long indexStart = System.currentTimeMillis();
        i.write(index);
        long indexStop = System.currentTimeMillis();
        
        out.println("Index run: " + (indexStop - indexStart) + " milliseconds");
        
        Inventory r = ReaderFactory.newInstance(index);
        Node indexRoot = r.root();
        
        long readStart = System.currentTimeMillis();
        recurse(indexRoot);
        long readStop = System.currentTimeMillis();
        
        out.println("Read run: " + (readStop - readStart) + " milliseconds");
        out.println();
        out.println("Index file size: " + index.length());
        out.println("Index file: " + index.getCanonicalPath());
        out.println("Test complete");
    }
}
