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
package com.solers.delivery.transport.http.client.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import com.solers.delivery.transport.http.client.TransferContent;
import com.solers.delivery.transport.http.client.TransferInventory;
import com.solers.delivery.transport.http.client.util.ContentCopy.FailedStreamToFileException;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class ContentCopyTestCase extends TestCase {
    
    ContentCopy cc;
    
    public void setUp() {
        cc = new ContentCopy();
    }
    
    public void testCopy() throws IOException {
        String path = "./ContentCopyTestCase.testCopy";
        String data = "testCopy";
        copy(path, data);
    }
    
    public void testShortFilenameCopy() throws IOException {
        String path = "./a";
        String data = "testShortFilenameCopy";
        copy(path, data);
    }
    
    public void testLongFilenameCopy() throws IOException {
        StringBuilder path = new StringBuilder(); 
        for (int i=1; i <= 200; i++) {
            path.append('a');
        }
        String data = "testLongFilenameCopy";
        copy("./"+path.toString(), data);
    }

    private void copy(String path, String data) throws IOException {
        TransferContent transfer = new TransferInventory(path, "ContentCopyTestCase", 0l, "");
        InputStream input = new ByteArrayInputStream(data.getBytes());
        
        File result = new File(path);
        result.deleteOnExit();
        
        assertEquals(0, transfer.getBytesTransferred());
        assertFalse(result.exists());
        try {
            cc.createFile(input, transfer);
        } catch (FailedStreamToFileException e) {
            fail(e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException ex) {
            fail(ex.getMessage());
            ex.printStackTrace();
        }
        assertTrue(result.exists());
        assertEquals(data, read(result));
        assertEquals(data.length(), transfer.getBytesTransferred());
    }
    
    private String read(File file) throws IOException {
        StringBuilder result = new StringBuilder();
        InputStream in = new FileInputStream(file);
        byte [] buffer = new byte[1024];
        
        int bytesRead = in.read(buffer);
        while (bytesRead != -1) {
            result.append(new String(buffer, 0, bytesRead));
            bytesRead = in.read(buffer);
        }
        in.close();
        return result.toString();
    }

}
