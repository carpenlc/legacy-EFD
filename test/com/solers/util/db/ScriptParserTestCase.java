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
import java.util.List;

import com.solers.util.db.ScriptParser;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class ScriptParserTestCase extends TestCase {

    public void testParse() throws Exception {
        URI uri = getClass().getResource("/script.sql").toURI();
        File file = new File(uri);
        
        ScriptParser parser = new ScriptParser();
        
        List<String> queries = parser.parse(file);
        
        assertEquals(4, queries.size());
        assertEquals("DROP TABLE IF EXISTS `password`", queries.get(0));
        assertEquals("CREATE TABLE `password` (    `id` bigint(20) NOT NULL auto_increment,    `password` varchar(255) NOT NULL,    `createdDate` datetime NOT NULL,    `users` bigint(20) default NULL,    PRIMARY KEY  (`id`),    KEY `FK4889BA9BBEC84CA` (`users`)  ) ENGINE=MyISAM DEFAULT CHARSET=latin1", queries.get(1));
        assertEquals("INSERT INTO `users` (`id`,`username`,`enabled`,`firstName`,`lastName`,`initialPassword`,`adminRole`,`lastLogin`,`failedLogins`)   VALUES (1,'admin',1,'Administrator','Administrator', 1 , 1, null,0)", queries.get(2));
        assertEquals("INSERT INTO password (password, createdDate, users)  VALUES ('@db.password@', now(), '1')", queries.get(3));
    }
}
