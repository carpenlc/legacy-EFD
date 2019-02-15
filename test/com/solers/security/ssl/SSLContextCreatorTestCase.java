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
package com.solers.security.ssl;

import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.solers.security.ssl.SSLContextCreator.SSLContextException;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class SSLContextCreatorTestCase {
    
    private Set<String> suites;
    
    @Before
    public void setUp() {
        suites = new HashSet<String>();
        suites.add("TLS_RSA_WITH_AES_128_CBC_SHA");
    }
    
    @Test
    public void testSocket() throws Exception {
        final String message = "message";
        final String response = "response";
        SSLContextCreator creator = new SSLContextCreator("password", 
            "password", 
            "password",
            "dev/security/portal_keystore.p12",
            "dev/security/portal_truststore.jks",
            suites,
            true,
            5000,
            new SecureRandom());
        creator.init();
        SSLContext ctx = SSLContext.getDefault();
        
        final SSLServerSocket socket = (SSLServerSocket) ctx.getServerSocketFactory().createServerSocket(0);
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.submit(new Callable<Void>() {
            public Void call() throws Exception {
                byte [] buf = new byte[message.length()];
                try {
                    Socket client = socket.accept();
                    InputStream input = client.getInputStream();
                    input.read(buf);
                    Assert.assertEquals(message, new String(buf));
                    client.getOutputStream().write(response.getBytes());
                    client.getOutputStream().flush();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                
                return null;
            }
        });
        
        Socket client = ctx.getSocketFactory().createSocket(InetAddress.getLocalHost(), socket.getLocalPort());
        client.getOutputStream().write(message.getBytes());
        client.getOutputStream().flush();
        InputStream input = client.getInputStream();
        byte [] buf = new byte[response.length()];
        input.read(buf);
        Assert.assertEquals(response, new String(buf));
        
        client.close();
        socket.close();
    }
    
    @Test
    public void testContext() throws Exception {
        SSLContextCreator creator = new SSLContextCreator("keyPassword", 
            "keystorePassword", 
            "truststorePassword",
            file("/security/keystore.p12"),
            file("/security/truststore.jks"),
            suites,
            true,
            5000,
            new SecureRandom());
        creator.init();
        
        SSLContext ctx = SSLContext.getDefault();
        
        Assert.assertEquals("TLS", ctx.getProtocol());
        Assert.assertEquals(5000, ctx.getClientSessionContext().getSessionTimeout());
        Assert.assertEquals(5000, ctx.getServerSessionContext().getSessionTimeout());
        
        SSLServerSocket socket = (SSLServerSocket) ctx.getServerSocketFactory().createServerSocket(0);
        Assert.assertTrue(socket.getNeedClientAuth());
        String [] enabledSuites = socket.getEnabledCipherSuites();
        Assert.assertEquals(1, enabledSuites.length);
        Assert.assertEquals(suites.iterator().next(), enabledSuites[0]);
    }
    
    @Test
    public void testInvalidPath() throws Exception {
        try {
            new SSLContextCreator("keyPassword", 
                "keystorePassword", 
                "truststorePassword",
                file("/security/keystore.p12"),
                "missing.jks",
                suites,
                true,
                5000,
                new SecureRandom());
            Assert.fail();
        } catch (SSLContextException expected) {
            
        }
        
        try {
            new SSLContextCreator("keyPassword", 
                "keystorePassword", 
                "truststorePassword",
                "missing.p12",
                file("/security/truststore.jks"),
                suites,
                true,
                5000,
                new SecureRandom());
            Assert.fail();
        } catch (SSLContextException expected) {
            
        }
    }
    
    @Test
    public void testInvalidPasswords() throws Exception {
        try {
           new SSLContextCreator("invalid", 
                "keystorePassword", 
                "truststorePassword",
                file("/security/keystore.p12"),
                file("/security/truststore.jks"),
                suites,
                true,
                5000,
                new SecureRandom());
           Assert.fail();
        } catch (SSLContextException expected) {
            
        }
        
        try {
            new SSLContextCreator("keyPassword", 
                 "invalid", 
                 "truststorePassword",
                 file("/security/keystore.p12"),
                 file("/security/truststore.jks"),
                 suites,
                 true,
                 5000,
                 new SecureRandom());
            Assert.fail();
         } catch (SSLContextException expected) {
             
         }
         
         try {
             new SSLContextCreator("keyPassword", 
                  "keystorePassword", 
                  "invalid",
                  file("/security/keystore.p12"),
                  file("/security/truststore.jks"),
                  suites,
                  true,
                  5000,
                  new SecureRandom());
             Assert.fail();
          } catch (SSLContextException expected) {
              
          }
    }
    
    private String file(String name) throws Exception {
        return new File((getClass().getResource(name)).toURI()).getAbsolutePath();
    }
}
