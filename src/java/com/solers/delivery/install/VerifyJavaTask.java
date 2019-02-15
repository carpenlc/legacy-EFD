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
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

public class VerifyJavaTask extends Task {

	public static final String JRE_PATH_PROP = "jre.package.dir";
	public static final String JVM_TYPE = "jvm.type";

	public static final String UNIX_JRE = "/usr/java/instances/jre7";
	public static final String WINDOWS_JRE = "c:\\program files (x86)\\Java\\jre8";

	private boolean isUnix = false;
	
	public void setIsUnix(boolean isUnix) {
		this.isUnix = isUnix;
	}
	
    public void execute() {
    	Project ant = getProject();
    	boolean foundJre = false;
    	String jrePath = null;
    	boolean clientVer = false;
    	String defaultJre = (isUnix) ? UNIX_JRE: WINDOWS_JRE;
        String ext =  (isUnix) ? "": ".exe"; 
        
    	while (! foundJre) {
    		jrePath = getJrePath(defaultJre, ! isUnix);

    		// check for JRE existence 
    		File javaExe = new File(jrePath + "/bin/java" + ext);
    		if (! javaExe.exists()) {
    			StringRequest req = new StringRequest("JRE not found at specified path, do you wish to reenter the path " +
    					"(otherwise install will abort, pending install of JRE)", "Y"); 
    	        getProject().getInputHandler().handleInput(req);
    			if (req.getInput().equalsIgnoreCase("Y")) continue; 
    	    	ant.log("EFD install stopped, pending install of JRE", Project.MSG_INFO);
    			throw new BuildException("EFD install stopped, pending install of JRE");
    		}

    		// check for JRE version
    		BufferedReader impStm = null;
    		try {
    			InputStream in = Runtime.getRuntime().exec(jrePath + "/bin/java" + ext + " -server -version").getErrorStream();
    			impStm = new BufferedReader(new InputStreamReader(in));
    			String str = impStm.readLine();
    			ant.log("Verifying 'java -server -version'", Project.MSG_INFO);
    			int[] verData = parseVersionData(str);
    			
    			// failed java server version verification, try client version verification
    			if (verData.length == 1 && verData[0] == 0) {
    				ant.log("Java Server version verification result: " + str, Project.MSG_INFO);
        			ant.log("No server version available - trying 'java -client -version'", Project.MSG_INFO);
        			in = Runtime.getRuntime().exec(jrePath + "/bin/java"+ ext + " -client -version").getErrorStream();
        			impStm = new BufferedReader(new InputStreamReader(in));
        			str = impStm.readLine();
        			verData = parseVersionData(str);
        	    	clientVer = true;
    			}

     			if (verData.length < 4) {
        			ant.log("Unable to parse java version string: " + str);
        			throw new BuildException("EFD install stopped, unable to parse Java version data (Details in install log; Contact Technical Support)");   				
    			}
    			if (verData[0] < 1 || verData[1] < 8) {
        	    	ant.log("EFD install stopped, pending install of correct JRE Version ", Project.MSG_INFO);
        	    	ant.log("Version check response ...  " + str, Project.MSG_INFO);
        			throw new BuildException("EFD install stopped, JRE version must be at least 1.8, please install correct version");    				
    			}
    			//if (verData[0] == 1 && verData[1] == 8 && verData[2] == 0 && verData[3] < 15) {
        	    	//ant.log("EFD install stopped, pending install of correct JRE 1.8.0 build ", Project.MSG_INFO);
           		//	throw new BuildException("EFD install stopped, JRE 1.8.0, build version must be 91 or greater, please install correct version");    				
    			//}
    		} catch (BuildException be) {
    			throw be;
    		} catch (Exception ioe) {
    	    	ant.log("EFD install stopped due to exception ", ioe, Project.MSG_ERR);
    			throw new BuildException("EFD install stopped, unable to verify Java version (Details in install log; Contact Technical Support)", ioe);
    		}
    		foundJre = true;
     	}
    	
    	if (clientVer) {
    		ant.setProperty(JVM_TYPE, "client");
       		ant.log("Java version successfully verified, Java running in client mode");
    	} else {
    		ant.setProperty(JVM_TYPE, "server");    		
       		ant.log("Java version successfully verified, Java running in server mode");
    	}
    	ant.setProperty(JRE_PATH_PROP, jrePath);

    }


    public int[] parseVersionData(String str) {
    	int[] noServerVer = {0};
    	int[] nul = {};
    	if (str.split("no .server. JVM").length > 1) return noServerVer;
    	if (! str.contains("java version")) return nul;
    	int[] res =  new int[4];
    	String items[] = str.split("[\"\\._-]");
    	if (items.length < 5) return nul;
    	try {
    		res[0] = Integer.parseInt(items[1]);
    		res[1] = Integer.parseInt(items[2]);
    		res[2] = Integer.parseInt(items[3]);
    		res[3] = Integer.parseInt(items[4]);
    	} catch (NumberFormatException e) {
    		return nul;
    	}
    	return res;
    }
    
    private String getJrePath(String defaultPath, boolean containSpaces) {
    	System.out.println("default Path  = "+ defaultPath);
        PathRequest request = new PathRequest("What is the path to the installed JRE", defaultPath, 
        										false, true, containSpaces, PathRequest.PathType.DIRECTORY, PathRequest.PathPermission.ANY);
        getProject().getInputHandler().handleInput(request);
        return request.getInput();
    }
        
    public static void main(String[] args) {
    	Project project = new Project();
    	Task task = new VerifyJavaTask();
    	task.setProject(project);
    	task.execute();
    	System.out.println(project.getProperty(JRE_PATH_PROP));
    	System.out.println("completed verification");
    }

}
