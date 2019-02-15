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
package com.solers.delivery.event.cleanup;

import java.util.Calendar;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;

import com.solers.delivery.BaseLuceneTestCase;
import com.solers.lucene.Converter;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class LuceneEventCleanupTaskTestCase extends BaseLuceneTestCase {
    
    public void testCleanupSynchronizations() throws Exception {
        long contentSetId = 1l;
        Directory index = helper.getIndex(contentSetId);
        
        helper.addDocument(index, newDocument(7));
        helper.addDocument(index, newDocument(10));
        helper.addDocument(index, newDocument(17));
        helper.addDocument(index, newDocument(20));
        
        IndexReader reader = IndexReader.open(index);
        try {
            assertEquals(4, reader.numDocs());
        } finally {
            reader.close();
        }
        
        Runnable r = new LuceneEventCleanupTask(helper, 100, 15);
        r.run();
        
        reader = IndexReader.open(index);
        try {
            assertEquals(2, reader.numDocs());
            assertEquals(newDate(7), reader.document(0).get("timeStamp"));
            assertEquals(newDate(10), reader.document(1).get("timeStamp"));
        } finally {
            reader.close();
        }
    }
    
    public void testCleanupSynchronizationDetails() throws Exception {
        long contentSetId = 1l;
        
        String [] syncKeys = new String [] { "sync1", "sync2", "sync3" };
        
        for (String key : syncKeys) {
            Directory index = helper.getIndex(contentSetId, key);
            helper.addDocument(index, newDocument(10));
            helper.addDocument(index, newDocument(17));
            helper.addDocument(index, newDocument(20));
            IndexReader reader = IndexReader.open(index);
            try {
                assertEquals(3, reader.numDocs());
            } finally {
                reader.close();
            }
        }
        
        
        Runnable r = new LuceneEventCleanupTask(helper, 15, 100);
        r.run();
        
        for (String key : syncKeys) {
            Directory index = helper.getIndex(contentSetId, key);
            IndexReader reader = IndexReader.open(index);
            try {
                assertEquals(1, reader.numDocs());
                Document doc = reader.document(0);
                assertEquals(newDate(10), doc.get("timeStamp"));
            } finally {
                reader.close();
            }
        }
    }
    
    private Document newDocument(int days) {
        Document document = new Document();
        
        String value = newDate(days);
        
        document.add(new Field("timeStamp", value, Field.Store.YES, Field.Index.NOT_ANALYZED));
        
        return document;
    }
    
    private String newDate(int days) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR) - days);
        return Converter.DATE.convertTo(c.getTime());
    }
}
