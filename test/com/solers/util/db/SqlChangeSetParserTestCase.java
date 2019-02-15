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
import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.solers.util.db.SqlChangeSet;
import com.solers.util.db.SqlChangeSetParser;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class SqlChangeSetParserTestCase {
    
    SqlChangeSetParser parser;
    
    @Before
    public void setUp() {
        parser = new SqlChangeSetParser();
    }
    
    @Test
    public void testParse() throws Exception {
        File file = file("/sql.config.test");
        
        List<SqlChangeSet> result = parser.parse(file);
        
        Assert.assertEquals(2, result.size());
        
        Assert.assertEquals("1.0", result.get(0).getVersion());
        Assert.assertEquals(3, result.get(0).getStatements().size());
        Assert.assertEquals("DROP TABLE IF EXISTS `foo`", result.get(0).getStatements().get(0));
        Assert.assertEquals("DROP TABLE IF EXISTS `bar`", result.get(0).getStatements().get(1));
        Assert.assertEquals("INSERT INTO \"foo\" (ID,VALUE) VALUES (1,1)", result.get(0).getStatements().get(2));
        
        Assert.assertEquals("1.1", result.get(1).getVersion());
        Assert.assertEquals(3, result.get(1).getStatements().size());
        Assert.assertEquals("drop function if exists nvl", result.get(1).getStatements().get(0));
        Assert.assertEquals("create function nvl(value bigint, value2 bigint) returns bigint return ifnull(value, value2)", result.get(1).getStatements().get(1));
        Assert.assertEquals("DROP TABLE IF EXISTS `event_timed`", result.get(1).getStatements().get(2));
    }
    
    @Test
    public void testParseInvalid() throws Exception {
        File file = file("/sql.config.invalid");
        
        try {
            parser.parse(file);
            Assert.fail();
        } catch (IOException expected) {
            
        }
    }
    
    private File file(String name) throws Exception {
        URI uri = getClass().getResource(name).toURI();
        File file = new File(uri);
        return file;
    }
    
}
