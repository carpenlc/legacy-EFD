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
package com.solers.delivery;

import java.io.IOException;
import java.io.PrintStream;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.solers.delivery.security.SecurityProviderUtil;
import com.solers.util.LoggingOutputStream;

public final class StartWeb {

    private static final Logger log = Logger.getLogger(StartWeb.class);

    private ConfigurableApplicationContext ctx;
    
    public void start() {
        System.setOut(new PrintStream(new LoggingOutputStream(log, Level.INFO), true));
        System.setErr(new PrintStream(new LoggingOutputStream(log, Level.ERROR), true));
        
        ctx = new ClassPathXmlApplicationContext(new String[] { "./config.xml", "./web-container.xml" });
        ctx.registerShutdownHook();
        startupComplete();
    }
    
    public void startupComplete() {
        log.info("WebUI started");
    }

    public void stop() {
        if (ctx != null) {
            ctx.close();
        }
    }

    public static void main(String[] args) throws IOException {
        SecurityProviderUtil.init();
        StartWeb app = new StartWeb();
        app.start();
    }
}

