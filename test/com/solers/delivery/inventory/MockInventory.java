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
package com.solers.delivery.inventory;

import java.util.Collections;
import java.util.Set;

import com.solers.delivery.inventory.node.Node;
import com.solers.util.Filter;

public class MockInventory implements Inventory {
    private final Node node;
    
    public MockInventory(Node node) {
        this.node = node;
    }
    
    @Override
    public void close() {
    }

    @Override
    public Object getProperty(String name) {
        return null;
    }

    @Override
    public Set<String> properties() {
        return Collections.emptySet();
    }

    @Override
    public Node read(String path) {
        return null;
    }

    @Override
    public Node root() {
        return node;
    }
    
    @Override
    public String getRootName() {
        return node.getName();
    }

    @Override
    public void useFilter(Filter<Node> filter) {
    }
}
