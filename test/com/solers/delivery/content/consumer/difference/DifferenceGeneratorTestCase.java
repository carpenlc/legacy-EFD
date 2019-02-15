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
package com.solers.delivery.content.consumer.difference;

import java.io.File;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;

import com.solers.delivery.content.status.ConsumerProgress;
import com.solers.delivery.domain.ConsumerContentSet;
import com.solers.delivery.domain.ContentSet;
import com.solers.delivery.inventory.InventoryFactory;
import com.solers.delivery.inventory.comparer.NodeListener;
import com.solers.delivery.inventory.fs.FileSystemInventoryProvider;
import com.solers.delivery.inventory.node.Node;
import com.solers.delivery.inventory.plugin.InventoryPlugin;
import com.solers.util.unit.FileSizeUnit;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class DifferenceGeneratorTestCase extends TestCase {
    
    private File root;
    
    public void setUp() throws Exception {
        InventoryPlugin.register(FileSystemInventoryProvider.class);
        root = new File(".", "DifferenceGeneratorTestCase");
        root.mkdir();
        root.deleteOnExit();
    }
    
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(root);
    }
    
    public void testPrescan() throws Exception {
        File path = new File(root, "content");
        path.mkdir();
        
        File inv = new File(root, "inventory");
        inv.mkdir();
        
        InventoryFactory.setSiteDirectory(inv.getCanonicalPath());
        
        ConsumerContentSet consumer = new ConsumerContentSet();
        consumer.setPath(path.getCanonicalPath());
        consumer.setName("consumer");
        consumer.setMaxFileSize(100);
        consumer.setMaxFileSizeUnit(FileSizeUnit.GIGABYTES);
        
        createInventory(root, consumer);
        
        DifferenceGenerator generator = new DifferenceGenerator(consumer, null);
        ConsumerProgress progress = new ConsumerProgress();
        generator.prescan(progress);
        
        Assert.assertEquals(3, progress.getTotalItems());
        Assert.assertEquals(5, progress.getTotalNewBytes());
        Assert.assertEquals(1, progress.getTotalNewFiles());
    }
    
    private void createInventory(File root, ConsumerContentSet consumer) throws Exception {
        File dir = new File(root, "supplierPath");
        dir.mkdirs();
        
        File sub = new File(dir, "sub");
        sub.mkdir();
        
        File file = new File(dir, "contents.txt");
        file.createNewFile();
        FileUtils.writeStringToFile(file, "xxxxx");
        
        ContentSet supplier = new ContentSet();
        supplier.setName("supplier");
        supplier.setPath(dir.getCanonicalPath());
        InventoryFactory.createInventory(supplier);
        
        FileUtils.copyDirectory(InventoryFactory.getOpenLocation(supplier), InventoryFactory.getOpenLocation(consumer));
    }
    
    public void testOnStopCalledIfExceptionThrown() {
        MockEventHandler handler = new MockEventHandler();
        ConsumerContentSet config = new ConsumerContentSet();
        config.setId(1l);
        config.setPath("xx");
        DifferenceGenerator generator = new RuntimeExceptionDifferenceGenerator(config, handler);

        try {
            generator.prescan(new ConsumerProgress());
        } catch (Exception ignore) {
            
        }

        assertFalse(handler.stopCalled);
        generator.run();
        assertTrue(handler.stopCalled);
    }

    class RuntimeExceptionDifferenceGenerator extends DifferenceGenerator {

        public RuntimeExceptionDifferenceGenerator(ConsumerContentSet contentSet, NodeListener listener) {
            super(contentSet, listener);
        }

        protected ConsumerProgress prescan() {
            return null;
        }

        protected void runCompare(NodeListener handler) {
            throw new RuntimeException();
        }

    }

    class MockEventHandler implements NodeListener {

        boolean stopCalled = false;

        /**
         * @see com.solers.delivery.inventory.comparer.NodeListener#onStop()
         */
        @Override
        public void onStop() {
            stopCalled = true;
        }

        /**
         * @see com.solers.delivery.inventory.comparer.NodeListener#onAdd(com.solers.delivery.inventory.node.Node)
         */
        @Override
        public void onAdd(Node node) {}

        /**
         * @see com.solers.delivery.inventory.comparer.NodeListener#onRemove(com.solers.delivery.inventory.node.Node)
         */
        @Override
        public void onRemove(Node node) {}

        /**
         * @see com.solers.delivery.inventory.comparer.NodeListener#onStart()
         */
        @Override
        public void onStart() {}

        /**
         * @see com.solers.delivery.inventory.comparer.NodeListener#onUpdate(com.solers.delivery.inventory.node.Node)
         */
        @Override
        public void onUpdate(Node node) {}
    }
}
