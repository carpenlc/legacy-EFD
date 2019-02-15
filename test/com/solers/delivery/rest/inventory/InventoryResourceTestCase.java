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
package com.solers.delivery.rest.inventory;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.restlet.data.MediaType;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.w3c.dom.Document;

import com.solers.delivery.inventory.fs.FileSystemInventoryProvider;
import com.solers.delivery.inventory.plugin.InventoryPlugin;
import com.solers.delivery.rest.BaseRestTestCase;
import com.solers.delivery.rest.Utils;

public class InventoryResourceTestCase extends BaseRestTestCase {
    public void setUp() {
        super.setUp();
        uri = "http://localhost:"+getPort()+"/inventory/";
    }
    
    public void testGetWithoutAuth() throws Exception {
        authenticate = false;
        get(Status.CLIENT_ERROR_UNAUTHORIZED);
    }
    
    public void testGet() throws Exception {
        authenticate = true;
        Response r = get(MediaType.TEXT_XML, Status.SUCCESS_OK);
        int numProviders = InventoryPlugin.getProviderInfo().size();
        Document d = r.getEntityAsDom().getDocument();
        String nodeName = d.getDocumentElement().getNodeName();
        assertEquals("plugins", nodeName);
        assertEquals(numProviders, d.getDocumentElement().getElementsByTagName("plugin").getLength());
        
        InventoryPlugin.register(FileSystemInventoryProvider.class);
        r = get(MediaType.TEXT_XML, Status.SUCCESS_OK);
        numProviders = InventoryPlugin.getProviderInfo().size();
        d = r.getEntityAsDom().getDocument();
        nodeName = d.getDocumentElement().getNodeName();
        int numPlugins = d.getDocumentElement().getElementsByTagName("plugin").getLength();
        assertEquals("plugins", nodeName);
        assertEquals(numProviders, numPlugins);
    }
    
    public void testPut() throws Exception {
        InventoryPlugin.register(FileSystemInventoryProvider.class);
        File f = new File(".");
        assertTrue(f.isDirectory());
        assertTrue(f.exists());
        String uri = f.toURI().toASCIIString();
        Response r = put(uri, MediaType.TEXT_XML, Status.SUCCESS_OK);
        assertEquals("parameters", r.getEntityAsDom().getDocument().getDocumentElement().getNodeName());
        assertEquals(InventoryPlugin.getParameterInfo(f.toURI()).size(),
            r.getEntityAsDom().getDocument().getElementsByTagName("parameter").getLength()
        );
    }
    
    public void testPutValidURINoInventory() throws Exception {
        String uri = "blahBLAH:/har/har/har/";
        put(uri, MediaType.TEXT_XML, Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE);
    }
    
    public void testPutInvalidURI() throws Exception {
        String uri = "throw : : : URI syntax exception";
        try {
            new URI(uri);
            fail("URI should throw URISyntaxException");
        } catch (URISyntaxException use) {}
        put(uri, MediaType.TEXT_XML, Utils.CLIENT_ERROR_VALIDATION);
    }
}
