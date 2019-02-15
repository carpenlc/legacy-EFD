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

import java.util.concurrent.Future;

/**
 * Scripting status and handle of execution task
 */
public class ScriptStatus extends ScriptUnit {
    private Future<?> scheduledTask; // can be NULL if the script is never scheduled for execution
    // for some reasons. script file doesn't exist (etc...)
    private ExecutionStatus execStatus;  

    public ScriptStatus(ScriptUnit script, Future<?> task, ExecutionStatus.StatusType status) {
        super(script.getId(), script.getGroupId(), script.getFile(), script.getScriptLanguage(), script.getArguments());
        this.scheduledTask = task;
        this.execStatus = new ExecutionStatus(status, null, null);
    }

    public synchronized void setStatus(ExecutionStatus status) {
        this.execStatus = status;       
    }

    public synchronized ExecutionStatus.StatusType getStatus() {
        return execStatus.getStatus();
    }    
    
    public synchronized String getErrorMessage() {
        return execStatus.getError();
    }    
    public synchronized Throwable getException() {
        return execStatus.getException();
    }        

    public synchronized Future<?> getScheduledTask() {
        return scheduledTask;
    }

    public synchronized Future<?> setScheduledTask(Future<?> task) {
        return this.scheduledTask = task;
    }

    public boolean isDone() {

        boolean done = false;
        Future<?> task = getScheduledTask();
        if (task != null && task.isDone()) {
            done = true;
        } else if (isCompleted()) {
            done = true;
        }
        return done;
    }

    private synchronized boolean isCompleted() {
        return execStatus.isCompleted();
    }

    public String toString() {
        String result = super.toString();
        String error = getErrorMessage();
        String errorText = error != null ? ("Error: " + error) : "";
        return String.format("%s \nStatus: %s \n%s ", result, getStatus().getDisplayLabel(), errorText);
    }
}
