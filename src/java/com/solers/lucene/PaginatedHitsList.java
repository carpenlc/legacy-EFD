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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.HitCollector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;

import com.solers.util.PaginatedList;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class PaginatedHitsList <T> extends PaginatedList<T> {

    protected static final Logger log = Logger.getLogger(PaginatedHitsList.class);
    
    private final Directory index;
    private final Query query;
    protected final Class<T> clazz;
    
    public PaginatedHitsList(int pageSize, Directory index, Query query, Class<T> clazz) {
        super(pageSize);
        this.index = index;
        this.query = query;
        this.clazz = clazz;
    }

    @Override
    protected List<T> getSubList(int fromIndex, int toIndex) {
        int size = size();
        int end = toIndex > size ? size : toIndex;
        int descendingStart = size - fromIndex;
        int descendingEnd = size - end;
        
        if (descendingStart < 0) {
            descendingStart = 0;
        }
        
        List<T> result = new ArrayList<T>();
        IndexReader r = null;
        Searcher s = null;
        try {
            r = IndexReader.open(index);
            s = new IndexSearcher(r);
            s.search(query, new ResultGatherer(r, descendingStart, descendingEnd, result));
        } catch (IOException ex) {
            log.error(String.format("Error occurred getting page from %d to %d", descendingStart, descendingEnd), ex);
        } finally {
            close(s);
            close(r);
        }
        
        Collections.reverse(result);
       
        return result;
    }

    @Override
    public void initialize() {
        Searcher s = null;
        try {
            s = new IndexSearcher(index);
            TopDocs hits = s.search(query, null, 1);
            initialized(hits.totalHits);
        } catch (IOException ex) {
            log.error("An error occurred", ex);
            initialized(0);
        } finally {
            close(s);
        }
    }
    
    private void close(Searchable s) {
        if (s != null) {
            try {
                s.close();
            } catch (IOException ignore) {
                log.error("Error closing", ignore);
            }
        }
    }
    
    private void close(IndexReader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException ignore) {
                log.error("Error closing", ignore);
            }
        }
    }
    
    class ResultGatherer extends HitCollector {

        private final IndexReader reader;
        private final long start;
        private final long end;
        private final List<T> result;
        
        private long count;
        
        /**
         * @param reader
         * @param start
         * @param end
         * @param result
         * @param count
         */
        public ResultGatherer(IndexReader reader, long start, long end, List<T> result) {
            this.reader = reader;
            this.start = start;
            this.end = end;
            this.result = result;
            this.count = 0;
        }
        
        @Override
        public void collect(int docNum, float score) {
            if (count < start && count >= end) {
                try {
                    result.add(ObjectCreator.create(clazz, reader.document(docNum)));
                } catch (IOException ex) {
                    log.error("An error occurred gathering hit "+count, ex);
                }
            }
            count = count + 1;
        }
    }
}
