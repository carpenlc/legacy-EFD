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
package com.solers.security.password;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class PasswordReaderTestCase {
    
    PasswordReader reader;
    
    @Before
    public void setUp() {
        reader = new PasswordReader();
    }
    
    @Test
    public void testRead() {
        InputStream in = new ByteArrayInputStream("password\n".getBytes());
        
        Assert.assertArrayEquals("password".toCharArray(), reader.readPassword(in));
    }
    
    @Test
    public void testEmptyRead() {
        Assert.assertArrayEquals("".toCharArray(), reader.readPassword(new ByteArrayInputStream("\n".getBytes())));
        Assert.assertArrayEquals("".toCharArray(), reader.readPassword(new ByteArrayInputStream("\r\n".getBytes())));
        Assert.assertArrayEquals("".toCharArray(), reader.readPassword(new ByteArrayInputStream("\r".getBytes())));
        Assert.assertArrayEquals("".toCharArray(), reader.readPassword(new ByteArrayInputStream("\r\n".getBytes())));
    }
    
    @Test
    public void testNewlines() {
        Assert.assertArrayEquals("password".toCharArray(), reader.readPassword(new ByteArrayInputStream("password\n".getBytes())));
        Assert.assertArrayEquals("password".toCharArray(), reader.readPassword(new ByteArrayInputStream("password\r\n".getBytes())));
        Assert.assertArrayEquals("password".toCharArray(), reader.readPassword(new ByteArrayInputStream("password\r".getBytes())));
        Assert.assertArrayEquals("password".toCharArray(), reader.readPassword(new ByteArrayInputStream("password\n\r".getBytes())));
    }
    
    @Test
    public void testReadFromStdin() {
        InputStream originalIn = System.in;
        System.setIn(new ByteArrayInputStream("password\n".getBytes()));
        
        Assert.assertArrayEquals("password".toCharArray(), reader.readPassword());
        
        System.setIn(originalIn);
    }
}
