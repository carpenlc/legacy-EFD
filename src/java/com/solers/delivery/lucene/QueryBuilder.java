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

import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;

import com.solers.lucene.Converter;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class QueryBuilder {
    
    private final StringBuilder builder;
    
    public QueryBuilder() {
        builder = new StringBuilder();
    }
    
    public QueryBuilder addDate(String field, Date start, Date end) {
        checkAnd();
        builder.append(field+":[");
        if (start == null) {
            builder.append("-");
        } else {
            builder.append(Converter.DATE.convertTo(start));
        }
        builder.append(" TO ");
        if (end == null) {
            builder.append("?");
        } else {
            builder.append(Converter.DATE.convertTo(end));
        }
        builder.append("]");
        return this;
    }
    public QueryBuilder addEquals(String field, String value) {
        checkAnd();
        builder.append(field+":"+value);
        return this;
    }
    
    public QueryBuilder addLike(String field, String value) {
        checkAnd();
        boolean first = true;
        for (String term : value.split(" ")) {
            if (first) {
                first = false;
            } else {
                addAnd();
            }
            builder.append(String.format("%s:%s*", field, term.trim()));
        }
        return this;
    }
    
    public String toString() {
        return builder.toString();
    }
    
    public Query toQuery() {
        return toQuery(new StandardAnalyzer());
    }
    
    public Query toQuery(Analyzer analyer) {
        if (isEmpty()) {
            return new MatchAllDocsQuery();
        }
        
        QueryParser qp = new QueryParser("id", analyer);
        qp.setDateResolution(DateTools.Resolution.MINUTE);
        try {
            return qp.parse(toString());
        } catch (ParseException ex) {
            throw new RuntimeException("Could not parse query", ex);
        }
    }
    
    private boolean isEmpty() {
        return builder.length() == 0;
    }
    
    private void addAnd() {
        builder.append(" AND ");
    }
    
    private void checkAnd() {
        if (!isEmpty()) {
            addAnd();
        }
    }
}
