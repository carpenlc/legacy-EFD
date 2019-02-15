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
package com.solers.delivery.transport.http.client.methods;

import com.solers.delivery.transport.http.HTTPContextPaths;
import com.solers.delivery.transport.http.HTTPHeaders;
import com.solers.delivery.transport.http.client.methods.ContentGetMethod;
import com.solers.delivery.transport.http.client.util.Session;

import junit.framework.TestCase;

public class ContentGetMethodTestCase extends TestCase {

    public void testPathsUseCorrectSeparator() {
        ContentGetMethod m = new ContentGetMethod(new Session("test","", ""),"a\\b\\c");
        assertEquals(HTTPContextPaths.CONTENT_CONTEXT_PATH + "/a/b/c", m.buildPath(HTTPHeaders.USER_AGENT.defaultValue()));
        assertEquals("/a/b/c", m.buildPath(null));
    }
    
    public void testSetPath() {
        ContentGetMethod m = new ContentGetMethod(new Session("whatever", "", ""),"a/b/c");
        assertEquals(HTTPContextPaths.CONTENT_CONTEXT_PATH + "/a/b/c", m.buildPath(HTTPHeaders.USER_AGENT.defaultValue()));
        assertEquals("/a/b/c", m.buildPath(null));
    }
}
