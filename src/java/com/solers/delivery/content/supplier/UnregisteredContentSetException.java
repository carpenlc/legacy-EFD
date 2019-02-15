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

/**
 * Exception to indicate when a call to a ContentSetManager is made with an
 * unknown identifier. This can occur when the manager is invoked by the
 * Transport (using a String identifier) or the System Manager (using a Long
 * identifier).
 * 
 * @author JGimourginas
 */
public class UnregisteredContentSetException extends RuntimeException {

    private static final long serialVersionUID = 1l;

    // unregistered identifier used to make a call to the manager, as a string
    private String unknownIdentifier;

    public UnregisteredContentSetException(String message, String identifier) {
        super(message);
        this.unknownIdentifier = identifier;
    }

    public String getUnknownIdentifier() {
        return unknownIdentifier;
    }
}
