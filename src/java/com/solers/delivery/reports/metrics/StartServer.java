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
package com.solers.delivery.reports.metrics;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.solers.delivery.security.SecurityProviderUtil;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class StartServer {
    
    public static void main(String [] args) {
        SecurityProviderUtil.init();
        String [] configs = new String[] { "config.xml", "metrics-server-config.xml"};
        ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext(configs);
        ctx.registerShutdownHook();
    }
    
}
