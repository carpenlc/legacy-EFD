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
package com.solers.delivery.reports.metrics.server.csv;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class CsvStoreTestCase {
    
    private long time = System.currentTimeMillis();
    private File root;
    private String template = "<synchronization>                      "+
    "                               <adds>%d</adds>"+
    "<bytesAdded>         %d</bytesAdded>"+
    "<bytesDeleted>%d</bytesDeleted>"+
    "<bytesFailed>%d             </bytesFailed>"+
    "<bytesUpdated>%d</bytesUpdated>"+
    "                                   "+
    "<deletes>%d</deletes>             "+
    "<elapsedTime>%d</elapsedTime>"+
    "<failures>%d</failures>"+
    "<host>%s</host>"+
    "           <id>%d</id>"+
    "<time>%d</time>"+
    "<updates>%d</updates>"+
  "              </synchronization>";
    
    @Before
    public void setUp() {
        root = new File(getClass().getName());
    }
    
    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(root);
    }
    
    @Test
    public void testSuppliersOnly() throws Exception {
        int count = 2;
        String hostname = "myhost";
        
        StringBuilder xml = new StringBuilder("<metrics>");
        xml.append(supplierXML(count));
        xml.append("</metrics>");
        
        CsvStore store = new CsvStore(root);
        store.store(hostname, xml.toString());
        
        File [] dirs = root.listFiles();
        Assert.assertEquals(1, dirs.length);
        File hostDir = dirs[0];
        Assert.assertEquals(hostname, hostDir.getName());
        File [] subDirs = hostDir.listFiles();
        Assert.assertEquals(2, subDirs.length);
        
        File consumerDir = new File(hostDir, "consumers");
        File supplierDir = new File(hostDir, "suppliers");
        Assert.assertTrue(consumerDir.exists());
        Assert.assertTrue(supplierDir.exists());
        
        File [] files = consumerDir.listFiles();
        Assert.assertEquals(0, files.length);
        
        files = supplierDir.listFiles();
        Assert.assertEquals(1, files.length);
        test(count, files[0]);
    }
    
    @Test
    public void testConsumersOnly() throws Exception {
        int count = 2;
        String hostname = "myhost";
        
        StringBuilder xml = new StringBuilder("<metrics>");
        xml.append(consumerXML(count));
        xml.append("</metrics>");
        
        CsvStore store = new CsvStore(root);
        store.store(hostname, xml.toString());
        
        File [] dirs = root.listFiles();
        Assert.assertEquals(1, dirs.length);
        File hostDir = dirs[0];
        Assert.assertEquals(hostname, hostDir.getName());
        File [] subDirs = hostDir.listFiles();
        Assert.assertEquals(2, subDirs.length);
        
        File consumerDir = new File(hostDir, "consumers");
        File supplierDir = new File(hostDir, "suppliers");
        Assert.assertTrue(consumerDir.exists());
        Assert.assertTrue(supplierDir.exists());
        
        File [] files = consumerDir.listFiles();
        Assert.assertEquals(1, files.length);
        test(count, files[0]);
        
        files = supplierDir.listFiles();
        Assert.assertEquals(0, files.length);
    }
    
    @Test
    public void testSuppliersAndConsumer() throws Exception {
        int count = 2;
        String hostname = "myhost";
        
        StringBuilder xml = new StringBuilder("<metrics>");
        xml.append(consumerXML(count));
        xml.append(supplierXML(count));
        xml.append("</metrics>");
        
        CsvStore store = new CsvStore(root);
        store.store(hostname, xml.toString());
        
        File [] dirs = root.listFiles();
        Assert.assertEquals(1, dirs.length);
        File hostDir = dirs[0];
        Assert.assertEquals(hostname, hostDir.getName());
        File [] subDirs = hostDir.listFiles();
        Assert.assertEquals(2, subDirs.length);
        
        File consumerDir = new File(hostDir, "consumers");
        File supplierDir = new File(hostDir, "suppliers");
        Assert.assertTrue(consumerDir.exists());
        Assert.assertTrue(supplierDir.exists());
        
        File [] files = consumerDir.listFiles();
        Assert.assertEquals(1, files.length);
        test(count, files[0]);
        
        files = supplierDir.listFiles();
        Assert.assertEquals(1, files.length);
        test(count, files[0]);
    }
    
    private void test(int count, File file) throws Exception {
        String result = FileUtils.readFileToString(file);
        
        String [] rows = result.split("\r\n");
        
        Assert.assertEquals((count+1), rows.length);
        
        Assert.assertEquals("adds,bytesAdded,bytesDeleted,bytesFailed,bytesUpdated,deletes,elapsedTime,failures,host,id,time,updates,", rows[0]);
        
        for (int row=1, i=0; row < rows.length; row++, i++) {
            String [] values = rows[row].split(",");
            
            Assert.assertEquals(String.valueOf(i), values[0]);
            Assert.assertEquals(String.valueOf(i+1), values[1]);
            Assert.assertEquals(String.valueOf(i+2), values[2]);
            Assert.assertEquals(String.valueOf(i+3), values[3]);
            Assert.assertEquals(String.valueOf(i+4), values[4]);
            Assert.assertEquals(String.valueOf(i+5), values[5]);
            Assert.assertEquals(String.valueOf(time+i), values[6]);
            Assert.assertEquals(String.valueOf(i+7), values[7]);
            Assert.assertEquals(String.valueOf("127.0.0."+i), values[8]);
            Assert.assertEquals(String.valueOf(i+8), values[9]);
            Assert.assertEquals(String.valueOf(time+(i+1)), values[10]);
            Assert.assertEquals(String.valueOf(i+9), values[11]);
        }
    }
    
    private String consumerXML(int count) {
        StringBuilder xml = new StringBuilder("<consumer>");
        for (int i=0; i < count; i++) {
            xml.append("<list>");
            xml.append(String.format(template, 
                i, 
                i+1, 
                i+2, 
                i+3, 
                i+4, 
                i+5,
                time+i,
                i+7,
                "127.0.0."+i,
                i+8,
                time+(i+1),
                i+9));
            xml.append("</list>");
        }
        xml.append("</consumer>");
        return xml.toString();
    }
    
    private String supplierXML(int count) {
        StringBuilder xml = new StringBuilder("<supplier>");
        for (int i=0; i < count; i++) {
            xml.append("<list>");
            xml.append(String.format(template, 
                i, 
                i+1, 
                i+2, 
                i+3, 
                i+4, 
                i+5,
                time+i,
                i+7,
                "127.0.0."+i,
                i+8,
                time+(i+1),
                i+9));
            xml.append("</list>");
        }
        xml.append("</supplier>");
        return xml.toString();
    }
}
