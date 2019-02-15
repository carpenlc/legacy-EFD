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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.solers.delivery.inventory.Inventory;
import com.solers.delivery.inventory.index.HashUtil;
import com.solers.delivery.inventory.index.ReaderFactory;
import com.solers.delivery.inventory.node.Node;
import com.solers.delivery.tools.ClTools;
import com.solers.util.HashFunction;

public final class Optimizer {
    public Map<String, Long> analyze(Inventory inv) {
        Map<String, Long> collisions = new TreeMap<String, Long>();
        for (HashFunction algorithm : HashFunction.values()) {
             collisions.put(algorithm.name(), analyze(inv.root(), algorithm));
        }
        return collisions;
    }
    
    public String optimal(Map<String, Long> resultData) {
        long smallest = Long.MAX_VALUE;
        String result = HashFunction.FNV.name();
        for (Entry<String, Long> e : resultData.entrySet()) {
            if (e.getValue() < smallest) {
                smallest = e.getValue();
                result = e.getKey();
            }
        }
        return result;
    }
    
    public String optimal(Inventory inv) {
        return optimal(analyze(inv));
    }
    
    private long analyze(Node n, HashFunction hash) {
        long collisions = 0;
        if (n.isDirectory()) {
            Set<Integer> s = new HashSet<Integer>();
            List<Node> children = n.getChildren();
            for (Node child : children) {
                int index = HashUtil.index(child.getName(), hash, children.size());
                if (s.contains(index)) {
                    collisions++;
                } else {
                    s.add(index);
                }
                if (child.isDirectory()) collisions += analyze(child, hash);
            }
        }
        return collisions;
    }
    
    private void run(PrintStream out, Inventory inv) {
        out.println("Calculating algorithm for inventory, please wait...\n");
        Map<String, Long> collisionData = analyze(inv);
        out.println(optimal(collisionData) + " is the optimal hashing algorithm.");
        out.println("\n  Hash\tCollision count");
        for (Entry<String, Long> e : collisionData.entrySet()) {
            out.printf("%6s\t%d%n", e.getKey(), e.getValue());
        }
    }
    
    public static void main(String... args) {
        if (args.length != 1) {
            printUsage(System.err);
            System.exit(1);
        }
        
        File inventoryFile = ClTools.extractExistingFile(args);
        if (inventoryFile == null) {
            System.err.println("Expecting inventory file.");
            printUsage(System.err);
            System.exit(1);
        }
        
        if (ClTools.containsEmptyArguments(args)) {
            System.err.println("Mandatory argument was missing.");
            printUsage(System.err);
            System.exit(1);
        }
        
        try {
            Inventory inv = ReaderFactory.newInstance(inventoryFile);
            Optimizer o = new Optimizer();
            o.run(System.out, inv);
        } catch (Exception e) {
            System.err.println("Failed to run Optimizer tool.");
            System.err.println("Cause: " + e.getMessage());
        }
    }
    
    private static void printUsage(PrintStream out) {
        out.println("optimizer <inventory_file>");
    }
}
