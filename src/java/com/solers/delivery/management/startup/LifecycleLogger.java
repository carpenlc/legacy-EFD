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
package com.solers.delivery.management.startup;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;

import com.solers.security.audit.Auditable;
import com.solers.security.audit.Auditable.Action;
import com.solers.security.audit.Auditable.Severity;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class LifecycleLogger {
    
    private static final Logger log = Logger.getLogger(LifecycleLogger.class);
    
    @PostConstruct
    @Auditable(action=Action.START,severity=Severity.MEDIUM)
    public void start() {
        log.info("Started");
    }
    
    @PreDestroy
    @Auditable(action=Action.STOP,severity=Severity.MEDIUM)
    public void stop() {
        log.info("Stopped");
    }

    @Override
    public String toString() {
        return "";
    }
    
}
