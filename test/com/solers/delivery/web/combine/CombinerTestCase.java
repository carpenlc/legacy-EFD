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
package com.solers.delivery.web.combine;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import com.solers.delivery.XPathAssert;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class CombinerTestCase {
    
    @Test
    public void testCombine() throws Exception {
        File file = file("/combine/combine.xml");
        File output = new File("combineResult.xml");
        File root = file.getParentFile();
        
        Combiner c = new Combiner(root);
        try {
            c.combine(file, output);
            
            Assert.assertEquals("one-css;Testing!\r\ntwo-css;Testing!\r\n", contents(root, "css/combined.css"));
            Assert.assertEquals("one-js;\r\ntwo-js;\r\n", contents(root, "javascript/combined.js"));
            
            String xml = contents(output);
            
            XPathAssert.assertXPathNodeExists(xml, "//combine");
            XPathAssert.assertXPathNodeValue(xml, "//combine/css/file", "/css/combined.css");
            XPathAssert.assertXPathNodeValue(xml, "//combine/javascript/file", "/javascript/combined.js");
            // File attributes should have been removed
            XPathAssert.assertXPathNodeValue(xml, "//combine/css[@file]", "");
            XPathAssert.assertXPathNodeValue(xml, "//combine/javascript[@file]", "");
            
        } finally {
            new File(root, "css/combined.css").delete();
            new File(root, "javascript/combined.js").delete();
            output.delete();
        }
    }
    
    private File file(String name) throws Exception {
        return new File((getClass().getResource(name)).toURI());
    }
    
    private String contents(File root, String fileName) throws IOException {
        File file = new File(root, fileName);
        return contents(file);
    }
    
    private String contents(File file) throws IOException {
        FileInputStream input = new FileInputStream(file);
        try {
            return IOUtils.toString(input);
        } finally {
            input.close();
        }
    }
}
