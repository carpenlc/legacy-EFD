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
package com.solers.delivery.lucene;

import java.util.Calendar;

import junit.framework.TestCase;

import org.apache.lucene.search.MatchAllDocsQuery;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class QueryBuilderTestCase extends TestCase {
    
    public void testDate() {
        QueryBuilder q = new QueryBuilder();
        
        Calendar start = Calendar.getInstance();
        start.set(Calendar.DAY_OF_MONTH, 5);
        start.set(Calendar.MONTH, 2);
        start.set(Calendar.YEAR, 2007);
        start.set(Calendar.HOUR_OF_DAY, 9);
        start.set(Calendar.MINUTE, 10);
        
        Calendar end = Calendar.getInstance();
        end.set(Calendar.DAY_OF_MONTH, 2);
        end.set(Calendar.MONTH, 1);
        end.set(Calendar.YEAR, 2008);
        end.set(Calendar.HOUR_OF_DAY, 10);
        end.set(Calendar.MINUTE, 15);
        
        assertEquals("date:[200703050910 TO 200802021015]", q.addDate("date", start.getTime(), end.getTime()).toString());
        
        q = new QueryBuilder();
        assertEquals("date:[- TO 200802021015]", q.addDate("date", null, end.getTime()).toString());
        
        q = new QueryBuilder();
        assertEquals("date:[200703050910 TO ?]", q.addDate("date", start.getTime(), null).toString());
        
        q = new QueryBuilder();
        assertEquals("date:[- TO ?]", q.addDate("date", null, null).toString());
    }
    
    public void testEquals() {
        QueryBuilder q = new QueryBuilder();
        
        assertEquals("field:testValue", q.addEquals("field", "testValue").toString());
    }
    
    public void testAnd() {
        QueryBuilder q = new QueryBuilder();
        
        assertEquals("field:testValue AND date:[- TO ?]", q.addEquals("field", "testValue").addDate("date", null, null).toString());
    }
    
    public void testToQuery() {
        assertTrue(new QueryBuilder().toQuery() instanceof MatchAllDocsQuery);
    }
    
    public void testAddLike() {
        QueryBuilder q = new QueryBuilder();
        
        assertEquals("field:test* AND field:value*", q.addLike("field", "test value").toString());
    }
}
