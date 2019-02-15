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


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import junit.framework.TestCase;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.junit.Test;
import com.solers.delivery.MockInputHandler;


/**
 * @author Tim Heintz</a>
 */
public class VerifyJavaTaskTestCase extends TestCase {
    
	private VerifyJavaTask task;
	private Project project;
	private String jrePath = "C:\\Program Files\\Java\\JRE6";
		
	public void setUp() {
		task = new VerifyJavaTask();
        project = new Project();
        task.setProject(project);
	}
	
    @Test
    public void testCheckVersion() throws Exception {
		InputStream in = Runtime.getRuntime().exec("java -version").getErrorStream();
		BufferedReader impStm = new BufferedReader(new InputStreamReader(in));
		String str = impStm.readLine();
		int[] verData = task.parseVersionData(str);
		System.out.println("ver data = " + verData[0] + "." + verData[1] + "." + verData[2] + "_" + verData[3]);
		assertTrue(verData[0] == 1);
		assertTrue(verData[1] == 6);
		assertTrue(verData[2] == 0);
		assertTrue(verData[3] == 21);
    }
    
    public void testVerifyTaskExecute() {
    	String err = null;
        project.setInputHandler(new MockInputHandler(jrePath));
    	try {
    		task.execute();
    	} catch (BuildException e) {
    		err = e.getMessage();
    	}
    	assertNull(err);
        assertTrue(project.getProperty(VerifyJavaTask.JRE_PATH_PROP).equals(jrePath));
    }
    
    public void testVerifyTaskExecuteRenterPath() {
    	String err = null;
    	project.setInputHandler(new MockInputHandler("//", "Y", jrePath));
    	try {
    		task.execute();
    	} catch (BuildException e) {
    		err = e.getMessage();
    	}
       	assertNull(err);
        assertTrue(project.getProperty(VerifyJavaTask.JRE_PATH_PROP).equals(jrePath));
    }

    public void testVerifyTaskExecuteBadPath() {
    	String err = null;
    	project.setInputHandler(new MockInputHandler("//", "N"));
    	try {
    		task.execute();
    	} catch (BuildException e) {
    		err = e.getMessage();
    	}
    	assertNotNull(err);
        assertTrue(err.contains("EFD install stopped, pending install of JRE"));
    }
    
}
