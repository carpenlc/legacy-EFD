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
package com.solers.jmx.registry;

/**
 * Object registry
 * 
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public interface Registrar {
    
    /**
     * Retrieve a managed bean
     * @param mxbean The interface that the registered object implements.  Must obey MXBean naming policy.
     * @param namingObject The object used to generate the managed name for this object.  toString() is invoked for this purpose.
     * @return An object implementing the interface specified by mxbean, managed through JMX.
     */
    <M> M getManagedBean(Class<? extends M> type, Object key);
    
    /**
     * Register an object
     * @param object
     * @param typeCategory
     * @param key
     */
    void registerManagedBean(Object object, String typeCategory, Object key);
    
}
