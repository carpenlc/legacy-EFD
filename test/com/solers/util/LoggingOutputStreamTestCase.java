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
package com.solers.util;

import java.io.PrintWriter;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class LoggingOutputStreamTestCase extends TestCase {
    
    public void testLogging() {
        TestLogger logger = new TestLogger();
        PrintWriter writer = new PrintWriter(new LoggingOutputStream(logger, Level.INFO));
        writer.print("Test message");
        writer.close();
        
        assertEquals(Level.INFO, logger.level);
        assertEquals("Test message", logger.message);
        
    }
    
    public void testWhiteSpaceIsNotAppended() {
        TestLogger logger = new TestLogger();
        PrintWriter writer = new PrintWriter(new LoggingOutputStream(logger, Level.INFO));
        writer.println("Test message");
        writer.close();
        
        assertEquals(Level.INFO, logger.level);
        assertEquals("Test message", logger.message);
    }
    
    public void testOnlyWhitespace() {
        TestLogger logger = new TestLogger();
        PrintWriter writer = new PrintWriter(new LoggingOutputStream(logger, Level.INFO));
        writer.println("");
        writer.close();
        
        assertNull(logger.level);
        assertNull(logger.message);
    }
    
    class TestLogger extends Logger {
        
        Priority level;
        Object message;
        
        TestLogger() {
            super("TestLogger");
        }
        
        public void log(Priority p, Object message) {
            this.level = p;
            this.message = message;
        }
    }
    
}
