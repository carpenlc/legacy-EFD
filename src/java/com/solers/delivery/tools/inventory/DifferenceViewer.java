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
package com.solers.delivery.tools.inventory;

import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.solers.delivery.inventory.DifferenceInventory;
import com.solers.delivery.inventory.TimeProperties;
import com.solers.delivery.inventory.index.diff.DiffParentNode;
import com.solers.delivery.inventory.index.diff.DifferenceFileReader;
import com.solers.delivery.inventory.index.diff.DifferenceNode;
import com.solers.delivery.inventory.node.Node;
import com.solers.delivery.tools.ClTools;
import com.solers.util.unit.FileSizeUnit;

/**
 * Utility class for viewing a single difference file.
 */
public final class DifferenceViewer {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("ddHHmmss'Z' MMM yyyy");
    private static final String OFFSET_STRING = "                      ";

    private final PrintStream out;
    private DifferenceViewer(PrintStream out) {
        this.out = out;
    }
    
    private boolean ordered = true;
    
    private long files = 0;
    private long dirs = 0;
    private long bytes = 0;
    
    public static void main(String... args) {
        if (args.length != 1) {
            printUsage(System.err);
            System.exit(1);
        }
        
        File diffFile = ClTools.extractExistingFile(args);
        if (diffFile == null) {
            System.err.println("Expected difference file as only argument.");
            printUsage(System.err);
            System.exit(1);
        }
        
        if (ClTools.containsEmptyArguments(args)) {
            System.err.println("Mandatory argument is missing.");
            printUsage(System.err);
            System.exit(1);
        }
        
        DifferenceViewer dv = new DifferenceViewer(System.out);
        try {
            DifferenceInventory inv = new DifferenceFileReader(diffFile);
            dv.run(inv);
        } catch (Exception e) {
            System.err.println("Could not read the difference file " + diffFile.getAbsolutePath());
            System.err.println("Cause: " + e.getMessage());
            System.exit(1);
        }
    }
    
    private static void printUsage(PrintStream out) {
        out.println("viewDiff <difference_file>");
    }
    
    public void run(DifferenceInventory inv) {
        out.printf("Difference taken on %s:%n%n", format((Long) inv.getProperty(TimeProperties.TIMESTAMP)));
        for (Node n : inv.root().getChildren()) {
            start(n, 0);
            out.println();
        }
        
        out.printf("%d files, %d directories (%d items) in %d locations, occupying %s%n",
            files, dirs, files + dirs, inv.root().getChildren().size(), FileSizeUnit.format(bytes));
        
        out.println();
        int propCount = inv.properties().size();
        out.printf("Extended properties (%d):%n", propCount);
        for (String property : inv.properties()) {
            out.printf("\t%s = %s%n", property, inv.getProperty(property));
        }
        
        files = 0;
        dirs = 0;
        bytes = 0;
    }
    
    protected void start(Node node, int depth) {
        StringBuilder tabs = new StringBuilder();
        for (int i = 0; i < depth - 1; ++i) {
            tabs.append("|   ");
        }
        if (depth > 0) {
            tabs.append("|---");
        }

        StringBuilder size = new StringBuilder(FileSizeUnit.format(node.getSize()).trim());
        while (size.length() < OFFSET_STRING.length()) {
            size.append(" ");
        }

        boolean isDifference = node instanceof DifferenceNode;
        boolean isParent = node instanceof DiffParentNode;
        boolean isRemoved = false;
        if (isDifference) {
            isRemoved = ((DifferenceNode) node).isRemoved();
        }
        
        out.println(
            (isParent ? "Diffs starting at:" : (isRemoved ? "---  REMOVED  --- " : node.getTimestamp() == 0 ? "                  " : format(node.getTimestamp()))) 
            + " " 
            + (!node.isDirectory() && !isRemoved ? size.toString() : OFFSET_STRING) 
            + tabs.toString() 
            + (isParent ? node.getPath() : node.getName())
            + (node.isDirectory() ? File.separator : ""));

        if (node.isDirectory()) {
            if (isDifference) dirs++;
            List<Node> children = node.getChildren();
            if (ordered) {
                Collections.sort(children);
            }
            
            for (Node f : children) {
                if (!f.isDirectory())
                    start(f, depth + 1);
            }
            for (Node f : children) {
                if (f.isDirectory())
                    start(f, depth + 1);
            }
        } else {
            files++;
            bytes += node.getSize();
        }
    }
    
    private String format(Long value) {
        synchronized(sdf) {
            return sdf.format(new Date(value));
        }
    }
}
