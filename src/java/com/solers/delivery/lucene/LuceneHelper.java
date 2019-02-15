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

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.MergeScheduler;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.solers.lucene.DocumentResult;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class LuceneHelper {
    
    private static final Logger log = Logger.getLogger(LuceneHelper.class);
    
    private final File siteDirectory;

    public LuceneHelper(File siteDirectory) {
        this.siteDirectory = siteDirectory;
    }
    
    public void removeIndex(long contentSetId, String syncId) {
        File dir = new File(getContentSetDirectory(contentSetId), syncId);
        try {
            FileUtils.cleanDirectory(dir);
            FileUtils.deleteDirectory(dir);
        } catch (IOException ex) {
            log.error("Error removing index: "+dir, ex);
        }
    }
    
    public void addDocument(Directory index, DocumentResult r) throws IOException {
        addDocument(index, r.getDocument(), r.getAnalyzer());
    }
    
    public void addDocument(Directory index, Document document) throws IOException {
        addDocument(index, document, new StandardAnalyzer());
    }
    
    public void addDocument(Directory index, Document document, Analyzer analyzer) throws IOException {
        addDocument(index, document, analyzer, null);
    }
    
    public void addDocument(Directory index, Document document, Analyzer analyzer, MergeScheduler scheduler) throws IOException {
        IndexWriter writer = null;
        try {
            writer = new IndexWriter(index, analyzer, IndexWriter.MaxFieldLength.UNLIMITED);
            if (scheduler != null) {
                writer.setMergeScheduler(scheduler);
            }
            writer.addDocument(document);
        } finally {
            close(writer);
        }
    }
    
    public boolean indexExists(Directory index) {
        try {
            return IndexReader.indexExists(index);
        } catch (IOException ex) {
            throw new RuntimeException("Could not open index", ex);
        }
    }
    
    public Directory getIndex(long contentSetId) {
        return getIndex(new File(getContentSetDirectory(contentSetId), "synchronizations"));
    }
    
    public Directory getIndex(long contentSetId, String syncId) {
        if (syncId == null) {
            return getIndex(contentSetId);
        }
        return getIndex(new File(getContentSetDirectory(contentSetId), syncId));
    }
    
    public static Directory getIndex(File index) {
        try {
            return FSDirectory.getDirectory(index);
        } catch (IOException ex) {
            throw new RuntimeException("Could not open index", ex);
        }
    }
    
    public void optimizeIndex(long contentSetId) {
        optimizeIndex(getIndex(contentSetId));
    }
    
    public void optimizeIndex(long contentSetId, String syncId) {
        optimizeIndex(getIndex(contentSetId, syncId));
    }
    
    public void optimizeIndex(Directory index) {
        IndexWriter writer = null;
        try {
            writer = new IndexWriter(index, new StandardAnalyzer(), IndexWriter.MaxFieldLength.UNLIMITED);
            writer.optimize();
        } catch (IOException ex) {
            log.error("An error occured", ex);
        } finally {
            close(writer);
        }
    }
    
    public File getContentSetDirectory(long contentSetId) {
        File dir = new File(getEventDirectory(), String.valueOf(contentSetId));
        dir.mkdirs();
        return dir;
    }
    
    public File getEventDirectory() {
        File dir = new File(siteDirectory, "events");
        dir.mkdirs();
        return dir;
    }
    
    public static void close(Searchable s) {
        if (s != null) {
            try {
                s.close();
            } catch (IOException ignore) {
                log.error("Error closing", ignore);
            }
        }
    }
    
    public static void close(IndexWriter writer) {
        if (writer != null) {
            try {
                writer.commit();
                writer.close();
            } catch (IOException ignore) {
                log.error("Error closing", ignore);
            }
        }
    }
    
    public static void close(IndexReader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException ignore) {
                log.error("Error closing", ignore);
            }
        }
    }
}
