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
package com.solers.delivery.scripting.security;

import java.io.File;
import java.io.FileDescriptor;
import java.security.Permission;

import javax.script.AbstractScriptEngine;

import org.apache.log4j.Logger;

import com.solers.delivery.util.FileSystemUtil;

public class ScriptingSecurityManager extends SecurityManager {
    private final static Logger log = Logger.getLogger(ScriptingSecurityManager.class);
    private final static String EFD_HOME_PROPERTY_KEY = "efd.home";
    private final static String EFD_DIR =
        FileSystemUtil.correctPath(System.getProperty(EFD_HOME_PROPERTY_KEY));
    private final static String EFD_PASSWORD_DIR = 
        FileSystemUtil.correctPath(String.format("%s/site/conf/security", 
            System.getProperty(EFD_HOME_PROPERTY_KEY), 
            File.separator));
    //cannot use enum - circularity error
    //will have to have enum be a standalone public class and load that class
    //before setting the security manager - too much of a hack.  Using final static string instead.
    private final static String ACTION_READ = "read";
    private final static String ACTION_WRITE = "write";
    private final static String ACTION_DELETE = "delete";
    
    /**
     * Override checkPermission to disallow runtime permission originating
     * from scripts that starts with 'set' before calling the superclass
     * @param perm - permission to be check
     * @throw SecurityException if 'set' runtime permission is request from a script
     */
    @Override
        
    // KRJ 2016-08-25: Made this method final based on HP Fortify recommendation
    
    public final void checkPermission(Permission perm) {
        this.detectRuntimePermission(perm);
        super.checkPermission(perm);
    }
    
    /**
     * Override checkPermission to disallow runtime permission originating
     * from scripts that starts with 'set' before calling the superclass
     * @param perm - permission to be check
     * @throw SecurityException if 'set' runtime permission is request from a script
     */
    @Override
    
    // KRJ 2016-08-25: Made this method final based on HP Fortify recommendation
    
    public final void checkPermission(Permission perm, Object context) {
        this.detectRuntimePermission(perm);
        super.checkPermission(perm, context);
    }
    
    /**
     * Overrides checkAccept to only accept the predetermined 
     * RMI host and port 
     * 
     * @param hostIP - host name to be checked
     * @param port - port number to be checked
     * 
     * @throws SecurityException if the host or port is not permitted
     */
    @Override
        
    // KRJ 2016-08-25: Made this method final based on HP Fortify recommendation
    
    public final void checkAccept(String hostIP, int port) {
        this.canAccessHostAndPort(hostIP, port);
        super.checkAccept(hostIP, port);
    }

    /**
     * Overrides checkConnect to only accept the predetermined 
     * RMI host and port 
     * 
     * @param hostIP - host name to be checked
     * @param port - port number to be checked
     * 
     * @throws SecurityException if the host or port is not permitted
     */
    @Override
        
    // KRJ 2016-08-25: Made this method final based on HP Fortify recommendation
    
    public final void checkConnect(String hostIP, int port) {
        this.canAccessHostAndPort(hostIP, port);
        super.checkConnect(hostIP, port);
    }
    
    /**
     * Overrides checkConnect to only accept the predetermined 
     * RMI host and port
     * 
     *  @param hostIP - host name to be checked
     *  @param port - port number to be checked
     *  @param context - security context
     *  @throws SecurityException if the host or port are not permitted
     */
    @Override
        
    // KRJ 2016-08-25: Made this method final based on HP Fortify recommendation
    
    public final void checkConnect(String hostIP, int port, Object context) {
        this.canAccessHostAndPort(hostIP, port);
        super.checkConnect(hostIP, port, context);
    }
    
    /**
     * Overrides checkListen to only accept the predetermined 
     * RMI port
     * 
     * @param port - port number to be checked
     * @throws SecurityException if the port is not permitted 
     */
    @Override
        
    // KRJ 2016-08-25: Made this method final based on HP Fortify recommendation
    
    public final void checkListen(int port) {
        this.canAccessHostAndPort(null, port);
        super.checkListen(port);
    }
    
    /**
     * Overrides checkExit to prevent any scripts from calling System.exit().
     * This is done by checking the class context for AbstractScriptEngine class
     * which is being used to execute all the scripts.
     * 
     * @param status - exit status
     * @throws SecurityException if this is called by a script
     */
    @Override
        
    // KRJ 2016-08-25: Made this method final based on HP Fortify recommendation
    
    public final void checkExit(int status) {
        this.canCallCheckExit();
        super.checkExit(status);
    }
    
    /**
     * This method throws a SecurityException if AbstractScriptEngine.class 
     * is in the class context to prevent script from creating a sub process.
     * @param cmd - command to be executed
     * @throws SecurityException if originated from a script.
     */
    @Override
        
    // KRJ 2016-08-25: Made this method final based on HP Fortify recommendation
    
    public final void checkExec(String cmd) {
        this.checkClassContext();
        super.checkExec(cmd);
    }
    
    /**
     * Simply call super.checkRead(FileDescriptor). Can't have a file descriptor without
     * first calling checkRead(String)
     */
    @Override
        
    // KRJ 2016-08-25: Made this method final based on HP Fortify recommendation
    
    public final void checkRead(FileDescriptor fd) {
        super.checkRead(fd);
    }
    
    /**
     * Calls canAccessFile before calling super.checkDelete()
     * @param file - path to file to be deleted
     * @throws SecurityException if originated from a script and file path is within EFD_HOME.
     * @throws NullPointerException if file is null.
     */
    @Override
        
    // KRJ 2016-08-25: Made this method final based on HP Fortify recommendation
    
    public final void checkDelete(String file) {
        this.canAccessFile(file, ACTION_DELETE);
        super.checkDelete(file);
    }
    
    /**
     * Calls canAccessFile before calling super.checkRead(String)
     * @param file - path to file to be read
     * @throws SecurityException if originated from a script and file path is within EFD_HOME.
     * @throws NullPointerException if file is null.
     */
    @Override
        
    // KRJ 2016-08-25: Made this method final based on HP Fortify recommendation
    
    public final void checkRead(String file) {
        this.canAccessFile(file, ACTION_READ);
        super.checkRead(file);
    }
    
    /**
     * Calls canAccessFile before calling super.checkRead(String, Object)
     * @param file - path to file to be read
     * @param context - specific security context
     * @throws SecurityException if originated from a script and file path is within EFD_HOME.
     * @throws NullPointerException if file is null.
     */
    @Override
        
    // KRJ 2016-08-25: Made this method final based on HP Fortify recommendation
    
    public final void checkRead(String file, Object context) {
        this.canAccessFile(file, ACTION_READ);
        super.checkRead(file, context);
    }

    /**
     * Simply call super.checkWrite(FileDescriptor). Can't have a file descriptor without
     * first calling checkWrite(String)
     */
    @Override
        
    // KRJ 2016-08-25: Made this method final based on HP Fortify recommendation
    
    public final void checkWrite(FileDescriptor fd) {
        super.checkWrite(fd);
    }
    
    /**
     * Calls canAccessFile before calling super.checkWrite(String)
     * @param file - path to file to be written to
     * @throws SecurityException if originated from a script and file path is within EFD_HOME.
     * @throws NullPointerException if file is null.
     */
    @Override
        
    // KRJ 2016-08-25: Made this method final based on HP Fortify recommendation
    
    public final void checkWrite(String file) {
        this.canAccessFile(file, ACTION_WRITE);
        super.checkWrite(file);
    }
    
    /**
     * This method throws a SecurityException if AbstractScriptEngine.class
     * is in the class context.
     * 
     * Override checkXXX methods in the SecurityManager to call this method if
     * we do not want the scripts to have permission to make such calls
     * 
     * @throws SecurityException if the script engine class is in the class context
     */
        
    // KRJ 2016-08-25: Made this method final based on HP Fortify recommendation
    
    public final void checkClassContext() {
        for (Class<?> klass : this.getClassContext()) {
            if (klass == AbstractScriptEngine.class) {
                throw new SecurityException();
            }
        }
    }

    /**
     * This method simply throws a SecurityException if AbstractScriptEngine
     * is in the class context and ignores the host/port being passed in since
     * we do not allow scripts to access any ports.
     * 
     * Update the implementation if we want to allow certain host/port
     * combinations.
     * 
     * @param host - ignored
     * @param port - ignored
     * @throws SecurityException if AbstractScriptEngine is in the class context
     */
    private void canAccessHostAndPort(String host, int port) throws SecurityException {
        try {
            checkClassContext();
        } catch(SecurityException ex) {
            log.warn("Script tries to access ports.");
            throw new SecurityException("Scripts are not allowed to access ports.");
        }
    }

    /**
     * This method simply throws a SecurityException if AbsctractScriptEngine is 
     * in the class context since we do not allow scripts to call System.exit()
     * 
     * @throws SecurityException if AbstractScriptEngine is in the class context
     */  
    
    // KRJ 2016-08-25: Made this method final based on HP Fortify recommendation
    
    private final void canCallCheckExit() throws SecurityException {
        try {
            checkClassContext();
        } catch(SecurityException ex) {
            log.warn("Script calls System.exit()");
            throw new SecurityException("Scripts are not allowed to call System.exit().");
        }
    }
    
    /**
     * Do not allow scripts read access to ${efd.home}/site/conf/security and delete/write access
     * to ${efd.home}
     * 
     * @param file - path to file to be accessed
     * @throws SecurityException if originated from a script and file path is within EFD_HOME.
     * @throws NullPointerException if file is null.
     */
    private void canAccessFile(String file, String action) {
        String correctedPath = FileSystemUtil.correctPath(file);
        if ((!action.equals(ACTION_READ) && correctedPath.startsWith(EFD_DIR)) ||
            (action.equals(ACTION_READ) && correctedPath.startsWith(EFD_PASSWORD_DIR))) {
            this.checkClassContext();
        }
    }

    /**
     * This method calls checkClassContext if it's processing a RuntimePermission that starts with 'set'
     * to prevent scripts from making changes to the Runtime environment, e.g. overriding security manager
     * 
     * @param perm - permission to be checked
     */
    private void detectRuntimePermission(Permission perm) {
        if (perm instanceof RuntimePermission) {
            if (perm.getName().startsWith("set")) {
                this.checkClassContext();
            }
        }
    }

}
