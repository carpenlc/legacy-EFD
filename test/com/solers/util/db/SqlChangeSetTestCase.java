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

import org.junit.Assert;
import org.junit.Test;

import com.solers.util.db.SqlChangeSet;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class SqlChangeSetTestCase {
    
    @Test
    public void testNewerThan() {
        SqlChangeSet config = new SqlChangeSet("1.1", null);
        
        Assert.assertFalse(config.newerThan("1.2"));
        Assert.assertFalse(config.newerThan("1.2.0"));
        
        Assert.assertTrue(config.newerThan("1.0.0"));
        Assert.assertTrue(config.newerThan("1.0"));
        Assert.assertTrue(config.newerThan("0.9"));
    }
    
}