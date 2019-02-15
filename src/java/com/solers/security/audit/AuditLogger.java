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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.ui.WebAuthenticationDetails;
import org.apache.log4j.Logger;

import com.solers.security.audit.Auditable.Severity;

@AutoAuditEventListener
public class AuditLogger implements AuditEventListener {
    
    private static final DateFormat DTG_FORMAT = new SimpleDateFormat("ddHHmmss'Z' MMM yyyy");
    private static final Logger log = Logger.getLogger(AuditLogger.class.getName());
    
    @Override
    public void onFailure(AuditFailureEvent event) {
        formatMessage(event.getInfo(), formatSource(event.getSource()), false);
    }

    @Override
    public void onSuccess(AuditSuccessEvent event) {
        formatMessage(event.getInfo(), formatSource(event.getSource()), true);
    }
    
    private void formatMessage(Auditable a, String id, boolean wasSuccessful) {
        String outcome = (wasSuccessful) ? "SUCCEEDED" : "FAILED";
        log(String.format("%s %s for (%s)", a.action().name(), outcome, id), a.severity());
    }
    
    private String formatSource(Object o) {
        if (o instanceof Number) {
            return String.format("Id: %s", o.toString());
        }
        return String.format("Type: %s [%s]", o.getClass().getSimpleName(), o.toString());
    }

    protected void log(String message, Severity severity) {
        SecurityContext ctx = SecurityContextHolder.getContext();
        Authentication auth = null;
        
        if (ctx != null) {
            auth = ctx.getAuthentication();
        }
        log(message, severity, auth);
    }
    
    protected void log(String message, Severity severity, Authentication auth) {
        String dtg = format(new Date());
        String actingUser = "UNKNOWN";
        String details = "UNKNOWN";
        
        if (auth != null) {
            actingUser = auth.getName();
            if (auth.getDetails() != null) {
                if (auth.getDetails() instanceof WebAuthenticationDetails) {
                    details = ((WebAuthenticationDetails) auth.getDetails()).getRemoteAddress();
                } else {
                    details = auth.getDetails().toString();
                }
            }
        }
        
        doLog("%s %s  Severity: %s  Initiating user: %s  IP Address: %s", dtg, message, severity, actingUser, details);
    }
    
    private void doLog(String message, Object... args) {
        log.info(String.format(message, args));
    }
    
    private static String format(Date d) {
        synchronized (DTG_FORMAT) {
            return DTG_FORMAT.format(d);
        }
    }
}
