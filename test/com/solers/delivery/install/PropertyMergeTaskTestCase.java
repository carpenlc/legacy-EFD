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
import java.io.FileReader;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class PropertyMergeTaskTestCase {
    
    @Test
    public void testMerge() throws Exception {
        String path = "testMerge.properties";
        
        PropertyMergeTask p = new PropertyMergeTask();
        p.setOld(file("/old.properties"));
        p.setCurrent(file("/new.properties"));
        p.setResult(path);
        p.execute();
        
        Properties result = new Properties();
        FileReader r = new FileReader(path);
        result.load(r);
        r.close();
        
        try {
            Assert.assertEquals(2, result.stringPropertyNames().size());
            Assert.assertEquals("1", result.getProperty("old.value"));
            Assert.assertEquals("3", result.getProperty("new.value"));
        } finally {
            new File(path).delete();
        }
    }
    
    private String file(String name) throws Exception {
        return new File((getClass().getResource(name)).toURI()).getAbsolutePath();
    }
    
}
