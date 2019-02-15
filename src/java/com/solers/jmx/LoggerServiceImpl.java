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
package com.solers.jmx;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.solers.jmx.registry.AutoRegister;
import com.solers.jmx.registry.AutoRegisterObjectName;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
@AutoRegister(category="Logging")
public class LoggerServiceImpl implements LoggerService {

    private static final Logger log = Logger.getLogger(LoggerServiceImpl.class);
    
    @Override
    @AutoRegisterObjectName
    public String toString() {
        return "Logger Service";
    }
    
    @Override
    public String getCurrentLevel() {
        return Logger.getRootLogger().getLevel().toString();
    }

    @Override
    public void useDebug() {
        setLevel(Level.DEBUG);
    }

    @Override
    public void useInfo() {
        setLevel(Level.INFO);
    }

    @Override
    public void useTrace() {
        setLevel(Level.TRACE);
    }

    @Override
    public void useError() {
        setLevel(Level.ERROR);
    }

    @Override
    public void useWarn() {
        setLevel(Level.WARN);
    }
    
    private void setLevel(Level level) {
        log.info(String.format("Changing root logger level from %s to %s", getCurrentLevel(), level.toString()));
        Logger.getRootLogger().setLevel(level);
    }
}
