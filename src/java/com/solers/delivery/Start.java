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

import java.io.PrintStream;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.solers.delivery.content.ContentSetManager;
import com.solers.delivery.security.SecurityProviderUtil;
import com.solers.util.LoggingOutputStream;


public final class Start {

	// TestPros, July 2010, Version 2.1.1 - made singleton to support context access needed
	// for enforcing REST account disabling after 3 failed logins
	private static Start deliveryService = null;
	
    private static final Logger log = Logger.getLogger(Start.class);
    
    private AbstractApplicationContext parent;
    private AbstractApplicationContext context;

    /**
     *  This method is only executed on the Windows platform.
     *  Solaris and Linux call init() and start() directly via JSVC
     * 
	 * TestPros, July 2010, Version 2.1.1 - uses singleton added to support context access needed
	 * for enforcing REST account disabling after 3 failed logins
     */
    public static void main(String[] args) throws Exception {
    	deliveryService = new Start();
    	deliveryService.init(null);
    	deliveryService.start();
    }
    
    public static Start getDeliveryService() {
    	return deliveryService;
    }
    
    /**
     * Jsvc lifecycle method.  When JSVC runs this method, it will be running as root.
     * 
     * Anything that is run in this method will be run by the root user.
     */
    public void init(String[] args) throws Exception {
    	if (deliveryService == null) deliveryService = this;
        System.setOut(new PrintStream(new LoggingOutputStream(log, Level.INFO), true));
        System.setErr(new PrintStream(new LoggingOutputStream(log, Level.ERROR), true));
        
        SecurityProviderUtil.init();
        if (! SecurityProviderUtil.inFIPSMode()) {
            log.info("Delivery stopped due to security provider initiaization problem");
            System.exit(1);
        }
        
        Runtime.getRuntime().addShutdownHook(new Shutdown("DeliveryShutdown"));
        
        parent = new ClassPathXmlApplicationContext(new String[] { "config.xml", "root-only.xml"});
    }
    
    /**
     * Jsvc lifecycle method.  When JSVC runs this method, it will be running as the "efd" user.
     * 
     * Anything that is run in this method will be run by the "efd" user.
     */
    public void start() throws Exception {
        
        String[] configs;
        boolean scriptingEnabled = Boolean.valueOf((((PropertyValue)parent.getBean("scriptingEnabled")).getValue().toString()));
        if (scriptingEnabled) {
            configs = new String[] { "scripting-client.xml","all.xml" };
        } else {
            configs = new String[] { "all.xml" };
        }
        context = new ClassPathXmlApplicationContext(configs, false, parent);
       
        context.addBeanFactoryPostProcessor(new BeanFactoryPostProcessor() {
            @Override
            public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
                for (Object o : parent.getBeansOfType(PropertyPlaceholderConfigurer.class).values()) {
                    BeanFactoryPostProcessor processor = (BeanFactoryPostProcessor) o;
                    processor.postProcessBeanFactory(beanFactory);
                }
            }
        });
        context.refresh();
        log.info("Delivery started");
    }

    /**
     * Jsvc lifecycle method
     */
    public void stop() {
        
    }

    /**
     * Jsvc lifecycle method
     */
    public void destroy() {
        
    }
    
    // TestPros, July 2010, Version 2.1.1 - made public to support context access needed
	// for enforcing REST account disabling after 3 failed logins
    public ConfigurableApplicationContext getContext() {
        return context;
    }
    
    protected ConfigurableApplicationContext getParentContext() {
        return parent;
    }
    
    private class Shutdown extends Thread {
        
        public Shutdown(String name) {
            super(name);
        }

        public void run() {
            log.info("Delivery stopping");
            // Spring's destroy facility requires adding dependencies to properly orchestrate the shutdown process
            // This would make it prone to breaking in the future so we opt to explicitly control the shutdown process here
            ((ContentSetManager)context.getBean("supplierContentSetManager")).shutdown();
            ((ContentSetManager)context.getBean("consumerContentSetManager")).shutdown();
            try {
                log.info("Waiting for content set managers...");
                Thread.sleep(5*1000);
            } catch (InterruptedException e) {
                log.error("Shutdown thread interrrupted while waiting for services to complete", e);
            }
            context.close();
            parent.close();
            log.info("Delivery stopped");
        }
    }
}
