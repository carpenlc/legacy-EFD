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
package com.solers.delivery.content;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.solers.delivery.domain.ConsumerContentSet;
import com.solers.delivery.domain.FileFilter;
import com.solers.delivery.inventory.node.Node;
import com.solers.util.Filter;
import com.solers.util.unit.FileSizeUnit;

/**
 * A node filter that uses the Consumer Content Set's advanced settings.
 */
public class ConsumerContentFilter implements Filter<Node>, Serializable {

    private static final long serialVersionUID = 1l;

    private final ConsumerContentSet consumer;
    private final Map<FileFilter, Pattern> regexCache = Collections.synchronizedMap(new HashMap<FileFilter, Pattern>());
    private final boolean checkNames;

    public ConsumerContentFilter(ConsumerContentSet consumer) {
        if (consumer == null) throw new IllegalArgumentException("ContentSet cannot be null.");
        this.consumer = consumer;

        this.checkNames = (consumer.getFileFilters() != null && !consumer.getFileFilters().isEmpty());
    }

    public boolean accept(Node node) {
        if (node == null) return false;
        boolean accept = true;
        
        //skip the arithmetic if it's a directory
        if (!node.isDirectory()) accept &= checkSize(node.getSize());
        //don't use &= so we can use short circuiting if checkSize rejected the node
        if (checkNames) accept = accept && checkName(node.getName());
        
        return accept;
    }

    private boolean checkSize(long lengthInBytes) {
        long allowableSize = Math.round(FileSizeUnit.BYTES.convert(consumer.getMaxFileSize(), consumer.getMaxFileSizeUnit()));
        return lengthInBytes <= allowableSize;
    }

    private boolean checkName(String name) {
        boolean result = true;
        List<FileFilter> filters = consumer.getFileFilters();
        if (filters == null || filters.isEmpty()) {
            return true;
        }

        for (FileFilter filter : filters) {
            Pattern p = regexCache.get(filter);
            switch (filter.getPatternType()) {
                case BEGINS:
                    result &= name.startsWith(filter.getPattern());
                    break;
                case ENDS:
                    result &= name.endsWith(filter.getPattern());
                    break;
                case CONTAINS:
                    result &= name.contains(filter.getPattern());
                    break;
                case REGEX:
                    if (p == null) {
                        p = Pattern.compile(filter.getPattern());
                        regexCache.put(filter, p);
                    }
                    result &= p.matcher(name).matches();
                    break;
                default:
                    // should never be here
                    result &= true;
            }
            if (!filter.isInclusive()) result = !result;
            // short circuit
            if (!result)
                break;
        }

        return result;
    }
}
