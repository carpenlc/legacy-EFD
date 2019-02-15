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
package com.solers.delivery.reports.metrics;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

import com.solers.delivery.XPathAssert;
import com.solers.delivery.content.ContentService;
import com.solers.delivery.content.ContentServiceImpl;
import com.solers.delivery.daos.MockDAOFactory;
import com.solers.delivery.domain.ConsumerContentSet;
import com.solers.delivery.domain.ContentSet;
import com.solers.delivery.reports.history.MockSynchronizationHistory;
import com.solers.delivery.reports.history.Synchronization;
import com.solers.delivery.rest.RestfulService;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class MetricsTaskTestCase {
    
    private File dataDirectory;
    private MockDAOFactory factory;
    private MockSynchronizationHistory history;
    private ContentService contentService;
    
    @Before
    public void setUp() {
        dataDirectory = new File(getClass().getName());
        factory = new MockDAOFactory();
        contentService = new ContentServiceImpl(factory, null, null);
        history = new MockSynchronizationHistory();
    }
    
    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(dataDirectory);
    }
    
    @Test
    public void testRun() throws Exception {
        factory.getContentSetDAO().makePersistent(new ContentSet());
        factory.getContentSetDAO().makePersistent(new ConsumerContentSet());
        RestfulService metricsService = new RestfulService(Protocol.HTTP, "", 0) {
            @Override
            public Response put(String data, MediaType mediaType, Object... uriParts) {
                try {
                    XPathAssert.assertXPathNodeValue(data, "//metrics/consumer/list/synchronization/adds/text()", "4");
                    XPathAssert.assertXPathNodeValue(data, "//metrics/consumer/list/synchronization/bytesAdded/text()", "1");
                    XPathAssert.assertXPathNodeValue(data, "//metrics/consumer/list/synchronization/bytesDeleted/text()", "3");
                    XPathAssert.assertXPathNodeValue(data, "//metrics/consumer/list/synchronization/bytesFailed/text()", "4");
                    XPathAssert.assertXPathNodeValue(data, "//metrics/consumer/list/synchronization/bytesUpdated/text()", "2");
                    XPathAssert.assertXPathNodeValue(data, "//metrics/consumer/list/synchronization/deletes/text()", "6");
                    XPathAssert.assertXPathNodeValue(data, "//metrics/consumer/list/synchronization/elapsedTime/text()", "100");
                    XPathAssert.assertXPathNodeValue(data, "//metrics/consumer/list/synchronization/failures/text()", "1");
                    XPathAssert.assertXPathNodeValue(data, "//metrics/consumer/list/synchronization/host/text()", "127.0.0.1");
                    XPathAssert.assertXPathNodeValue(data, "//metrics/consumer/list/synchronization/timestamp/text()", String.valueOf(MockSynchronizationHistory.DATE.getTime()));
                    XPathAssert.assertXPathNodeValue(data, "//metrics/consumer/list/synchronization/updates/text()", "5");
                    
                    XPathAssert.assertXPathNodeValue(data, "//metrics/supplier/list/synchronization/adds/text()", "4");
                    XPathAssert.assertXPathNodeValue(data, "//metrics/supplier/list/synchronization/bytesAdded/text()", "1");
                    XPathAssert.assertXPathNodeValue(data, "//metrics/supplier/list/synchronization/bytesDeleted/text()", "3");
                    XPathAssert.assertXPathNodeValue(data, "//metrics/supplier/list/synchronization/bytesFailed/text()", "4");
                    XPathAssert.assertXPathNodeValue(data, "//metrics/supplier/list/synchronization/bytesUpdated/text()", "2");
                    XPathAssert.assertXPathNodeValue(data, "//metrics/supplier/list/synchronization/deletes/text()", "6");
                    XPathAssert.assertXPathNodeValue(data, "//metrics/supplier/list/synchronization/elapsedTime/text()", "100");
                    XPathAssert.assertXPathNodeValue(data, "//metrics/supplier/list/synchronization/failures/text()", "1");
                    XPathAssert.assertXPathNodeValue(data, "//metrics/supplier/list/synchronization/host/text()", "127.0.0.1");
                    XPathAssert.assertXPathNodeValue(data, "//metrics/supplier/list/synchronization/timestamp/text()", String.valueOf(MockSynchronizationHistory.DATE.getTime()));
                    XPathAssert.assertXPathNodeValue(data, "//metrics/supplier/list/synchronization/updates/text()", "5");
                } catch (Exception e) {
                    Assert.fail(e.getMessage());
                    e.printStackTrace();
                }
                Response result = new Response(new Request());
                result.setStatus(Status.SUCCESS_OK);
                return result;
            }
        };
        
        MetricsTask task = new MetricsTask(dataDirectory, contentService, history, metricsService, true);
        task.run();
        checkRuntimeExists();
    }
    
    @Test
    public void testRunWithNoResults() throws Exception {
        RestfulService metricsService = new RestfulService(Protocol.HTTP, "", 0) {
            @Override
            public Response put(String data, MediaType mediaType, Object... uriParts) {
                Assert.fail(); // No data should be sent
                return null;
            }
        };
        
        MetricsTask task = new MetricsTask(dataDirectory, contentService, history, metricsService, true);
        task.run();
        
        factory.getContentSetDAO().makePersistent(new ContentSet());
        history.syncData = new ArrayList<Synchronization>();
        task.run();
        
        checkRuntimeExists();
    }
    
    @Test
    public void testRunDisabled() {
        RestfulService metricsService = new RestfulService(Protocol.HTTP, "", 0) {
            @Override
            public Response put(String data, MediaType mediaType, Object... uriParts) {
                Assert.fail(); // No data should be sent
                return null;
            }
        };
        
        MetricsTask task = new MetricsTask(dataDirectory, contentService, history, metricsService, false);
        
        factory.getContentSetDAO().makePersistent(new ContentSet());
        task.run();
        
        checkRuntimeEmpty();
    }
    
    @Test
    public void testRuntimeOnlyUpdatedWithSuccessfulSend() {
        factory.getContentSetDAO().makePersistent(new ContentSet());
        RestfulService metricsService = new RestfulService(Protocol.HTTP, "", 0) {
            @Override
            public Response put(String data, MediaType mediaType, Object... uriParts) {
                Response result = new Response(new Request());
                result.setStatus(Status.CONNECTOR_ERROR_COMMUNICATION);
                return result;
            }
        };
        
        MetricsTask task = new MetricsTask(dataDirectory, contentService, history, metricsService, true);
        task.run();
        checkRuntimeEmpty();
    }
    
    private void checkRuntimeExists() throws Exception {
        File file = new File(dataDirectory, "lastRuntime");
        long time = Long.parseLong(FileUtils.readFileToString(file));
        Assert.assertTrue(time > 0);
        Assert.assertTrue(System.currentTimeMillis() >= time);
    }
    
    private void checkRuntimeEmpty() {
        File file = new File(dataDirectory, "lastRuntime");
        Assert.assertFalse(file.exists());
    }
}
