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

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import com.solers.lucene.DocumentCreator;
import com.solers.lucene.LuceneField;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class LuceneHelperTestCase extends TestCase {
    
    public void testAddDocument() throws IOException {
        LuceneHelper helper = new LuceneHelper(null);
        Directory d = new RAMDirectory();
        helper.addDocument(d, DocumentCreator.create(new Test()));
        
        IndexReader reader = IndexReader.open(d);
        assertEquals(1, reader.numDocs());
        
        Document doc = reader.document(0);
        assertEquals(1, doc.getFields().size());
        assertEquals("name", doc.get("name"));
    }
    
    public static class Test {
        
        @LuceneField
        public String getName() {
            return "name";
        }
    }
    
}
