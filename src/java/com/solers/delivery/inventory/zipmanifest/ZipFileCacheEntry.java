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

public class ZipFileCacheEntry {
    private final String path;
    private final long timestamp;
    
    public ZipFileCacheEntry(File zipFile) {
        String path = null;
        try {
            path = zipFile.getCanonicalPath();
        } catch (IOException e) {
            path = zipFile.getAbsolutePath();
        }
        
        this.path = path;
        this.timestamp = zipFile.lastModified();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ZipFileCacheEntry) {
            ZipFileCacheEntry other = (ZipFileCacheEntry) obj;
            return new File(path).equals(new File(other.path))
                && timestamp == other.timestamp;
        }
        
        return false;
    }
    
    @Override
    public int hashCode() {
        return (path + timestamp).hashCode();
    }
    
    @Override
    public String toString() {
        return "cacheKey{" + path + "|" + timestamp + "}";
    }
}
