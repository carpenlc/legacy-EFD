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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
class CsvHandler extends DefaultHandler {
    
    private static final byte [] NEWLINE = new byte [] { '\r', '\n' };
    
    private final File supplierFile;
    private final File consumerFile;
    
    private final List<String> buffer;
    
    private OutputStream current;
    private boolean headersComplete;
    private boolean headersStarted;
    
    CsvHandler(File supplierFile, File consumerFile) {
        this.supplierFile = supplierFile;
        this.consumerFile = consumerFile;
        
        buffer = new ArrayList<String>();
        headersComplete = false;
        headersStarted = false;
    }
    
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String value = new String(ch, start, length);
        if (!StringUtils.isBlank(value)) {
            if (headersComplete) {
                write(value);
            } else {
                buffer.add(value);
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String name) throws SAXException {
        if (name.equals("consumer")) {
            headersComplete = false;
            headersStarted = false;
        }
        
        if (headersStarted) {
            if (name.equals("synchronization")) {
                headersComplete = true;
                headersStarted = false;
                writeBuffer();
            }
        }
        if (name.equals("synchronization")) {
            newline();
        }
    }

    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
        if (name.equals("consumer")) {
            closeCurrent();
            setCurrent(consumerFile);
        }
        if (name.equals("supplier")) {
            closeCurrent();
            setCurrent(supplierFile);
        }
        
        if (headersComplete) {
            return;
        }
        if (!headersStarted) {
            if (name.equals("synchronization")) {
                headersStarted = true;
            }
        } else {
            write(name);
        }
    }
    
    @Override
    public void endDocument() throws SAXException {
        closeCurrent();
    }

    private void write(String value) throws SAXException {
        try {
            current.write(value.trim().getBytes());
            current.write(',');
        }  catch (IOException ex) {
            throw new SAXException(ex);
        }
    }
    
    private void writeBuffer() throws SAXException {
        newline();
        for (String value : buffer) {
            write(value);
        }
        buffer.clear();
    }
    
    private void newline() throws SAXException {
        try {
            current.write(NEWLINE);
        } catch (IOException ex) {
            throw new SAXException(ex);
        }
    }
    
    private void closeCurrent() throws SAXException {
        if (current != null) {
            try {
                current.close();
            } catch (IOException ex) {
                throw new SAXException(ex);
            }
        }
    }
    
    private void setCurrent(File file) throws SAXException {
        try {
            current = new FileOutputStream(file);
        } catch (IOException ex) {
            throw new SAXException(ex);
        }
    }
}
