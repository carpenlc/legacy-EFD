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

import java.io.File;

import com.solers.delivery.scripting.event.Consumer;
import com.solers.delivery.scripting.event.DeliveryEventConsumer;
import com.solers.delivery.scripting.event.EventConsumers;
import com.solers.delivery.scripting.event.EventQueue;

/**
 * Factory to create scripting (Runnable) tasks
 */
public class ScriptExecutionTaskFactory {
    private EventQueue eventQueue;
    private EventConsumers<Consumer<DeliveryEventConsumer>> eventConsumers;
    private String scriptingEventManagerAlias;

    public ScriptExecutionTaskFactory() {
        this(null, null);
    }

    public ScriptExecutionTaskFactory(EventQueue eventQueue, EventConsumers<Consumer<DeliveryEventConsumer>> eventConsumers) {
        this(eventQueue, eventConsumers, "efdEventManager");
    }

    public ScriptExecutionTaskFactory(EventQueue eventQueue, EventConsumers<Consumer<DeliveryEventConsumer>> eventConsumers, String hookAlias) {
        this.eventQueue = eventQueue;
        this.eventConsumers = eventConsumers;
        this.scriptingEventManagerAlias = hookAlias;
    }

    public void setScriptingEventManagerAlias(String eventManagerHookAlias) {
        this.scriptingEventManagerAlias = eventManagerHookAlias;
    }

    public ScriptExecutionTask newTask(ScriptUnit unit) throws ScriptException {        
        File file = unit.getFile();
        if (file == null) {
            throw new ScriptException("Attempt to create task for null script file");
        } else if (file.getAbsolutePath() == null || file.getAbsolutePath().equals("") || !file.isFile() || !file.exists()) {
            throw new ScriptException(String.format("Can not create task for script file %s: file does not exist", file.getAbsolutePath()));
        }
        if (unit.getScriptLanguage() == null || unit.getScriptLanguage().trim().length() == 0) {
            throw new ScriptException("Can not create job for script file " + file.getName() + " because the language is not specified");
        }
        ScriptExecutionTask runner = new ScriptExecutionTask(unit, this.eventQueue, this.eventConsumers, this.scriptingEventManagerAlias);
        return runner;
    }

}
