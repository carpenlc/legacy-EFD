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

import junit.framework.TestCase;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.store.RAMDirectory;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class PaginatedHitsListTestCase extends TestCase {
    
    public void testIterate() throws Exception {
        RAMDirectory index = new RAMDirectory();
        IndexWriter writer = new IndexWriter(index, new StandardAnalyzer(), true, IndexWriter.MaxFieldLength.UNLIMITED);
        
        int items = 97;
        
        for (int i=0; i < items; i++) {
            Document d = new Document();
            d.add(new Field("id", i+"", Field.Store.YES, Field.Index.NOT_ANALYZED));
            d.add(new Field("name", "Foo", Field.Store.YES, Field.Index.NOT_ANALYZED));
            writer.addDocument(d);
        }
        
        writer.optimize();
        writer.close();
        
        PaginatedHitsList<Person> result = new PaginatedHitsList<Person>(25, index, new MatchAllDocsQuery(), Person.class);
        result.initialize();
        
        assertEquals(items, result.size());
        
        int count = items - 1;
        for (Person p : result) {
            assertEquals("Foo", p.getName());
            assertEquals(count--, p.getId());
        }
    }
    
    static class Person {
        private long id;
        private String name;
        
        public long getId() {
            return id;
        }
        
        @LuceneField(converter=Converter.LONG)
        public void setId(long id) {
            this.id = id;
        }
        
        public String getName() {
            return name;
        }
        
        @LuceneField
        public void setName(String name) {
            this.name = name;
        }
    }
    
}
