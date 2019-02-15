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
package com.solers.util.db;

import java.io.File;
import java.net.URI;

import org.junit.Assert;
import org.junit.Test;

import com.solers.util.db.SqlChangeSetManager;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class SqlChangeSetManagerTestCase {
    
    @Test
    public void testGetChangeSetsNoVersion() throws Exception {
        File sqlConfig = file("/sql.config.test");
        
        SqlChangeSetManager manager = new SqlChangeSetManager(sqlConfig, null);
        Assert.assertEquals(2, manager.getChangeSets().size());
        Assert.assertEquals("1.1", manager.getCurrentVersion());
        
        manager = new SqlChangeSetManager(sqlConfig, new File(".", "doesNotExist"));
        Assert.assertEquals(2, manager.getChangeSets().size());
        Assert.assertEquals("1.1", manager.getCurrentVersion());
        
        File emptyFile = new File(".", "testGetChangeSetsNoVersion");
        emptyFile.createNewFile();
        
        try {
            manager = new SqlChangeSetManager(sqlConfig, emptyFile);
            Assert.assertEquals(2, manager.getChangeSets().size());
            Assert.assertEquals("1.1", manager.getCurrentVersion());
        } finally {
            emptyFile.delete();
        }
    }
    
    @Test
    public void testGetChangeSetsCurrentVersion() throws Exception {
        File sqlConfig = file("/sql.config.test");
        File versionFile = file("/sql.config.version.current");
        
        SqlChangeSetManager manager = new SqlChangeSetManager(sqlConfig, versionFile);
        
        Assert.assertTrue(manager.getChangeSets().isEmpty());
        Assert.assertEquals("1.1", manager.getCurrentVersion());
    }
    
    @Test
    public void testGetChangeSetsOldVersion() throws Exception {
        File sqlConfig = file("/sql.config.test");
        File versionFile = file("/sql.config.version.10");
        
        SqlChangeSetManager manager = new SqlChangeSetManager(sqlConfig, versionFile);
        
        Assert.assertEquals(1, manager.getChangeSets().size());
        Assert.assertEquals("1.1", manager.getCurrentVersion());
        Assert.assertEquals("1.1", manager.getChangeSets().get(0).getVersion());
    }
    
    private File file(String name) throws Exception {
        URI uri = getClass().getResource(name).toURI();
        File file = new File(uri);
        return file;
    }
}
