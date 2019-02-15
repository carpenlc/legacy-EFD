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
 * This class is used for encapsulating a DeliveryEventConsumer object and 
 * the file path to the script that created the object.
 * 
 * equals and hashCode methods are overridden in the class so that script
 * writers do not have to override the equals/hashcode methods in the scripts.
 */
package com.solers.delivery.scripting.event;


/**
 * @author mchen
 *
 */
public final class Consumer<V> {
    private V consumer;
    private String fileName;
    
    /**
     * Constructor
     * 
     * @param consumer DeliveryEventConsumer object created in a script
     * @param fileName file path to the script that is used as the unique id
     */
    public Consumer(V consumer, String fileName) {
        this.consumer = consumer;
        this.fileName = fileName;
    }
    
    /**
     * Returns the DeliveryEventConsumer object created from a script
     * @return a DeliveryEventConsumer object
     */
    public V getConsumer() {
        return this.consumer;
    }
    
    /**
     * Returns the file name of that created the DeliveryEventConsumer object
     * 
     * @return path to script
     */
    public String getFileName() {
        return this.fileName;
    }
    
    /**
     * Objects are equal if they are of the same type and 
     * the file names are the same
     * @return true if objects are equal, false otherwise
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Consumer)) {
            return false;
        }
        
        Consumer<V> c = (Consumer<V>)obj;
        return this.fileName.equals(c.getFileName());
    }
    
    /**
     * Returns the hashcode of the underlying file name string
     * 
     * @return hashcode of the underlying file name string
     */
    @Override
    public int hashCode() {
        return this.fileName.hashCode();
    }
}
