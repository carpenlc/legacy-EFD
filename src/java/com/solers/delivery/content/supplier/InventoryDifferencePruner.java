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
package com.solers.delivery.content.supplier;

import java.io.File;

import com.solers.delivery.domain.ContentSet;
import com.solers.delivery.inventory.InventoryFactory;
import com.solers.delivery.inventory.InventoryFactory.Stats;

public class InventoryDifferencePruner {
    
    private static final Policy DEFAULT_PRUNE_POLICY = Policy.FILECOUNT;
    private static final long DEFAULT_PRUNE_VALUE = 5;
    
    private final ContentSet config;
    
    public InventoryDifferencePruner(ContentSet config) {
        this.config = config;
    }
    
    public boolean prune() {
        return prune(DEFAULT_PRUNE_VALUE, DEFAULT_PRUNE_POLICY);
    }
    
    public boolean prune(long value) {
        return prune(value, DEFAULT_PRUNE_POLICY);
    }
    
    /**
     * Prune excess differences from the open inventory directory where there is a violation
     * of difference accumulation policy.  Since the inventory differences must remain
     * contiguous, the pruning will stop and return <quote>false</quote> if any tail-end file
     * cannot be removed.  When no pruning is necessary, or all pruning succeeds, pruning
     * will return <quote>true</quote>.
     * @param value The value for the policy to enforce.
     * @param policy The pruning policy: duration or file count.
     * @return false only if the pruning operation could not succeed.
     */
    public boolean prune(long value, Policy policy) {
        switch (policy) {
            case FILECOUNT:
                return pruneFileCount(value);
            case DURATION:
                return pruneDuration(value);
            default:
                return false;
        }
    }
    
    protected boolean pruneFileCount(long files) {
        Stats s = InventoryFactory.availableDifferences(config);
        if (s.count() > files) {
            File next = next(s.listFiles());
            if (next == null) return false;
            if (next.delete()) return pruneFileCount(files);
            return false;
        }
        return true;
    }
    
    protected boolean pruneDuration(long duration) {
        Stats s = InventoryFactory.availableDifferences(config);
        if (s.duration() > duration) {
            File next = next(s.listFiles());
            if (next == null) return false;
            if (next.delete()) return pruneDuration(duration);
            return false;
        }
        return true;
    }

    public static enum Policy {
        FILECOUNT,
        DURATION
    }
    
    private File next(File[] files) {
        if (files == null) return null;
        if (files.length == 0) return null;
        return files[0];
    }
}
