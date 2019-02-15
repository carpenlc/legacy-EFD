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
import java.util.List;

import org.apache.log4j.Logger;

import com.solers.delivery.scripting.input.ScriptInput;
import com.solers.delivery.scripting.input.UnitsScanner;

public class ScriptingApplication {
    private final static Logger log = Logger.getLogger(ScriptingApplication.class);

    private final ScriptExecutionManager taskManager;
    private final UnitsScanner scanner;    
    private final long scanningIntervalMillis;
    private final long statusIntervalMillis;

    private long lastTimeMillis;
    private boolean verbose;

    public ScriptingApplication(ScriptExecutionManager manager, ScriptConfiguration config, long scanDurationMillis, long statusDurationMillis) {
        this.taskManager = manager;
        this.scanner = new UnitsScanner(config);
        
        if(scanDurationMillis <=  0) {
            throw new IllegalArgumentException("Scanning interval must be a positive value");
        }
        if(statusDurationMillis <=  0) {
            throw new IllegalArgumentException("Status report interval must be a positive value");
        }        
        this.scanningIntervalMillis = scanDurationMillis;
        this.statusIntervalMillis = statusDurationMillis;        
    }

    public void start(ScriptInput input, boolean verbose) {        
        this.verbose = verbose;
        executeScripts(input);
    }

    /**
     * Detect and execute script specified in the configuration file
     */
    private void executeScripts(ScriptInput input) {
        List<ScriptUnit> units = null;

        try {
            while (true) {
                // TODO: Can have the scanner run on a separate thread for better performance
                units = scanner.scanNext(input);

                if (units.size() > 0) {
                    log.info(String.format("Detect %d  new scripts added", units.size()));
                    scheduleScripts(taskManager, units);
                }

                runPeriodicChores();
                Thread.sleep(scanningIntervalMillis);

            }
        } catch (InterruptedException e) {
            log.error("Application received interrup exception: " + e.getMessage());
        } finally {
            outputScriptStatus(isVerbose());
        }
    }
    
    private void scheduleScripts(ScriptExecutionManager taskManager, List<ScriptUnit> units) {
        for (ScriptUnit unit : units) {            
            log.info("Execute file name " + unit.getFile().getName() + " in language " + unit.getScriptLanguage());
            taskManager.execute(unit);
        }
    }    

    /**
     * Activity and cleanup 
     */
    private void runPeriodicChores() {
        if ((lastTimeMillis + statusIntervalMillis) < System.currentTimeMillis()) {
            outputScriptStatus(isVerbose());
            lastTimeMillis = System.currentTimeMillis();
        }
    }

    private void outputScriptStatus(boolean detail) {
        StringWriter holder = new StringWriter();
        taskManager.printScriptsStatus(new PrintWriter(holder), detail);        
        log.info("\n---------- SCRIPT(S) STATUS ----------\n" + holder.toString() + "---------- END STATUS ----------");
    }

    public boolean isVerbose() {
        return verbose;
    }
}
