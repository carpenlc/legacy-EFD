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
package com.solers.delivery.content.consumer;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import com.solers.delivery.content.consumer.difference.ContentDifference;
import com.solers.delivery.content.consumer.difference.ContentDifferenceActions;
import com.solers.delivery.content.consumer.difference.DifferenceQueuer;
import com.solers.delivery.inventory.node.AbstractNode;
import com.solers.delivery.inventory.node.Node;
import com.solers.util.NamedThreadFactory;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class DifferenceQueuerTestCase extends TestCase {

    BlockingQueue<ContentDifference> refreshes;
    BlockingQueue<ContentDifference> deletes;
    DifferenceQueuer queuer;

    @Before
    public void setUp() {
        refreshes = new ArrayBlockingQueue<ContentDifference>(10);
	    deletes = new ArrayBlockingQueue<ContentDifference>(10);
	    queuer = new DifferenceQueuer(refreshes, deletes, new Long(1), 2);
    }

    @Test
    public void testQueuer() throws Exception {

    	ExecutorService executor = Executors.newFixedThreadPool(2,
    		new NamedThreadFactory());
    
    	List<String> refreshMessages = new ArrayList<String>();
    	List<String> deleteMessages = new ArrayList<String>();
    
    	Future<?> a = executor.submit(new Consumer(refreshes, refreshMessages));
    	Future<?> b = executor.submit(new Consumer(deletes, deleteMessages));
    
    	queuer.onAdd(new MockNode("add1"));
    	queuer.onRemove(new MockNode("remove1"));
    	queuer.onUpdate(new MockNode("update1"));
    
    	queuer.onStop();
    
    	a.get();
    	b.get();
    
    	assertTrue(refreshMessages.contains("add1"));
    	assertTrue(refreshMessages.contains("update1"));
    	assertTrue(deleteMessages.contains("remove1"));
    	
    	// There should be an extra "Done" action here since we specified 2 requesters in the constructor
    	assertEquals(ContentDifferenceActions.DONE, refreshes.take().getAction());
    }

    class MockNode extends AbstractNode {

    	String name;
    
    	MockNode(String name) {
    	    this.name = name;
    	}
    
    	/**
    	 * @see com.solers.delivery.inventory.node.Node#getChildren()
    	 */
    	@Override
    	public List<Node> getChildren() {
    	    return null;
    	}
    	
    	/**
    	 * @see com.solers.delivery.inventory.node.Node#getName()
    	 */
    	@Override
    	public String getName() {
    	    return name;
    	}
    
    	/**
    	 * @see com.solers.delivery.inventory.node.Node#getPath()
    	 */
    	@Override
    	public String getPath() {
    	    return getName();
    	}
    
    	/**
    	 * @see com.solers.delivery.inventory.node.Node#getSize()
    	 */
    	@Override
    	public long getSize() {
    	    return 0;
    	}
    
    	/**
    	 * @see com.solers.delivery.inventory.node.Node#getTimestamp()
    	 */
    	@Override
    	public long getTimestamp() {
    	    return 0;
    	}
    
    	/**
    	 * @see com.solers.delivery.inventory.node.Node#isDirectory()
    	 */
    	@Override
    	public boolean isDirectory() {
    	    return false;
    	}
    	
    	@Override
    	public Node getParent() {
    	    return null;
    	}
    	
    	@Override
    	public InputStream openStream() {
    	    return null;
    	}

    }

    class Consumer implements Runnable {

    	BlockingQueue<ContentDifference> queue;
    	List<String> messages;
    
    	Consumer(BlockingQueue<ContentDifference> queue, List<String> messages) {
    	    this.queue = queue;
    	    this.messages = messages;
    	}
    
    	public void run() {
    	    try {
    		for (;;) {
    		    ContentDifference d = queue.take();
    		    if (d.getAction() == ContentDifferenceActions.DONE) {
    			Thread.currentThread().interrupt();
    		    } else {
    			messages.add(d.getPath());
    		    }
    		}
    	    } catch (InterruptedException ex) {
    
    	    } catch (Exception ex) {
    		throw new RuntimeException(ex);
    	    }
    	}
    }
}
