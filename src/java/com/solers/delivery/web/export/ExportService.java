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
package com.solers.delivery.web.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.solers.util.NamedThreadFactory;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class ExportService {
    
    protected static final Logger log = Logger.getLogger(ExportService.class);
    
    private final File exportDirectory;
    private final ExecutorService executor;
    
    public ExportService(String directory) {
        this(new File(directory));
    }
    
    public ExportService(File directory) {
        this.exportDirectory = directory;
        this.executor = Executors.newCachedThreadPool(new NamedThreadFactory());
        
        log.info("ExportService initialized with exportDirectory: <" + directory + ">");
    }
    
    public String getExportPath() {
        return this.exportDirectory.getAbsolutePath();
    }
    
    public <T> void export(ExportData<T> data, String name) {
        this.executor.submit(new ExportTask<T>(data, name));
    }
    
    /**
     * Shutdown the export service and wait for any outstanding jobs to complete
     */
    public void shutdown() {
        log.info("Shutting down");
        this.executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException ex) {
            throw new RuntimeException("Interrupted waiting for ExportService to shutdown", ex);
        }
        log.info("Shut down");
    }
    
    protected <T> File getExportFile(String name, ExportData<T> data) {
        File directory = new File(exportDirectory, name);
        directory.mkdirs();
        
        return new File(directory, data.getExportFileName());
    }
    
    class ExportTask<T> implements Runnable {
        
        private final ExportData<T> data;
        private final String name;
        
        ExportTask(ExportData<T> data, String name) {
            this.data = data;
            this.name = name;
        }
        
        // KRJ 2016-08-31: Converted writer creation to "try with resources"
        // based on an HP Fortify recommendation
        
        public void run() {
            File file = getExportFile(name, data);
                
            try (Writer writer = new BufferedWriter(new FileWriter(file, true))) {
                Iterator<T> iterator = data.iterator();
                
                writer.write(data.getHeader());
                
                while (iterator.hasNext()) {
                    writer.write(data.getRow(iterator.next()));
                }                
            } catch (IOException ex) {
                log.error("Error exporting " + name + ": " + ex.getMessage(), ex);
                throw new RuntimeException(ex);
            }
        }
    }
}
