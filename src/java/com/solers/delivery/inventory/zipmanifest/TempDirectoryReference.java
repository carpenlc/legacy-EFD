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
import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;

import org.apache.commons.io.FileUtils;

public class TempDirectoryReference<T> extends PhantomReference<T> {
    private final File tempdir;
    
    public TempDirectoryReference(T referent, ReferenceQueue<? super T> q, File tempdir) {
        super(referent, q);
        this.tempdir = tempdir;
        tempdir.deleteOnExit();
    }

    public void cleanup() {
        try {
            FileUtils.deleteDirectory(tempdir);
        } catch (IOException ioe) {
            //no need to worry about it.
        }
    }
}
