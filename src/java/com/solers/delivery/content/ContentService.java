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
package com.solers.delivery.content;

import java.util.List;

import com.solers.delivery.domain.ConsumerContentSet;
import com.solers.delivery.domain.ContentSet;
import com.solers.util.dao.ValidationException;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public interface ContentService {
    
    /**
     * @return A list of consumer and supplier content sets
     */
    List<ContentSet> getContentSets();
    
    /**
     * @return A list of supplier content sets
     */
    List<ContentSet> getSuppliers();
    
    /**
     * @return A list of consumer content sets
     */
    List<ConsumerContentSet> getConsumers();
    
    /**
     * @param id
     * @return The content set with the given id or null if it doesn't exist
     */
    <T extends ContentSet> T get(Long id);
    
    /**
     * Save the given content set data.  If the data does not exist, it will be
     * created
     * 
     * @param contentSet
     * @return The id of the content set
     * @throws ValidationException
     */
    Long save(ContentSet contentSet) throws ValidationException;
    
    /**
     * Delete the given content set
     * 
     * @param contentSet
     */
    void remove(Long id);
    
    /**
     * Enable the given content set
     * @param id
     */
    void enable(Long id);
    
    /**
     * Disable the given content set
     * @param id
     */
    void disable(Long id);
    
}
