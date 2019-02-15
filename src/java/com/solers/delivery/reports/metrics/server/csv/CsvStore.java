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
package com.solers.delivery.reports.metrics.server.csv;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class CsvStore {
    
    private File root;
    
    public CsvStore(File root) {
        this.root = root;
    }
    
    public void store(String hostname, String xml) throws IOException {
        File supplierDirectory = getDirectory(hostname, "suppliers");
        File consumerDirectory = getDirectory(hostname, "consumers");
        
        String name = String.valueOf(System.nanoTime());
        File supplierFile = new File(supplierDirectory, name);
        File consumerFile = new File(consumerDirectory, name);
        try {
            store(xml, supplierFile, consumerFile);
        } catch (SAXException ex) {
            throw new IOException(ex);
        } 
    }
    
    private void store(String xml, File supplierFile, File consumerFile) throws SAXException, IOException {
        XMLReader reader = XMLReaderFactory.createXMLReader();
        
        //KRJ 20116-08-25: Added the next 3 setFeature() calls in response to a Fortify recommendation.
        // These may need to be backed out.  Hopefully this can be tested.
        
        reader.setFeature("http://xml.org/sax/features/external-general-entities", false);
        reader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        reader.setFeature("http://xml.org/sax/features/nonvalidating/load-external-dtd", false);
        
        reader.setContentHandler(new CsvHandler(supplierFile, consumerFile));
        reader.parse(new InputSource(new StringReader(xml)));
    }
    
    private File getHostDirectory(String hostname) {
        File directory = new File(root, hostname);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return directory;
    }
    
    private File getDirectory(String hostname, String name) {
        File hostDirectory = getHostDirectory(hostname);
        File result = new File(hostDirectory, name);
        if (!result.exists()) {
            result.mkdirs();
        }
        return result;
    }
}
