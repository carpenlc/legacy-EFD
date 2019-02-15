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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class DocumentResult {
    
    private final Document document;
    private final Analyzer analyzer;
    
    public DocumentResult(Document document, Analyzer analyzer) {
        this.document = document;
        this.analyzer = analyzer;
    }
    
    public Document getDocument() {
        return document;
    }
    
    public Analyzer getAnalyzer() {
        return analyzer;
    }
}
