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
/**
 * 
 */
package com.solers.delivery.scripting.security;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.solers.delivery.util.FileSystemUtil;

/**
 * @author mchen
 *
 */
public class ScriptingSecurityManagerTestCase extends TestCase {
    private ScriptingSecurityManager manager;
    private SecurityManager origManager;
    private ScriptEngine engine;
    private static final String EFD_HOME = System.getProperty("efd.home");
    
    @Override
    @Before
    public void setUp() {
        this.origManager = System.getSecurityManager();
        this.manager = new ScriptingSecurityManager();
        System.setSecurityManager(this.manager);
        
        ScriptEngineManager engineManager = new ScriptEngineManager();
        for(ScriptEngineFactory factory : engineManager.getEngineFactories()) {
            if (factory.getEngineName().equals("groovy")) {
                this.engine = factory.getScriptEngine();
            }
        }
        assertNotNull(this.engine);
    }
    
    @Override
    @After
    public void tearDown() {
        System.setSecurityManager(this.origManager);
    }
    
    @Test
    public void testCheckExec() {
        boolean exceptionThrown = false;
        
        try {
            this.engine.eval("Runtime.getRuntime().exec(\"netstat\");");
            fail("Should have thrown a SecurityException");
        } catch (ScriptException e) {
            assertTrue(e.getCause().getCause() instanceof SecurityException);
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown);
        exceptionThrown = false;
        
        try {
            this.engine.eval("System.getSecurityManager().checkExec(\"netstat\");");
            fail("Should have thrown a SecurityException");
        } catch (ScriptException e) {
            assertTrue(e.getCause().getCause() instanceof SecurityException);
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown);

        try {
            System.getSecurityManager().checkExec("netstat");
        } catch(SecurityException ex) {
            fail("Should be able to access ports if not being called from a script");
        }
}
    
    @Test
    public void testCheckExit() {
        boolean exceptionThrown = false;
        
        try {
            this.engine.eval("System.exit(0)");
            fail("Should have thrown a SecurityException");
        }catch (ScriptException e) {
            assertTrue(e.getCause().getCause() instanceof SecurityException);
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown);

        exceptionThrown = false;
        
        try {
            this.engine.eval("System.getSecurityManager().checkExit(0);");
            fail("Should have thrown a SecurityException");
        }catch (ScriptException e) {
            assertTrue(e.getMessage().indexOf("SecurityException") > -1);
        }
    }
    
    @Test
    public void testCheckConnect() throws UnknownHostException, IOException {
        boolean exceptionThrown = false;

        try {
            this.engine.eval("System.getSecurityManager().checkConnect(\"localhost\", 1234); System.getSecurityManager().checkConnect(\"localhost\", 1234, System.getSecurityManager().getSecurityContext());");
            fail("Should have thrown a SecurityException");
        }catch (ScriptException e) {
            assertTrue(e.getCause().getCause() instanceof SecurityException);
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown);
        
        try {
            System.getSecurityManager().checkConnect("localhost", 1234);
            System.getSecurityManager().checkConnect("localhost", 1234, System.getSecurityManager().getSecurityContext());
        } catch(SecurityException ex) {
            fail("Should be able to access ports if not being called from a script");
        }
    }
    
    @Test
    public void testCheckListen() {
        boolean exceptionThrown = false;

        try {
            this.engine.eval("System.getSecurityManager().checkListen(0);");
            fail("Should have thrown a SecurityException");
        }catch (ScriptException e) {
            assertTrue(e.getCause().getCause() instanceof SecurityException);
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown);
        
        try {
            System.getSecurityManager().checkListen(0);
        } catch(Exception ex) {
            fail("Should be able to bind to a socket if not being called from a script.");
        }
    }
    
    @Test
    public void testCheckAccept() {
        boolean exceptionThrown = false;
        
        try{
            this.engine.eval("System.getSecurityManager().checkAccept(\"localhost\", 1234);");
        }catch(ScriptException e) {
            assertTrue(e.getCause().getCause() instanceof SecurityException);
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown);
        
        try {
            System.getSecurityManager().checkAccept("foo", 1234);
        } catch(SecurityException ex) {
            fail("Should be able to call checkAccept if not being called from a script");
        }
    }
    
    @Test
    public void testCanLoadClasses() {
        try {
            this.engine.eval("import com.solers.delivery.scripting.event.DeliveryEventConsumer; System.out.println(DeliveryEventConsumer.class);");
        } catch(ScriptException ex) {
            ex.printStackTrace();
            fail("Shouldn't have thrown an exception");
        }
    }
    
    @Test
    public void testCannotReadFromPasswordDir() {
      String filePath = FileSystemUtil.correctPath(EFD_HOME + "/site/conf/security/dummy");
      this.runFileTest(filePath, "file.exists();");
      
      boolean exceptionThrown = false;
      
      try {
          System.getSecurityManager().checkRead(filePath, null);
      } catch (SecurityException ex) {
          exceptionThrown = true;
      }
      
      assertTrue(exceptionThrown);
    }
    
    @Test
    public void testCheckRead() throws IOException {
        String filePath1 = FileSystemUtil.correctPath(EFD_HOME + "/site/conf/security/dummy");
        String filePath2 = FileSystemUtil.correctPath(EFD_HOME + "/README.txt");
        File file1 = new File(filePath1);
        File file2 = new File(filePath2);
        
        //calling .exists() is fine because they are not being called from a script
        assertFalse(file1.exists());
        assertTrue(file2.exists());
        
        this.runFileTest(filePath1, "file.exists();");
        
        boolean exceptionThrown = false;
        
        //shouldn't throw exception since it's not in the security folder
        try {
            System.getSecurityManager().checkRead(filePath2, System.getSecurityManager().getSecurityContext());
        } catch (SecurityException ex) {
            exceptionThrown = true;
        }
        
        assertFalse(exceptionThrown);
    }

    @Test
    public void testCanWrite() throws IOException {
        String filePath = FileSystemUtil.correctPath(EFD_HOME + File.separator + "README.txt");
        File file = new File(filePath);
        //calling .canWrite() is fine because it's not being called from a script
        assertTrue(file.canWrite());
        //should throw an exception because it's being called from a script
        this.runFileTest(filePath, "file.canWrite();");
        this.runFileTest(filePath, "file.createNewFile()");

        boolean exceptionThrown = false;
        
        try{
            this.engine.eval("import java.io.File; File.createTempFile(\"xxx\", \"\");");
        }catch(ScriptException e) {
            e.printStackTrace();
            assertTrue(e.getCause().getCause() instanceof SecurityException);
            exceptionThrown = true;
        }

        assertFalse(exceptionThrown);
    }
    
    @Test
    public void testCanDelete() {
        String filePath = FileSystemUtil.correctPath(EFD_HOME + File.separator + "dummy");
        File file = new File(filePath);
        //calling .delete() is fine because it's not being called from a script
        assertFalse(file.delete());
        //calling .delete() should throw an exception because it's being called from a script
        this.runFileTest(filePath, "file.delete();");
        boolean exceptionThrown = false;
        
        try{
            this.engine.eval("import java.io.File; File file = File.createTempFile(\"xxx\", \"\"); file.delete();");
        }catch(ScriptException e) {
            e.printStackTrace();
            assertTrue(e.getCause().getCause() instanceof SecurityException);
            exceptionThrown = true;
        }

        assertFalse(exceptionThrown);
    }
    
    @Test
    public void testCannotResetSecurityManager() {
        String filePath = FileSystemUtil.correctPath(EFD_HOME + File.separator + "dummy");
        boolean exceptionThrown = false;
        try {
            this.engine.eval("import java.io.*; class Foo extends SecurityManager{}; System.setSecurityManager(new Foo()); File file = new File(\"" + filePath + "\"); file.canWrite();");
        } catch (ScriptException e) {
            exceptionThrown = true;
        } 
        
        //should not be able to create/set security manager
        assertTrue(exceptionThrown);
    }
    
    
    private void runFileTest(String filePath, String testCode) {
        String existsScript = "import java.io.*; File file = new File(\"" + filePath + "\");" + testCode;

        boolean exceptionThrown = false;
        
        try{
            this.engine.eval(existsScript);
        }catch(ScriptException e) {
            assertTrue(e.getCause().getCause() instanceof SecurityException);
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown);
    }
}
