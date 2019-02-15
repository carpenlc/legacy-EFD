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
package com.solers.delivery.install;

import java.io.File;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.input.InputRequest;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class CRLSetupTaskTestCase {
    
    @Test
    public void testDisableCRL() {
        Project project = new Project();
        project.setInputHandler(new MockInputHandler("y"));
        
        CRLSetupTask task = new CRLSetupTask();
        task.setProject(project);
        
        task.execute();
        
        Assert.assertEquals("false", project.getProperty("crl.enable"));
        Assert.assertEquals("true", project.getProperty("crl.dynamic"));
        Assert.assertEquals("", project.getProperty("crl.stores"));
    }
    
    @Test
    public void testEnableCRLEnableDynamic() {
        Project project = new Project();
        project.setInputHandler(new MockInputHandler("n", "n"));
        
        CRLSetupTask task = new CRLSetupTask();
        task.setProject(project);
        
        task.execute();
        
        Assert.assertEquals("true", project.getProperty("crl.enable"));
        Assert.assertEquals("true", project.getProperty("crl.dynamic"));
        Assert.assertEquals("", project.getProperty("crl.stores"));
    }
    
    @Test
    public void testEnableCRLDisableDynamic() throws Exception { 
        File path = new File(".", ".");
        
        Project project = new Project();
        project.setInputHandler(new MockInputHandler("n", "y", path.getAbsolutePath()));
        
        CRLSetupTask task = new CRLSetupTask();
        task.setProject(project);
        
        task.execute();
        
        Assert.assertEquals("true", project.getProperty("crl.enable"));
        Assert.assertEquals("false", project.getProperty("crl.dynamic"));
        Assert.assertEquals(path.getCanonicalFile(), new File(project.getProperty("crl.stores")).getCanonicalFile());
    }
    
    private class MockInputHandler extends InputHandler {
        
        private final String [] values;
        private int count;
        
        public MockInputHandler(String... values) {
            this.values = values;
        }
        
        @Override
        public void handleInput(InputRequest request) {
            request.setInput(values[count++]);
        }
        
    }
    
}
