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
package com.solers.delivery.inventory.diff;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

import junit.framework.TestCase;

import com.solers.delivery.inventory.DifferenceInventory;
import com.solers.delivery.inventory.Inventory;
import com.solers.delivery.inventory.MockInventory;
import com.solers.delivery.inventory.comparer.Comparer;
import com.solers.delivery.inventory.comparer.NodeListener;
import com.solers.delivery.inventory.comparer.UnorderedInventoryComparer;
import com.solers.delivery.inventory.index.ReaderFactory;
import com.solers.delivery.inventory.index.diff.DifferenceFileReader;
import com.solers.delivery.inventory.index.diff.DifferenceNode;
import com.solers.delivery.inventory.index.diff.MergedNode;
import com.solers.delivery.inventory.node.Node;

public class DiffTestCase extends TestCase {
    private File diff_1_to_2;
    private File diff_2_to_3;
    private File index_1;
    private File index_2;
    private File index_3;
    
    private NodeListener noMutationsHandler = new NodeListener() {
        @Override
        public void onAdd(Node node) {
            fail();
        }

        @Override
        public void onRemove(Node node) {
            fail();
        }

        @Override
        public void onUpdate(Node node) {
            fail();
        }

        @Override
        public void onStart() { }
        @Override
        public void onStop() { }
    };
    
    public DiffTestCase() {
        try {
            diff_1_to_2 = find("/1_2.diff");
            diff_2_to_3 = find("/2_3.diff");
            index_1 = find("/diff_1.index");
            index_2 = find("/diff_2.index");
            index_3 = find("/diff_3.index");
        } catch (URISyntaxException use) {
            fail();
        }
    }
    
    private File find(String name) throws URISyntaxException {
        return new File(DiffTestCase.class.getResource(name).toURI());
    }
    
    public void testDiff1_2() {
        DifferenceFileReader dfr = new DifferenceFileReader(diff_1_to_2);
        try {
            Node root = dfr.read("diff");
            assertTrue(root != null);
            assertTrue(root.getChildren().size() == 2);
            assertTrue(root.getChild("2.txt") != null);
            assertTrue(root.getChild("2").getChildren().get(0).getChildren().size() == 4);
        } finally {
            dfr.close();
        }
    }
    
    public void testDiff2_3() {
        DifferenceFileReader dfr = new DifferenceFileReader(diff_2_to_3);
        try {
            Node root = dfr.read("diff");
            assertTrue(root != null);
            DifferenceNode dn = (DifferenceNode) root.getChild("1");
            assertTrue(dn.isRemoved());
            dn = (DifferenceNode) root.getChild("1.txt");
            assertTrue(dn.isChanged());
            assertTrue(dn.getSize() == 77l);
            dn = (DifferenceNode) root.getChild("3");
            assertTrue(dn.isChanged());
            assertTrue(dn.getChildren().size() == 2);
        } finally {
            dfr.close();
        }
    }
    
    public void testMerge1_2() {
        try {
            Inventory desired = ReaderFactory.newInstance(index_2);
            Inventory merged = new MockInventory(
                new MergedNode(ReaderFactory.newInstance(index_1).root(), new DifferenceFileReader(diff_1_to_2))
            );
            try {
                Comparer c = new UnorderedInventoryComparer(false, false);
                c.compare(desired, merged, noMutationsHandler);
            } finally {
                desired.close();
                merged.close();
            }
        } catch (IOException ioe) {
            fail();
        }
    }
    
    public void testMerge1_3() {
        try {
            Inventory desired = ReaderFactory.newInstance(index_3);
            Inventory merged = new MockInventory(
                new MergedNode(ReaderFactory.newInstance(index_1).root(),
                Arrays.asList(new DifferenceInventory[] {
                    new DifferenceFileReader(diff_1_to_2),
                    new DifferenceFileReader(diff_2_to_3)
                })
            ));
            try {
                Comparer c = new UnorderedInventoryComparer(false, false);
                c.compare(desired, merged, noMutationsHandler);
            } finally {
                desired.close();
                merged.close();
            }
        } catch (IOException ioe) {
            fail();
        }        
    }
}
