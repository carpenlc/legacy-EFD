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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap; 
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

/**
 * This class is used to schedule the execution of scripts and to monitor their status.
 * It is thread safe.
 */
public class ScriptExecutionManager {
    private final static Logger log = Logger.getLogger(ScriptExecutionManager.class);
    private final ExecutorService pool;
    private final Map<String, ScriptStatus> scriptStatusMap;
    private final ScriptExecutionTaskFactory factory;

    public ScriptExecutionManager(ScriptExecutionTaskFactory factory, int poolSize) {
        scriptStatusMap = new ConcurrentHashMap<String, ScriptStatus>();
        if (poolSize < 1) {
            throw new IllegalArgumentException("Thread pool size must be a positive number");
        }
        pool = Executors.newFixedThreadPool(poolSize);
        this.factory = factory;
    }

    public synchronized void init() {}

    public synchronized void shutdown() {
        log.info("Script manager shutting down ...");
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(2, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks

                // Wait a while for tasks to respond to being canceled
                if (!pool.awaitTermination(1, TimeUnit.SECONDS))
                    log.error("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    public void execute(ScriptUnit unit) {
        if (log.isDebugEnabled()) {
            log.debug("Script manager preapre to schedule Script id " + unit.getId());
        }

        scriptStatusMap.put(unit.getId(), new ScriptStatus(unit, null, ExecutionStatus.StatusType.SCHEDULED));
        try {
            ScriptExecutionTask task = createTask(unit);
            // schedule the task
            Future<?> job = scheduleTask(task);
            // update status' the schedule future task.
            scriptStatusMap.get(unit.getId()).setScheduledTask(job);
        } catch (ScriptException e) {
            scriptStatusMap.get(unit.getId()).setStatus(new ExecutionStatus(ExecutionStatus.StatusType.COMPLETED_ERROR, e.getMessage(), e));
        }
    }

    private synchronized Future<?> scheduleTask(Runnable task) {
        return pool.submit(task);
    }

    private ScriptExecutionTask createTask(ScriptUnit unit) throws ScriptException {
        ScriptExecutionTask task = factory.newTask(unit);
        task.addObserver(new Observer() {

            // Report back the status
            @Override
            public void update(Observable o, Object arg) {
                if (o instanceof ScriptExecutionTask && arg instanceof ExecutionStatus) {
                    String id = ((ScriptExecutionTask) o).getId();
                    ExecutionStatus execStatus = (ExecutionStatus) arg;
                    ScriptStatus scriptStatus = scriptStatusMap.get(id);
                    if (scriptStatus != null) {
                        scriptStatus.setStatus(execStatus);
                        // do a write to update the map                        
                        scriptStatusMap.put(id, scriptStatus);
                        if (log.isDebugEnabled()) {
                            log.debug("ID: " + id + " - " + execStatus);
                        }
                    } else {
                        throw new IllegalStateException("Could not find the entry for Script id = " + id);
                    }
                }
            }
        });
        return task;
    }

    public int getCompletedCount() {
        // check the snapshot (view) for the number of completed scripts
        int count = 0;
        Collection<ScriptStatus> statusCollection = scriptStatusMap.values();
        for (ScriptStatus status : statusCollection) {
            if (status.isDone()) {
                count++;
            }
        }
        return count;
    }

    public boolean allScriptsCompleted() {
        int completed = getCompletedCount();
        return completed == scriptStatusMap.size();
    }
    
    public static String getStackTraceAsString(Throwable e, int length) {
        String result = null;
        if (e != null) {
            StringWriter stackTrace = new StringWriter();
            // no need to close PrintWriter in this case
            e.printStackTrace(new PrintWriter(stackTrace));
            result = stackTrace.toString();
            if (result.length() > length) {
                result = result.substring(0, length);
            }
        }
        return result;
    }    

    public void printScriptsStatus(PrintWriter writer, boolean detail) {
        Collection<ScriptStatus> statusCollection = scriptStatusMap.values();
        for (ScriptStatus status : statusCollection) {
            writer.println("                       ");
            writer.println(status.toString());
            if(detail && status.getException() != null) {
                writer.println("Exception: " + getStackTraceAsString(status.getException(), 1024));                
            }
        }
    }

}
