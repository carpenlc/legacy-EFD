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

import java.io.FileReader;
import java.util.Observable;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.solers.delivery.scripting.event.Consumer;
import com.solers.delivery.scripting.event.DeliveryEventConsumer;
import com.solers.delivery.scripting.event.EventConsumers;
import com.solers.delivery.scripting.event.EventQueue;

/**
 * Scripting task to be scheduled by scripting manager. It is built as a Runnable and to be executed
 * By some sort of executor service. It contains the thread-safe JavaToScriptMediator  to be exposed to
 * scripting environment
 */
public class ScriptExecutionTask extends Observable implements Runnable {
    private final static Logger log = Logger.getLogger(ScriptExecutionTask.class);
    private final ScriptUnit scriptUnit;
    private final EventConsumers<Consumer<DeliveryEventConsumer>> eventConsumers;
    private final EventQueue eventQueue;
    private final String eventManagerHookAlias;
    
    public ScriptExecutionTask(ScriptUnit unit, EventQueue eventQueue, EventConsumers<Consumer<DeliveryEventConsumer>> eventConsumers, String hookAlias) {
        super();
        this.scriptUnit = unit;
        this.eventConsumers = eventConsumers;
        this.eventQueue = eventQueue;
        this.eventManagerHookAlias = hookAlias;
    }

    public String getId() {
        return scriptUnit.getId();
    }

    @Override
    public void run() {
        if(log.isDebugEnabled()) {
            log.debug("Run task for script id  " +  getId());
        }
        FileReader reader = null;
        ScriptEngine engine = null;
        notifyObservers(createStatus(ExecutionStatus.StatusType.IN_EXECUTION));
        try {
            reader = new FileReader(scriptUnit.getFile());
            ScriptEngineManager factory = new ScriptEngineManager();

            // create a JavaScript engine
            engine = factory.getEngineByName(scriptUnit.getScriptLanguage());
            if (engine == null) {
                throw new ScriptException("No script engine found for language " + scriptUnit.getScriptLanguage());
            }
            engine.put(eventManagerHookAlias, new JavaToScriptEventManager(scriptUnit));
            // evaluate JavaScript code from String
            engine.eval(reader);
            notifyObservers(createStatus(ExecutionStatus.StatusType.COMPLETED_SUCCESS));
            log.info(String.format("Script (%s, %s) completed successfully", scriptUnit.getId(), scriptUnit.getFile().getAbsolutePath()));
        } catch (Throwable e) {
            log.error(String.format("Script (%s, %s) Throwable excepted", scriptUnit.getId(), scriptUnit.getFile().getAbsolutePath()), e);
            notifyObservers(createStatus(ExecutionStatus.StatusType.COMPLETED_ERROR, e));
        } finally {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Script (%s, %s) execution ended", scriptUnit.getId(), scriptUnit.getFile().getAbsolutePath()));
            }
            IOUtils.closeQuietly(reader);
            engine = null;
        }
    }

    private ExecutionStatus createStatus(ExecutionStatus.StatusType type) {
        return createStatus(type, null);
    }

    private ExecutionStatus createStatus(ExecutionStatus.StatusType type, Throwable e) {
        setChanged();
        return new ExecutionStatus(type, (e != null ? e.getMessage():null), e);
    }

    /**
     * JavaToScriptEventManager exposes the event consumer register and event consuming interface to the scripting
     * programming environment.
     * It is thread safe
     */
    private class JavaToScriptEventManager implements JavaToScriptInterface {
        private final ScriptUnit unit; 
        
        private JavaToScriptEventManager(ScriptUnit unit) {
            this.unit = unit;
        }       
        
        /**
         * start traffic of events, blocked receiving the events, invoke script callback for each arrived event, 
         * return the after timeout expired
         */
        @Override
        public void startListening(long timeoutMillis) throws ScriptException {
            if(log.isDebugEnabled()) {
               log.debug("Id: " +  getId() + " - startBlockedListening, timeout = " + timeoutMillis);
            }
            
            try {
                eventQueue.waitForEvents(timeoutMillis);
            } catch (Exception e) {
                // log the error as this exception is not to be shown by the script engine
                log.error("Exception during waiting for events.", e);
                throw new ScriptException("Exception during waiting for events.", e);
            }
        }

        /** 
         * stop the flow of events, stuff a termination event into the queue and 
         * terminate the consumer thread (if applicable)
         * 
         */
        @Override
        public void stopListening(DeliveryEventConsumer deliveryEventConsumer) throws ScriptException {
            if(log.isDebugEnabled()) {
                log.debug("Id: " +  getId() + " - stopListening ");
             }                 
            try {
                eventConsumers.removeConsumer(new Consumer<DeliveryEventConsumer>(deliveryEventConsumer, this.unit.getFile().getAbsolutePath()));
            } catch (Exception e) {
                throw new ScriptException("Exception during stopping the connector.", e);
            }
        }

        @Override
        public String[] getArguments() {
            String[] args = this.unit.getArguments() != null ? this.unit.getArguments().split(" "):(new String[0]);
            return args;
        }

        /* (non-Javadoc)
         * @see com.solers.delivery.scripting.JavaToScriptInterface#addConsumer(com.solers.delivery.scripting.event.DeliveryEventConsumer)
         */
        @Override
        public void addConsumer(DeliveryEventConsumer deliveryEventConsumer) {
            eventConsumers.addConsumer(new Consumer<DeliveryEventConsumer>(deliveryEventConsumer, this.unit.getFile().getAbsolutePath()));
        }
    }
}
