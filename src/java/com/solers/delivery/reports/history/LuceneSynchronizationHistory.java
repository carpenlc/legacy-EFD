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
package com.solers.delivery.reports.history;

import java.io.IOException;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;

import com.solers.delivery.lucene.LuceneHelper;
import com.solers.delivery.lucene.QueryBuilder;
import com.solers.lucene.ObjectCreator;
import com.solers.lucene.PaginatedHitsList;
import com.solers.lucene.PathAnalyzer;
import com.solers.util.PaginatedList;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class LuceneSynchronizationHistory implements SynchronizationHistory {

    private static final Logger log = Logger.getLogger(LuceneSynchronizationHistory.class);
    
    private final LuceneHelper helper;
    
    public LuceneSynchronizationHistory(LuceneHelper helper) {
        this.helper = helper;
    }

    @Override
    public Synchronization getSynchronization(Long contentSetId, String id) {
        Directory index = helper.getIndex(contentSetId);
        if (!helper.indexExists(index)) {
            return null;
        }
        
        Searcher s = null;
        QueryBuilder query = new QueryBuilder().addEquals("id", id);
        try {
            s = new IndexSearcher(index);
            TopDocs result = s.search(query.toQuery(new KeywordAnalyzer()), null, 1);
            if (result.totalHits > 0) {
                Document doc = s.doc(result.scoreDocs[0].doc);
                return ObjectCreator.create(Synchronization.class, doc);
            }
        } catch (IOException ex) {
            log.error("An error occurred", ex);
        } finally {
            LuceneHelper.close(s);
        }
        return null;
    }

    @Override
    public PaginatedList<ReportDetail> getSynchronizationDetails(Long contentSetId, String synchronizationId, String action, String path, int pageSize) {
        Directory index = helper.getIndex(contentSetId, synchronizationId);
        if (!helper.indexExists(index)) {
            return PaginatedList.emptyList();
        }
        
        QueryBuilder query = new QueryBuilder();
        
        PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper(new StandardAnalyzer());
        
        if (StringUtils.isNotEmpty(action)) {
            query.addEquals("action", action);
        }
        
        if (StringUtils.isNotEmpty(path)) {
            query.addLike("path", path);
            analyzer.addAnalyzer("path", new PathAnalyzer());
        }
        
        return search(query.toQuery(analyzer), index, ReportDetail.class, pageSize);
    }

    @Override
    public PaginatedList<Synchronization> getSynchronizationsAfter(Long contentSetId, Date endTime, boolean showEmptyRecords, int pageSize) {
        Directory index = helper.getIndex(contentSetId);
        if (!helper.indexExists(index)) {
            return PaginatedList.emptyList();
        }
        
        QueryBuilder query = new QueryBuilder();
        query.addDate("endDate", endTime, null);
        if (!showEmptyRecords) {
            query.addEquals("empty", "false");
        }
        
        return search(query.toQuery(), index, Synchronization.class, pageSize);
    }

    @Override
    public PaginatedList<Synchronization> getSynchronizations(Long contentSetId, Date startTime, Date endTime, boolean showEmptyRecords, int pageSize) {
        Directory index = helper.getIndex(contentSetId);
        if (!helper.indexExists(index)) {
            return PaginatedList.emptyList();
        }
        
        QueryBuilder query = new QueryBuilder();
        query.addDate("timeStamp", startTime, endTime);
        if (!showEmptyRecords) {
            query.addEquals("empty", "false");
        }
        
        return search(query.toQuery(), index, Synchronization.class, pageSize);
    }
    
    private <T> PaginatedList<T> search(Query q, Directory index, Class<T> clazz, int pageSize) {
        PaginatedHitsList<T> result = new PaginatedHitsList<T>(pageSize, index, q, clazz);
        result.initialize();
        return result;
    }
}
