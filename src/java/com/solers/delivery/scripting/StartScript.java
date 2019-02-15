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

import org.apache.log4j.Logger;
import org.springframework.beans.PropertyValue;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.solers.delivery.scripting.input.ScriptInput;
import com.solers.delivery.scripting.security.ScriptingSecurityManager;
import com.solers.delivery.security.SecurityProviderUtil;

/**
 * Scripting application main program
 */
public class StartScript {
    private final static Logger log = Logger.getLogger(StartScript.class);
    
    private AbstractXmlApplicationContext ctx;  
    private StartScript() {
        super();      
    }    
    /** 
     * Start the the IOC mechanism
     */
    
    private void start(boolean verbose) {
        log.debug("EFD Scripting start");
        ctx = new ClassPathXmlApplicationContext(new String[] { "config.xml", "scripting-server.xml" });
        ctx.registerShutdownHook();
        String scriptingDir = ((PropertyValue)ctx.getBean("scriptingDirectory")).getValue().toString();

        try {
            ScriptInput input = new ScriptInput(scriptingDir);
            ScriptingApplication app = (ScriptingApplication) ctx.getBean("scriptingApplication");   
            app.start(input, verbose);
        } catch (Exception e) {
            log.error("Application terminated with error: ", e);
        } finally {
            ctx.close();
        }
        log.debug("EFD Scripting stop");
    }    
  
    public static void main(String[] args) {
        StartScriptCmdLneParser parser = parseAndValidateArgs(args);
        System.setSecurityManager(new ScriptingSecurityManager());
        SecurityProviderUtil.init();
        SecurityProviderUtil.getProvider();
        StartScript strScript = new StartScript();
        strScript.start(parser.isVerbose());
    }
    
    private static StartScriptCmdLneParser parseAndValidateArgs(String[] args) {
        StartScriptCmdLneParser parser = new StartScriptCmdLneParser(args);
        if (parser.getErrors() != null && parser.getErrors().size() > 0) {
            System.err.println("Error parsing command line: " );
            for(String error: parser.getErrors()) {
                System.err.println(error);
            }
            StartScriptCmdLneParser.printHelp();
            System.exit(0);
        }
        
        return parser;
    }
}
