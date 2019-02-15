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
package com.solers.delivery.event.listener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.solers.delivery.event.ContentEvent;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class MockEventListener extends FlushingListener {
	
    public Collection<ContentEvent> added = new ArrayList<ContentEvent>();
    
    public MockEventListener(int batchSize, int flushTime) {
        super(batchSize, flushTime);
    }

    @Override
    public void flush(List<ContentEvent> events) {
        added.addAll(events);
    }
}
