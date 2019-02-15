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
import com.solers.util.Callback;

/**
 * Interface describing the mappings necessary for the Supplier to interact with the 
 * Transport. The Transport will field requests to specific SupplierContentSets. The 
 * Transport will be sent the Name of SupplierContentSet from which Content or an 
 * Inventory is needed. The Transport must then lookup the SupplierContentSet root or 
 * packaged inventory location and return that file, based on the ContentSet name.
 */
public interface ContentSetMapper {

    /**
     * Get absolute path on file system of the content requested. 
     * The two parameters indicate a.) the content set root directory that should be 
     * used (i.e. the beginning of the path) b.) the relative path from the content set
     * root to the desired piece of content.
     * 
     * @param contentSetName
     *            identifies the ContentSet whose root will be used in constructing the file path
     * @param pathFromRoot
     *            the relative path from the root (and including the root) of the piece of 
     *            content whose path will be returned
     * @return URI representing piece of content on the file system
     */
    File getFile(String contentSetName, String pathFromRoot);
    File getFile(String contentSetName, String pathFromRoot, long timestamp);

    /**
     * Gets primary key DB identifier of the Supplier Content Set based on its name.
     * 
     * @param contentSetName
     *            name of the Supplier Content Set to be looked up
     * @return Long id representing Supplier Content Set configuration row in database
     * @throws UnregisteredContentSetException
     */
    Long getId(String contentSetName);
    
    /**
     * 
     * @param id
     * @return The content set name
     */
    String getName(Long id);

    /**
     * Test if the given path refers to an inventory file, not a regular file
     * 
     * @param path
     * @return True if <code>path</code> refers to an inventory file
     */
    boolean isInventory(String path);
    
    /**
     * 
     * @param contentSetName
     * @param commonName
     * @return True if {@code commonName} is allowed to access @{code contentSetName}
     */
    boolean isAllowed(String contentSetName, String commonName);
    
    void addDisabledListener(Callback<ContentSet> e);
    
    void removeDisabledListener(Callback<ContentSet> e);
}