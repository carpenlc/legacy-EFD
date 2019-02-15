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

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class AuditAspect {
    
    private final Set<AuditEventListener> listeners;
    
    public AuditAspect() {
        this.listeners = new CopyOnWriteArraySet<AuditEventListener>();
    }
    
    @AfterReturning("execution(*.new(..)) && @within(a) && target(o)")
    public void advice(AutoAuditEventListener a, Object o) {
        if (o instanceof AuditEventListener) {
            addListener((AuditEventListener) o);
        } else {
            throw new IllegalArgumentException(o+" does not implement "+AuditEventListener.class.getName());
        }
    }
    
    public void addListener(AuditEventListener listener) {
        listeners.add(listener);
    }
 
    @AfterReturning("execution(public void *()) && @annotation(a) && target(o)")
    public void audit(Auditable a, Object o) {
        for (AuditEventListener l  : listeners) {
            l.onSuccess(new AuditSuccessEvent(o, a));
        }
    }
    
    @AfterReturning("execution(public * *(..)) && @annotation(a) && args(obj,..) && within(! com.solers.delivery.web.listeners.*)")
    public void auditSuccess(Object obj, Auditable a) {
        for (AuditEventListener l  : listeners) {
            l.onSuccess(new AuditSuccessEvent(obj, a));
        }
    }

    @AfterThrowing(pointcut = "execution(public * *(..)) && @annotation(a) && args(obj,..)", throwing = "e")
    public void auditFailure(Object obj, Auditable a, Exception e) {
        for (AuditEventListener l  : listeners) {
            l.onFailure(new AuditFailureEvent(obj, a));
        }
    }
}
