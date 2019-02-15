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

import static com.solers.delivery.transport.http.HTTPHeaders.CONTENT_SET_NAME;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileWriter;

import org.apache.commons.io.FileUtils;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mortbay.jetty.Handler;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.solers.delivery.content.status.SupplierProgress;
import com.solers.delivery.content.supplier.ContentSetMapper;
import com.solers.delivery.content.supplier.MockSupplierContentSetManager;
import com.solers.delivery.domain.ContentSet;
import com.solers.delivery.event.ContentEvent;
import com.solers.delivery.event.MockEventManager;
import com.solers.delivery.event.SynchronizationEvent;
import com.solers.delivery.event.ContentEvent.ContentEventResult;
import com.solers.delivery.transport.http.HTTPHeaders;
import com.solers.delivery.transport.http.HTTPRangeHeader;
import com.solers.delivery.transport.http.HTTPStatusCodes;
import com.solers.delivery.transport.http.monitor.TransferMonitor;
import com.solers.util.Callback;

public class FileHandlerTest {

    private static final String CONTENT_SET = "test";
    private static final String PATH = "contentSet1/file1";

    private FileHandler handler;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private TransferMonitor monitor;
    private MockEventManager eventManager;
    private static ContentSetMapper mock;

    @BeforeClass
    public static void beforeClass() {
        mock = createMock(ContentSetMapper.class);
    }

    @Before
    public void beforeEachTest() {
        reset(mock);
        eventManager = new MockEventManager();
        monitor = new TransferMonitor();
        monitor.setSupplierManager(new MockSupplierContentSetManager());
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        handler = new FileHandler();
        handler.setCm(mock);
        handler.setEventMgr(eventManager);
        handler.setTransferMonitor(monitor);

        request.setPathInfo(PATH);
        request.addHeader(CONTENT_SET_NAME.headerName(), CONTENT_SET);
    }
    
    @After
    public void tearDown() throws Exception {
        File file = new File("test");
        file.deleteOnExit();
        FileUtils.deleteDirectory(file);
    }
    

    @Test
    public void fullFile() throws Exception {
        this.testFile(1000, 0);
        assertEquals(HTTPStatusCodes.OK.code(), response.getStatus());
        String rangeHeader = (String) response.getHeader(HTTPHeaders.CONTENT_RANGE.headerName());
        assertEquals("0-1000/1000", rangeHeader);
        ContentEvent event = (ContentEvent) eventManager.getMostRecent();
        assertEquals(1000, event.getBytesManipulated());
        assertEquals(1000, event.getBytesRequested());
        assertEquals(0, event.getBytesFailed());
        assertEquals(ContentEventResult.SUCCESS.value(), event.getResult());
    }

    @Test
    public void partialFile() throws Exception {
        this.testFile(1000, 500);
        assertEquals(HTTPStatusCodes.OK_PARTIAL.code(), response.getStatus());
        String rangeHeader = (String) response.getHeader(HTTPHeaders.CONTENT_RANGE.headerName());
        assertEquals("500-1000/500", rangeHeader);
        ContentEvent event = (ContentEvent) eventManager.getMostRecent();
        assertEquals(500, event.getBytesManipulated());
        assertEquals(500, event.getBytesRequested());
        assertEquals(0, event.getBytesFailed());
        assertEquals(ContentEventResult.SUCCESS.value(), event.getResult());
    }

    @Test
    public void partialFileBadStartPos() throws Exception {
        // This needs to be fixed, InputStream.skip() skips zero bytes when
        // negative
        this.testFile(1000, 1500);
        assertEquals(HTTPStatusCodes.OK.code(), response.getStatus());
        String rangeHeader = (String) response.getHeader(HTTPHeaders.CONTENT_RANGE.headerName());
        assertEquals("0-1000/1000", rangeHeader);
    }

    @SuppressWarnings("unchecked")
    private void testFile(int fileLength, int startPos) throws Exception {

        SynchronizationEvent event = new SynchronizationEvent("xxx", 1001);
        monitor.startSupplier(event, new SupplierProgress(1, 1));
        
        if (startPos != 0) {
            HTTPRangeHeader range = new HTTPRangeHeader(startPos);
            request.addHeader(HTTPHeaders.RANGE.headerName(), range.toString());
        }
        
        request.addHeader(HTTPHeaders.SYNC_ID.headerName(), "xxx");

        File tempFile = File.createTempFile("temp", "tmp");
        FileWriter writer = new FileWriter(tempFile);
        for (int i = 0; i < fileLength; i++) {
            writer.write("X");
        }
        writer.close();

        expect(mock.getFile(CONTENT_SET, PATH, 0l)).andReturn(tempFile);
        expect(mock.getId(CONTENT_SET)).andReturn((long) 1001);
        expect(mock.isInventory(PATH)).andReturn(false);
        mock.addDisabledListener((Callback<ContentSet>)EasyMock.anyObject());
        mock.removeDisabledListener((Callback<ContentSet>)EasyMock.anyObject());
        replay(mock);
        handler.handle("/", request, response, Handler.REQUEST);
        verify(mock);

        String contentLength = (String) response.getHeader(HTTPHeaders.CONTENT_LENGTH.headerName());
        int totalbytes = fileLength - startPos > 0 ? fileLength - startPos : fileLength;
        assertEquals(Integer.valueOf(totalbytes), Integer.valueOf(contentLength));
    }
}
