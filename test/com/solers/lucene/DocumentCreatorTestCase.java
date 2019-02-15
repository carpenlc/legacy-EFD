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

import java.io.Reader;
import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import com.solers.lucene.Converter;
import com.solers.lucene.Index;
import com.solers.lucene.LuceneField;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class DocumentCreatorTestCase extends TestCase {
    
    public void testCreate() {
        DocumentResult r = DocumentCreator.create(new Child());
        Document doc = r.getDocument();
        
        assertEquals(6, doc.getFields().size());
        
        Field field = doc.getField("string");
        assertNotNull(field);
        assertEquals("str", field.stringValue());
        assertTrue(field.isIndexed());
        assertTrue(field.isTokenized());
        assertTrue(field.isStored());
        
        field = doc.getField("bool");
        assertNotNull(field);
        assertEquals("false", field.stringValue());
        assertTrue(field.isIndexed());
        assertTrue(field.isStored());
        
        field = doc.getField("id");
        assertNotNull(field);
        assertEquals("2", field.stringValue());
        assertTrue(field.isIndexed());
        assertFalse(field.isTokenized());
        assertFalse(field.isStored());
        
        field = doc.getField("childValue");
        assertNotNull(field);
        assertEquals("3", field.stringValue());
        assertTrue(field.isIndexed());
        assertTrue(field.isStored());
        
        field = doc.getField("dateValue");
        assertNotNull(field);
        assertEquals("200802131022", field.stringValue());
        assertTrue(field.isIndexed());
        assertTrue(field.isStored());
        
        field = doc.getField("wasRenamed");
        assertNotNull(field);
        assertEquals("renamed", field.stringValue());
        assertTrue(field.isIndexed());
        assertTrue(field.isStored());
        
        Analyzer a = r.getAnalyzer();
        assertFalse(a.tokenStream("id", null) instanceof TestTokenStream);
        assertTrue(a.tokenStream("string", null) instanceof TestTokenStream);
    }
    
    private static class Parent {
        
        @LuceneField
        public boolean isBool() {
            return false;
        }
        
        @LuceneField(index=Index.TOKENIZED, store=true, analyzer=TestAnalyzer.class)
        public String getString() {
            return "str";
        }
        
        @LuceneField(index=Index.UN_TOKENIZED, store=false)
        public long getId() {
            return 2;
        }
        
        public long getIgnore() {
            return 3;
        }
    }
    
    private static class Child extends Parent {
        
        @LuceneField
        public long getChildValue() {
            return 3;
        }
        
        @LuceneField
        public String getNullValue() {
            return null;
        }
        
        @LuceneField(converter=Converter.DATE)
        public Date getDateValue() {
            Calendar c = Calendar.getInstance();
            
            c.set(Calendar.DAY_OF_MONTH, 13);
            c.set(Calendar.MONTH, Calendar.FEBRUARY);
            c.set(Calendar.YEAR, 2008);
            c.set(Calendar.HOUR_OF_DAY, 10);
            c.set(Calendar.MINUTE, 22);
            
            return c.getTime();
        }
        
        @LuceneField(name="wasRenamed")
        public String getRenamedField() {
            return "renamed";
        }
    }
    
    private static class TestAnalyzer extends Analyzer {

        public TestAnalyzer() {
            
        }
        
        @Override
        public TokenStream tokenStream(String fieldName, Reader reader) {
            return new TestTokenStream();
        }
        
    }
    
    private static class TestTokenStream extends TokenStream {
        
    }
}
