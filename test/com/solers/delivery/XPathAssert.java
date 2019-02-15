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
package com.solers.delivery;

import java.io.StringReader;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import junit.framework.Assert;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class XPathAssert {
    
    public static void assertXPathNodeExists(String xml, String expression) throws Exception {
        XPath xpath = XPathFactory.newInstance().newXPath();
        
        Node node = (Node) xpath.evaluate(expression, new InputSource(new StringReader(xml)), XPathConstants.NODE);
        Assert.assertFalse(node == null);
    }
    
    public static void assertXPathNodeValue(String xml, String expression, String expected) throws Exception {
        XPath xpath = XPathFactory.newInstance().newXPath();
        
        String actual = (String) xpath.evaluate(expression, new InputSource(new StringReader(xml)), XPathConstants.STRING);
        Assert.assertEquals(expected, actual);
    }
    
    public static void assertXPathNodeSetEmpty(String xml, String expression) throws Exception {
        XPath xpath = XPathFactory.newInstance().newXPath();
        
        Node node = (Node) xpath.evaluate(expression, new InputSource(new StringReader(xml)), XPathConstants.NODE);
        Assert.assertFalse(node == null);
        Assert.assertEquals(0, node.getChildNodes().getLength());
    }
    
    public static void assertXPathNodeSetValues(String xml, String expression, String... values) throws Exception {
        XPath xpath = XPathFactory.newInstance().newXPath();
        
        Node node = (Node) xpath.evaluate(expression, new InputSource(new StringReader(xml)), XPathConstants.NODE);
        Assert.assertFalse(node == null);
        
        NodeList nodes = node.getChildNodes();

        int count = 0;
        for (int i=0; i < nodes.getLength(); i++) {
            if (nodes.item(i).getNodeType() == Node.TEXT_NODE) {
                continue;
            }
            count++;
        }
        Assert.assertEquals(values.length, count);
        
        int skip = 0;
        for (int i=0; i < nodes.getLength(); i++) {
            if (nodes.item(i).getNodeType() == Node.TEXT_NODE) {
                skip++;
                continue;
            }
            Assert.assertEquals(values[i - skip], nodes.item(i).getFirstChild().getNodeValue());
        }
    }
}
