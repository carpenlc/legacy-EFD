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
package com.solers.delivery.web;

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class XSLTFilter implements Filter {
    
    private static final Logger log = Logger.getLogger(XSLTFilter.class);
    
    private boolean debug;
    private ServletContext ctx;
    private Templates cache;
    private TransformerFactory factory;
    private String styleSheet;
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        response.setContentType("text/html");
        
        PrintWriter out = response.getWriter();
        Source inputSource = getSource(request, response);
        try {
            Transformer transformer = newTransformer();
            StreamResult result = new StreamResult(out);
            transformer.transform(inputSource, result);
        } catch (TransformerException ex) {
            log.error("Could not perform transformation", ex);
            throw new ServletException("Could not perform transformation", ex);
        }
    }

    @Override
    public void init(FilterConfig config) throws ServletException {
        ctx = config.getServletContext();
        debug = Boolean.parseBoolean(config.getInitParameter("debug"));
        String file = config.getInitParameter("styleSheet");
        
        /*
            KRJ 2016-10-25 
        
            Converted InputStream creation below to "try with resources" based on an
            HP Fortify recommendation
        
        */
        
        try (InputStream style = ctx.getResourceAsStream(file))
        {
            Source styleSource = new StreamSource(style);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
        
            if (debug) {
                factory = transformerFactory;
                styleSheet = file;
            } else {
                try {
                    cache = transformerFactory.newTemplates(styleSource);
                } 
                catch (TransformerConfigurationException ex) {
                    log.error("Could not configure transformer", ex);
                    throw new ServletException("Could not configure transformer", ex);
                }
            }
        }
        catch (IOException ex) {
            throw new ServletException ("Could not establish InputStream object in init() method", ex);
        }
    }

    @Override
    public void destroy() {
        
    }
    
    private Source getSource(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        ResponseWrapper wrapper = new ResponseWrapper((HttpServletResponse)response);
        RequestDispatcher dispatcher = ctx.getRequestDispatcher(((HttpServletRequest)request).getRequestURI());
        dispatcher.include(request, wrapper);
        return new StreamSource(wrapper.getReader());
    }
    
    private Transformer newTransformer() throws TransformerConfigurationException, IOException {
        if (debug) {
            
            /* 
            
            KRJ 2016-10-25
            
            Converted InputStream object creation below to "try with resources" in 
            response to an HP Fortify recommendation
            
            */
            
            try (InputStream inputStream = ctx.getResourceAsStream(styleSheet)) {
                return factory.newTransformer(new StreamSource(inputStream));
            }
        } else {
            return cache.newTransformer();
        }
    }
    
    private static class ResponseWrapper extends HttpServletResponseWrapper {
        
        private final CharArrayWriter output;
        
        public ResponseWrapper(HttpServletResponse response) {
            super(response);
            output = new CharArrayWriter();
        }
        
        public Reader getReader() {
            return new CharArrayReader(output.toCharArray());
        }
        
        @Override
        public PrintWriter getWriter() {
            return new PrintWriter(output);
        }
    }
    
}
