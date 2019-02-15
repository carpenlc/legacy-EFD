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
package com.solers.delivery.scripting;
/**
 * Script execution state
 */
public class ExecutionStatus {

    private final StatusType status;
    private final String error;
    private final Throwable exception;

    public ExecutionStatus(StatusType status) {
        this(status, null, null);
    }

    public ExecutionStatus(StatusType status, String error, Throwable exception) {
        this.status = status;
        this.error = error;
        this.exception = exception;
    }

    public StatusType getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public Throwable getException() {
        return exception;
    }

    public boolean isCompleted() {
        StatusType currentStatus = getStatus();
        return (currentStatus.equals(StatusType.COMPLETED_SUCCESS) || currentStatus.equals(StatusType.COMPLETED_ERROR));
    }

    public String toString() {
        String ts = getStatus().toString();
        String theError = getError();
        if (theError != null) {
            ts += ", Error: " + theError;
        }
        return ts;
    }

    public enum StatusType {
        SCHEDULED("Scheduled"), IN_EXECUTION("In execution"), COMPLETED_SUCCESS("Completed successfully"), COMPLETED_ERROR("Completed with error(s)");

        private String displayLabel;

        private StatusType(String display) {
            this.displayLabel = display;
        }

        public String getDisplayLabel() {
            return displayLabel;
        }
    }
}
