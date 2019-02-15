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
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;

import com.solers.delivery.inventory.Inventory;
import com.solers.delivery.inventory.comparer.Comparer;
import com.solers.delivery.inventory.comparer.NodeListener;
import com.solers.delivery.inventory.comparer.UnorderedInventoryComparer;
import com.solers.delivery.inventory.fs.FileSystemInventory;
import com.solers.delivery.inventory.index.ReaderFactory;
import com.solers.delivery.inventory.node.Node;
import com.solers.util.unit.FileSizeUnit;
import com.solers.util.unit.TimeIntervalUnit;

public final class ComparerPerfTool {
    private final PrintStream out;
    private long differences = 0;
    private final boolean verbose;
    
    private ComparerPerfTool(PrintStream out, boolean verbose) {
        this.out = out;
        this.verbose = verbose;
    }
    
    private void printMemoryStats(String type, final MemoryUsage before, final MemoryUsage after) {
        MemoryUsage comparison = new MemoryUsage(0, 0, 0, 0) {
            @Override public long getInit() {
                return after.getInit() - before.getInit();
            }
            @Override public long getUsed() {
                return after.getUsed() - before.getUsed();
            }
            @Override public long getCommitted() {
                return after.getCommitted() - before.getCommitted();
            }
            @Override public long getMax() {
                return after.getMax() - before.getMax();
            }
        };
        print(type, comparison);
    }
    
    private void print(String type, MemoryUsage mu) {
        out.printf("%s Change:%nUsed     : %s%nCommitted: %s%n",
            type,
            FileSizeUnit.format(Math.abs(mu.getUsed())),
            FileSizeUnit.format(Math.abs(mu.getCommitted()))
        );
    }
    
    public void compare(Comparer c, Inventory desired, Inventory existing, NodeListener handler) {
        differences = 0;
        System.gc();
        
        MemoryUsage beforeHeap = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        MemoryUsage beforeNonheap = ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();
        
        long start = System.currentTimeMillis();
        
        c.compare(desired, existing, handler);
        
        long stop = System.currentTimeMillis();
        MemoryUsage afterHeap = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        MemoryUsage afterNonheap = ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();

        out.println(TimeIntervalUnit.format(stop - start) + " elapsed");
        out.printf("Tracking %d differences in total.%n", differences);
        printMemoryStats("Heap Usage", beforeHeap, afterHeap);
        printMemoryStats("Non-Heap Usage", beforeNonheap, afterNonheap);
    }
    
    public void compare(Comparer c, Inventory desired, Inventory existing) {
        compare(c, desired, existing, new NodeListener() {
            @Override
            public void onAdd(Node node) {
                ++differences;
                if (verbose) out.println("ADD " + node);
            }

            @Override
            public void onRemove(Node node) {
                ++differences;
                if (verbose) out.println("RMV " + node);
            }

            @Override
            public void onStart() {
            }

            @Override
            public void onStop() {
            }

            @Override
            public void onUpdate(Node node) {
                ++differences;
                if (verbose) out.println("UPD " + node);
            }
        });
    }
    
    public static void main(String... args) throws Exception {
        String inventory = args[0];
        String existingDir = args[1];
        Inventory desired = ReaderFactory.newInstance(new File(inventory));
        Inventory existing = new FileSystemInventory(existingDir);
        
        ComparerPerfTool dst = new ComparerPerfTool(System.out, true);
        dst.compare(new UnorderedInventoryComparer(), desired, existing);
    }
}
