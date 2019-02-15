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
import java.util.HashMap;

import com.solers.delivery.inventory.Inventory;
import com.solers.delivery.inventory.comparer.Comparer;
import com.solers.delivery.inventory.comparer.NodeListener;
import com.solers.delivery.inventory.comparer.UnorderedInventoryComparer;
import com.solers.delivery.inventory.node.Node;
import com.solers.delivery.inventory.plugin.InventoryPlugin;
import com.solers.delivery.inventory.plugin.PluginException;
import com.solers.delivery.inventory.plugin.PluginLoader;
import com.solers.delivery.tools.ClTools;
import com.solers.util.unit.FileSizeUnit;

/**
 * Tool to quickly compare inventory files/file system in some sort of mix and
 * spit the results back out.
 * @author gvanore
 * TODO: Make this class capable to load inventory plugins
 */
public final class Compare {
    private NodeListener handler;
    private final Comparer comparer;
    
    public Compare(Comparer comparer) {
        this.comparer = comparer;
    }
    
    public void compare(Inventory a, Inventory b) {
        if (handler == null) throw new IllegalStateException("Must set a handler.");
        comparer.compare(a, b, handler);
    }

    public NodeListener getHandler() {
        return handler;
    }

    public void setHandler(NodeListener handler) {
        this.handler = handler;
    }
    
    public static void main(String... args) throws Exception {
        if (args.length < 2) {
            System.out.println("Invalid argument count.");
            printUsage();
            System.exit(1);
        }
        
        File invA = ClTools.extractExistingFile(0, args);
        File invB = ClTools.extractExistingFile(1, args);
        if (invA == null || invB == null) {
            System.err.println("Required: two inventory locations, either index data or filesystem directories.");
            printUsage();
            System.exit(1);
        }
        
        if (ClTools.containsEmptyArguments(args)) {
            System.err.println("Mandatory argument missing.");
            printUsage();
            System.exit(1);
        }

        boolean recurseAdd = false;
        boolean recurseDel = false;
        boolean flip = false;
        final String pluginArg = "--plugin=";
        for (int i = 2; i < args.length; ++i) {
            if (args[i].equals("--recurse-adds")) recurseAdd = true;
            else if (args[i].equals("--recurse-dels")) recurseDel = true;
            else if (args[i].equals("--switch")) flip = true;
            else if (args[i].startsWith(pluginArg)) {
                File plugin = new File(args[i].substring(args[i].indexOf(pluginArg) + pluginArg.length()));
                if (plugin.isFile()) {
                    InventoryPlugin.register(plugin);
                } else if (plugin.isDirectory()) {
                    PluginLoader.load(plugin.getAbsolutePath());
                } else {
                    System.out.println("Could not load plugin: " + args[i]);
                    printUsage();
                    System.exit(1);
                }
            } else {
                System.out.println("Invalid argument: " + args[i]);
                printUsage();
                System.exit(1);
            }
        }
        
        //always load our proprietary format reader
        InventoryPlugin.register(IndexInventoryProvider.class);
        
        Inventory a = null;
        Inventory b = null;
        try {
            a = load(invA);
            b = load(invB);
            
            Compare c = new Compare(new UnorderedInventoryComparer(recurseAdd, recurseDel));
            c.setHandler(new CompareListener());
            if (flip) {
                c.compare(b, a);
            } else {
                c.compare(a, b);
            }
        } catch (PluginException pe) {
            System.out.println("Failed to load inventory file:");
            pe.printStackTrace(System.out);
            System.exit(2);
        } finally {
            if (a != null) a.close();
            if (b != null) b.close();
        }
    }
    
    public static void printUsage() {
        System.out.println("Compare <inv_desired> <inv_existing> [--recurse-adds] [--recurse-dels] [--switch] [<pluginopts>]");
        System.out.println("Inventory files may be directories on the file system or .index files.");
        System.out.println("--switch flips the desired/existing inventory parameter order.");
        System.out.println("You may also add any number of --plugin=<file> commands, where <file> is either:");
        System.out.println("\t- a plugin archive");
        System.out.println("\t- a directory, where all files will be loaded");
        System.out.println("Sample:");
        System.out.println("compare c:/supply_loc c:/supply_loc.zip --plugin=conf/plugins");
    }
    
    private static Inventory load(File f) throws PluginException {
        return InventoryPlugin.newInstance(f.toURI(), new HashMap<String, Object>(0));
    }
    
    static class CompareListener implements NodeListener {
        private long start;
        @Override
        public void onAdd(Node node) {
            System.out.println("ADD: " + describe(node));
        }

        @Override
        public void onRemove(Node node) {
            System.out.println("DEL: " + describe(node));
        }

        @Override
        public void onStart() {
            System.out.println("Beginning comparison...");
            start = System.currentTimeMillis();
        }

        @Override
        public void onStop() {
            long finish = System.currentTimeMillis() - start;
            System.out.println("Comparison finished in " + finish + " milliseconds.");
        }

        @Override
        public void onUpdate(Node node) {
            System.out.println("UPD: " + describe(node));
        }
        
        private String describe(Node n) {
            String file = "file";
            String directory = "directory";
            StringBuilder sb = new StringBuilder(n.getPath());
            sb.append(" (").append(n.isDirectory() ? directory : file);
            if (!n.isDirectory()) sb.append(", ").append(FileSizeUnit.format(n.getSize()))
                .append(", ").append(n.getTimestamp());
            return sb.append(")").toString();
        }
    }
}
