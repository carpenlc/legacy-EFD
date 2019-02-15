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

import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class SingleFieldSelector implements FieldSelector {
    
    private static final long serialVersionUID = 1l;
    
    private final String field;
    private final FieldSelectorResult result;
    
    public SingleFieldSelector(String field, FieldSelectorResult result) {
        this.field = field;
        this.result = result;
    }
    @Override
    public FieldSelectorResult accept(String field) {
        if (this.field.equals(field)) {
            return result;
        }
        return FieldSelectorResult.NO_LOAD;
    }
    
    
}
