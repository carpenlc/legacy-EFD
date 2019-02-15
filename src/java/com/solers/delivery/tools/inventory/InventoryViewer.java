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
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.solers.delivery.inventory.Inventory;
import com.solers.delivery.inventory.index.ReaderFactory;
import com.solers.delivery.inventory.node.Node;
import com.solers.delivery.tools.ClTools;
import com.solers.util.unit.FileSizeUnit;

public class InventoryViewer {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("ddHHmmss'Z' MMM yyyy");
    private static final String OFFSET_STRING = "                      ";

    private final PrintStream out;
    private boolean showPath = false;
    private boolean ordered = true;
    private boolean showProps = false;
    
    //statistics
    private long bytes = 0;
    private long files = 0;
    private long dirs = 0;

    public InventoryViewer(PrintStream out) {
        this.out = out;
    }
    
    protected static boolean toggleFlags(InventoryViewer v, String... args) {
        if (args.length > 1) {
            for (int i = 1; i < args.length; ++i) {
                String arg = args[i];
                if (arg.equals("--verbose") || arg.equals("-v")) {
                    v.setShowPath(true);
                } else if (arg.equals("--unordered") || arg.equals("-u")) {
                    v.setOrdered(false);
                } else if (arg.equals("--properties") || arg.equals("-p")) {
                    v.setShowProperties(true);
                } else {
                    return false;
                }
            }
        }
        return true;
    }
    
    public static void main(String... args) throws IOException {
        InventoryViewer v = new InventoryViewer(System.out);
        
        File invFile = ClTools.extractExistingFile(args);
        if (invFile == null) {
            System.err.println("Expecting inventory file as argument.");
            printUsage(System.err);
            System.exit(1);
        }
        
        if (ClTools.containsEmptyArguments(args)) {
            System.err.println("Missing a required argument.");
            printUsage(System.err);
            System.exit(1);
        }
        
        boolean flagsAccepted = toggleFlags(v, args);
        if (!flagsAccepted) {
            System.err.println("Unknown argument found on command line.");
            printUsage(System.err);
            System.exit(1);
        }
        
        try {
            v.run(invFile);
        } catch (IllegalArgumentException iae) {
            error(invFile, iae.getMessage());
        } catch (IOException ioe) {
            error(invFile, ioe.getMessage());
        }
    }
    
    protected static void printUsage(PrintStream out) {
        out.println("viewInventory <inventory_file> [-u|--unordered] [-v|--verbose] [-p|--properties]");
        out.println("\tinventory_file: a file containing the inventory you wish to print.");
        out.println("\t-h, --help: print this usage message.");
        out.println("\t-u, --unordered: print the inventory in the order it exists in the file");
        out.println("\t-p, --properties: print extended properties, if any, set for this inventory");
        out.println("\t-v, --verbose: print the full path as well as file name");
        out.println();
        out.println("Examples:");
        out.println("viewInventory ../site/inventories/suppliers/mysupplier/open/inventory.index");
        out.println("viewInventory inventory.index -u -v");
    }

    public static void error(File file, String msg) {
        String path;
        try {
            path = file.getCanonicalPath();
        } catch (IOException ioe) {
            path = file.getAbsolutePath();
        }

        System.err.println("There was an error reading the inventory file:\n" + path);
        System.err.println((msg != null) ? msg + "\n" : "");
        System.err.println("Please ensure that the first parameter to this program is a valid");
        System.err.println("inventory file, and that the current user has permission to read it.\n");
        printUsage(System.err);
        System.exit(2);
    }

    public void setShowPath(boolean showPath) {
        this.showPath = showPath;
    }
    
    public void setOrdered(boolean ordered) {
        this.ordered = ordered;
    }
    
    public void setShowProperties(boolean showProps) {
        this.showProps = showProps;
    }

    public void run(File invFile) throws IOException {
        Inventory r = ReaderFactory.newInstance(invFile);
        Node root = r.root();
        run(root);
        
        if (showProps) {
            out.println();
            int propCount = r.properties().size();
            out.printf("Extended properties (%d):%n", propCount);
            for (String property : r.properties()) {
                out.printf("\t%s = %s%n", property, r.getProperty(property));
            }
        }
    }
    
    public void run(Node node) {
        bytes = 0;
        files = 0;
        dirs = 0;
        
        int depth = 0;
        start(node, depth);
        
        out.printf("%n%d files in %d directories (%d items), occupying %s%n",
            files, dirs, files + dirs, FileSizeUnit.format(bytes));
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

        out.println(
            (format(node.getTimestamp())) 
            + " " 
            + (!node.isDirectory() ? size.toString() : OFFSET_STRING) 
            + tabs.toString() 
            + node.getName() 
            + (node.isDirectory() ? File.separator : "") 
            + ((showPath) ? " " + node.getPath() : ""));

        if (node.isDirectory()) {
            dirs++;
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
