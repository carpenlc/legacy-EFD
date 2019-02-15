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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.solers.delivery.management.XmlProxy;
import javax.xml.XMLConstants;
import org.apache.log4j.Logger;

/**
 * <p>Combines multiple css and javascript files into a single file</p>
 * 
 * <p>This class will process an XML document that specifies how a certain group of css files and javascript files
 * should be combined into a single file.  For example:</p>
 * 
 * <code>
 *      <combine>
 *          <css file="/css/output.css">
 *              <file>/css/file1.css</file>
 *              <file>/css/file2.css</file>
 *          </css>
 *          
 *          <javascript file="/js/output.js">
 *              <file>/js/file1.js</file>
 *              <file>/js/file2.js</file>
 *          </javascript>
 *      </combine>
 * </code>
 * 
 * <p>The above would combine /css/file1.css and /css/file2.css into /css/output.css.  It would also combine
 *  /js/file1.js and /js/file2.js into /js/output.js</p>
 *  
 *  <p>You can also specify {@code com.solers.delivery.web.combine.Processor} with <processor class="..."/>
 *  That will perform some post processing or transformation on the combined text</p> 
 * 
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class Combiner {
    
    private static final Logger log = Logger.getLogger(Combiner.class);

    private final DocumentBuilder builder;
    private final File root;
    
    /**
     * @param root Directory that paths in the XML document are relative to.
     */
    public Combiner(File root) {
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            throw new RuntimeException(ex);
        }
        this.root = root;
    }
    
    /**
     * Process the <combine/> XML document contained in {@code file} and write the result to {@code output}
     * @param file
     * @param output
     * @throws IOException
     * @throws SAXException
     */
    public void combine(File file, File output) throws IOException, SAXException {
        InputStream input = new FileInputStream(file);
        try {
            Document root = builder.parse(input);
            Element index = (Element) root.getFirstChild();
            handle(root, index.getElementsByTagName("css"));
            handle(root, index.getElementsByTagName("javascript"));
            write(root, output);
        } finally {
            input.close();
        }
    }
    
    /* KRJ 2016-10-24
    
    Converted OutputStream object creation in handle() method below to 
    "try with resources" in response to     and identified HP Fortify issue
    
    */
    
    private void handle(Document document, NodeList elements) throws IOException {
        for (int i=0; i < elements.getLength(); i++) {
            Element element = (Element) elements.item(i);
            List<Processor> processors = getProcessors(element);
            try (OutputStream output = getOutput(element.getAttribute("file"))) {
                NodeList files = element.getElementsByTagName("file");
                for (int j=0; j < files.getLength(); j++) {
                    Node file = files.item(j);
                    output.write(process(file, processors));
                    output.write(new byte [] {'\r', '\n'});
                }
                clearChildren(element);
                addFileNode(document, element);
                element.removeAttribute("file");
            } catch (IOException ex) 
            {
                log.error ("Could not create OutputStream object", ex);
            }
        }
    }
    
    /*
    
    Old code for handle() method before "try with resources" implementation above
    
    private void handle(Document document, NodeList elements) throws IOException {
        for (int i=0; i < elements.getLength(); i++) {
            Element element = (Element) elements.item(i);
            List<Processor> processors = getProcessors(element);
            OutputStream output = getOutput(element.getAttribute("file"));
            NodeList files = element.getElementsByTagName("file");
            for (int j=0; j < files.getLength(); j++) {
                Node file = files.item(j);
                output.write(process(file, processors));
                output.write(new byte [] {'\r', '\n'});
            }
            clearChildren(element);
            addFileNode(document, element);
            element.removeAttribute("file");
            output.close();
        }
    }*/
    
    private void addFileNode(Document document, Element parent) {
        Element node = document.createElement("file");
        node.setTextContent(parent.getAttribute("file"));
        parent.appendChild(node);
    }
    
    private void clearChildren(Node node) {
        Node child = node.getFirstChild();
        while (child != null) {
            node.removeChild(child);
            child = node.getFirstChild();
        }
    }
    
    private OutputStream getOutput(String fileName) throws IOException {
        File file = new File(root, fileName);
        return new FileOutputStream(file);
    }
    
    private byte[] process(Node node, List<Processor> processors) throws IOException {
        File file = new File(root, node.getTextContent());
        String data = null;
        
        // KRJ 2016-08-26: Converted creation of input to "try with resources"
        // based on an HP Fortify recommendation
        
        try (InputStream input = new FileInputStream(file)) {
            data = IOUtils.toString(input);
            input.close();
            for (Processor p : processors) {
                data = p.process(data);
            }
        }
        catch (IOException i) {
            log.error ("Could not create FileInputStream object", i);
        }
        return ((data != null) ? data.getBytes() : null);
    }
    
    private List<Processor> getProcessors(Element e) throws IOException {
        NodeList processors = e.getElementsByTagName("processor");
        int length = processors.getLength();
        if (length == 0) {
            return Collections.emptyList();
        }
        
        List<Processor> result = new ArrayList<Processor>(length);
        for (int i=0; i < processors.getLength(); i++) {
            Element p = (Element) processors.item(i);
            XmlProxy proxier = new XmlProxy(p);
            try {
                Class<?> clazz = Class.forName(p.getAttribute("class"));
                Processor pr = (Processor) proxier.proxyBean(clazz);
                result.add(pr);
            } catch (Exception ex) {
                throw new IOException(ex);
            }
        }
        
        return result;
    }
    
    private void write(Document doc, File file) throws IOException {
        TransformerFactory factory = TransformerFactory.newInstance();
        
        // KRJ 6/23/2016: Added the next two setAttribute calls in response
        // to an HPFortify issue identified.  This may be an issue for EFD, but
        // we shall see.
        
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        try {
            Transformer transformer = factory.newTransformer();
            transformer.transform(new DOMSource(doc), new StreamResult(file));
        } catch (TransformerException ex) {
            throw new IOException(ex);
        }
    }
}
