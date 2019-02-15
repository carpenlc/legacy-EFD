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
package com.solers.delivery.transport.http.server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mortbay.jetty.Handler;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.solers.delivery.content.status.SupplierProgress;
import com.solers.delivery.content.supplier.ContentSetMapper;
import com.solers.delivery.content.supplier.MockSupplierContentSetManager;
import com.solers.delivery.domain.ContentSet;
import com.solers.delivery.event.ContentEvent;
import com.solers.delivery.event.EventManager;
import com.solers.delivery.event.MockEventManager;
import com.solers.delivery.event.SynchronizationEvent;
import com.solers.delivery.event.ContentEvent.ContentEventResult;
import com.solers.delivery.transport.gbs.GBSService;
import com.solers.delivery.transport.http.HTTPHeaders;
import com.solers.delivery.transport.http.monitor.TransferMonitor;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class GbsHandlerTestCase {
    
    private TestHandler handler;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private TransferMonitor monitor;
    private MockEventManager eventManager;
    private MockSupplierContentSetManager supplierManager;
    private MockGBSService gbs;
    
    @Before
    public void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        supplierManager = new MockSupplierContentSetManager();
        eventManager = new MockEventManager();
        monitor = new TransferMonitor();
        monitor.setSupplierManager(supplierManager);
        gbs = new MockGBSService();
        handler = new TestHandler(true, eventManager, supplierManager, gbs);
        handler.setTransferMonitor(monitor);
    }
    
    @Test
    public void testHandle() throws Exception {
        request.addHeader(HTTPHeaders.SYNC_ID.headerName(), "xxx");
        request.addHeader(HTTPHeaders.GBS_RETRIEVAL.headerName(), "consumerName");
        request.addHeader(HTTPHeaders.CONTENT_SET_NAME.headerName(), "supplierName");
        request.setPathInfo("pathInfo");
        
        SynchronizationEvent syncEvent = new SynchronizationEvent("xxx", 1L);
        monitor.startSupplier(syncEvent, new SupplierProgress(7, 1));
        
        supplierManager.setFile(createFile("testHandle"));
        supplierManager.setId(1L);
        
        SupplierProgress p = monitor.getSupplierProgress("1-xxx");
        Assert.assertEquals(0D, p.getPercentComplete(), 0.0D);
        Assert.assertEquals(0, p.getCompletedBytes());
        Assert.assertEquals(0, p.getCompletedItems());
        
        handler.handle("/", request, response, Handler.REQUEST);
        
        Assert.assertEquals("consumerName", gbs.consumerContentSet);
        Assert.assertEquals("supplierName", gbs.supplierContentSet);
        Assert.assertEquals("pathInfo", gbs.path);
        Assert.assertEquals("xxx", gbs.syncKey);
        
        ContentEvent event = (ContentEvent) eventManager.getMostRecent();
        Assert.assertEquals("pathInfo", event.getPath());
        Assert.assertEquals(7, event.getBytesManipulated());
        Assert.assertEquals("1-xxx", event.getSynchronizationId());
        Assert.assertEquals(ContentEventResult.SENT_GBS.value(), event.getResult());
        
        p = monitor.getSupplierProgress("1-xxx");
        Assert.assertEquals(100D, p.getPercentComplete(), 0.0D);
        Assert.assertEquals(7, p.getCompletedBytes());
        Assert.assertEquals(1, p.getCompletedItems());
    }
    
    private File createFile(String name) throws IOException {
        File file = new File(".", name);
        file.createNewFile();
        file.deleteOnExit();
        FileWriter output = new FileWriter(file);
        IOUtils.write("xdiogng", output);
        output.close();
        return file;
    }
    
    private static class TestHandler extends GbsHandler {
        boolean handled = false;
        
        private TestHandler(boolean enabled, EventManager eventMgr, ContentSetMapper cm, GBSService gbsSupplier) {
            super(enabled, eventMgr, cm, gbsSupplier);
        }
        
        @Override
        protected void setRequestHandled(HttpServletRequest request, HttpServletResponse response) {
           handled = true;
        }
        
    }
    
    private static class MockGBSService implements GBSService {
        
        String consumerContentSet;
        String syncKey;
        String supplierContentSet;
        String host;
        File file;
        String path;

        @Override
        public void addContentSet(ContentSet contentSet) {
            
        }

        @Override
        public void addFile(String consumerContentSet, String syncKey, String supplierContentSet, String host, File file, String path) {
            this.consumerContentSet = consumerContentSet;
            this.syncKey = syncKey;
            this.supplierContentSet = supplierContentSet;
            this.host = host;
            this.file = file;
            this.path = path;
        }

        @Override
        public void removeContentSet(Long id) {
            
        }

        @Override
        public void startContentSet(Long id) {
            
        }

        @Override
        public void stopContentSet(Long id) {
            
        }

        @Override
        public void run() {
            
        }
        
    }
}
