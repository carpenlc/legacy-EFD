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
/**
 * 
 */
package com.solers.delivery.scripting.event;

import java.util.Iterator;

/**
 * @author mchen
 * 
 *
 */
public interface EventConsumers<E> {

    /**
     * Adds a consumer to the underlying data structure.  Depending on the
     * implementation of the underlying data structure, one may need to call 
     * removeConsumer before adding it so that the latest changes are reflected
     * in subsequent executions.
     * 
     * @param consumer - a event consuming object
     */
    void addConsumer(E consumer);
    
    /**
     * Removes a consumer from the underlying data structure
     * @param consumer - a event consuming object
     */
    void removeConsumer(E consumer);

    /**
     * Returns the number of event consuming objects held by the underlying 
     * data structure
     * @return number of event consuming objects
     */
    int size();
    
    /**
     * Returns an iterator of the event consuming objects from the 
     * underlying data structure
     * 
     * @return an iterator
     */
    Iterator<E> iterator();
}
