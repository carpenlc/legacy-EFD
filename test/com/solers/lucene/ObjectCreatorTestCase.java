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
package com.solers.lucene;

import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import com.solers.lucene.Converter;
import com.solers.lucene.LuceneField;
import com.solers.lucene.ObjectCreator;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class ObjectCreatorTestCase extends TestCase {
    
    public void testCreate() {
        Document doc = new Document();
        doc.add(new Field("string", "123", Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("date", "200102190000", Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("someothername", "456", Field.Store.YES, Field.Index.NOT_ANALYZED));
        
        Test obj = ObjectCreator.create(Test.class, doc);
        assertNotNull(obj);
        
        assertEquals("123", obj.str);
        assertEquals(456l, obj.longValue);
        
        assertNotNull(obj.date);
        Calendar c = Calendar.getInstance();
        c.setTime(obj.date);
        
        assertEquals(2001, c.get(Calendar.YEAR));
        assertEquals(Calendar.FEBRUARY, c.get(Calendar.MONTH));
        assertEquals(19, c.get(Calendar.DAY_OF_MONTH));
        
        assertEquals("UNCHANGED", obj.nullValue);
    }
    
    public static class Test {
        
        String str;
        Date date;
        long longValue;
        String nullValue = "UNCHANGED";
        
        @LuceneField
        public void setString(String value) {
            str = value;
        }
        
        @LuceneField(converter=Converter.DATE)
        public void setDate(Date value) {
            date = value;
        }
        
        @LuceneField(name="someothername", converter=Converter.LONG)
        public void setLong(long value) {
            longValue = value;
        }
        
        @LuceneField
        public void setNull(String value) {
            nullValue = value;
        }
        
    }
    
}
