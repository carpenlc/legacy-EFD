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
package com.solers.security.audit;

import java.util.EventObject;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class AuditSuccessEvent extends EventObject {
    
    private static final long serialVersionUID = 1L;

    private final transient Auditable info;
    
    public AuditSuccessEvent(Object source, Auditable info) {
        super(source);
        this.info = info;
    }

    public Auditable getInfo() {
        return info;
    }
}
