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
import static com.solers.delivery.transport.http.HTTPHeaders.INVENTORY_TIMESTAMP;
import static com.solers.delivery.transport.http.HTTPStatusCodes.UNSUPPORTED_METHOD;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mortbay.jetty.Handler;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.solers.delivery.content.supplier.ContentSetMapper;
import com.solers.delivery.content.supplier.InventoryNotChangedException;
import com.solers.delivery.content.supplier.UnregisteredContentSetException;
import com.solers.delivery.domain.ContentSet;
import com.solers.util.Callback;

public class ValidationHandlerTest {

    private ValidationHandler vHandler;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @Before
    public void beforeEachTest() {
        vHandler = new MockValidationHandler();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        vHandler.setCm(new MockContentSetMapper());
    }

    @Test
    public void testHandlerPost() {

        request.setMethod("POST");
        try {
            vHandler.handle("/", request, response, Handler.REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert response.getStatus() == UNSUPPORTED_METHOD.code();
    }

    @Test
    public void testHandlerWithHeader() {

        request.setMethod("GET");
        request.addHeader(CONTENT_SET_NAME.headerName(), "testContentSet");
        request.setPathInfo("//");
        try {
            vHandler.handle("//", request, response, Handler.REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert response.getStatus() == 200;
    }
    
    @Test
    public void testNotModifiedResponse() {
        request.setMethod("GET");
        request.addHeader(CONTENT_SET_NAME.headerName(), "testContentSet");
        request.addHeader(INVENTORY_TIMESTAMP.headerName(), "5");
        request.setPathInfo("//");
        try {
            vHandler.handle("//", request, response, Handler.REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert response.getStatus() == 304;
    }

    class MockContentSetMapper implements ContentSetMapper {

        /**
         * @see com.solers.delivery.content.supplier.ContentSetMapper#getFile(java.lang.String, java.lang.String)
         */
        @Override
        public File getFile(String contentSetName, String pathFromRoot) throws UnregisteredContentSetException {
            return null;
        }
        
        @Override
        public File getFile(String contentSetName, String pathFromRoot, long timestamp) {
            if (timestamp >= 5) throw new InventoryNotChangedException();
            return null;
        }

        /**
         * @see com.solers.delivery.content.supplier.ContentSetMapper#getId(java.lang.String)
         */
        @Override
        public Long getId(String contentSetName) throws UnregisteredContentSetException {
            return new Long(1);
        }

        public boolean isInventory(String path) {
            return false;
        }

        @Override
        public boolean isAllowed(String contentSetName, String commonName) {
            return true;
        }

        @Override
        public String getName(Long id) {
            return null;
        }

        @Override
        public void addDisabledListener(Callback<ContentSet> e) {
            
        }

        @Override
        public void removeDisabledListener(Callback<ContentSet> e) {
            
        }

    }

    class MockValidationHandler extends ValidationHandler {
        @Override
        protected void setRequestHandled(HttpServletRequest request, HttpServletResponse response) {

        }
    }

}
