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
import java.security.Security;

import junit.framework.TestCase;

import org.apache.tools.ant.Project;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.providers.encoding.PasswordEncoder;

import com.rsa.jsafe.provider.JsafeJCE;
import com.solers.delivery.MockInputHandler;
import com.solers.delivery.security.PasswordType;
import com.solers.delivery.util.password.UserPasswordEncoder;
import com.solers.security.EncryptionUtils;
import com.solers.security.password.DefaultPasswordProvider;
import com.solers.security.password.PasswordProvider;


/**
 * @author Tim Heintz</a>
 */
public class SecurityConfigTaskTestCase extends TestCase {
    
	public void setUp() throws Exception {
		File dir = new File("testsite/conf/security");
		dir.mkdirs();
	}

    @Test
    public void testSecuritySetup() throws Exception {
//    	PasswordProvider provider = new DefaultPasswordProvider("Test@Pros-10000".toCharArray(), new File("testsite/conf/security/passwd"));
        Security.insertProviderAt(new JsafeJCE(), 1);
//    	provider.setPassword(PasswordType.PKI_KEYSTORE.key(), "Test@Pros-10000");
//    	provider.setPassword(PasswordType.PKI_TRUSTSTORE.key(), "Test@Pros-10000");
    	
        Project project = new Project();
        String password = String.valueOf(EncryptionUtils.encrypt("Test@Pros-10000".toCharArray(), "Test@Pros-10000".getBytes()));
        project.setInputHandler(new MockInputHandler(password, "y", "C:\\Users\\Tim Heintz\\certs\\private.key", "C:\\Users\\Tim Heintz\\certs\\cert.crt"));
        
        SecuritySetupTask task = new SecuritySetupTask();

        task.setKeyStore("testsite/conf/security/portal_keystore.p12");
        task.setTrustStore("testsite/conf/security/portal_truststore.jks");
        task.setPasswordfile("testsite/conf/security/passwd");
        
        task.setProject(project);
        
        task.execute();
        Assert.assertEquals("true", project.getProperty("security.setup"));

    }
    
    public void tearDown() throws Exception {
    	File keyStore	= new File("testsite/conf/security/portal_keystore.p12");
    	File trustStore = new File("testsite/conf/security/portal_truststore.jks");
    	File password 	= new File("testsite/conf/security/passwd");
		File dir = new File("testsite/conf/security");
    	keyStore.delete();
    	trustStore.delete();
    	password.delete();
    	dir.delete();
    	dir = new File("testsite/conf");
    	dir.delete();
    	dir = new File("testsite");
    	dir.delete();
    }
}
