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
package com.solers.delivery.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.restlet.engine.ClientHelper;
import org.restlet.engine.ServerHelper;
import org.restlet.engine.authentication.AuthenticationHelper;
import org.restlet.engine.authentication.HttpBasicHelper;
import org.restlet.engine.http.StreamServerHelper;
import org.restlet.ext.spring.SpringEngine;

import com.solers.delivery.content.ContentService;
import com.solers.delivery.content.RestfulContentService;
import com.solers.delivery.domain.ContentSet;
import com.solers.delivery.lucene.LuceneHelper;
import com.solers.delivery.reports.history.LuceneSynchronizationHistory;
import com.solers.delivery.reports.history.ReportDetail;
import com.solers.delivery.reports.history.Synchronization;
import com.solers.delivery.reports.history.SynchronizationHistory;
import com.solers.delivery.rest.auth.DefaultRestAuthentication;
import com.solers.delivery.rest.auth.RestAuthentication;
import com.solers.delivery.rest.connectors.SslClientHelper;
import com.solers.delivery.web.export.ExportService;
import com.solers.delivery.web.export.SynchronizationDetailsExportData;
import com.solers.delivery.web.export.SynchronizationExportData;
import com.solers.util.IOConsole;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class LuceneExporter {
    
    public static void main(String [] args) {
        SpringEngine engine = new SpringEngine();
        engine.setRegisteredClients(Arrays.asList((ClientHelper) new SslClientHelper(null)));
        engine.setRegisteredServers(Arrays.asList((ServerHelper) new StreamServerHelper(null)));
        engine.setRegisteredAuthentications(Arrays.asList((AuthenticationHelper) new HttpBasicHelper()));
        try {
            run(args);
        } catch (UsageException ex) {
            usage(ex.getMessage());
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }
    
    private static void run(String [] args) throws UsageException {
        checkArgs(args);
        File siteDirectory = getSiteDirectory(args);
        int port = getPort(args);
        String host = "localhost";
        RestAuthentication auth = new DefaultRestAuthentication(getUsername(), getPassword());
        ContentService service = new RestfulContentService(host, port, auth);
        
        if (listing(args)) {
            listContentSets(service);
        } else {
            ContentSet set = getContentSet(args, service);
            if (set != null) {
                export(siteDirectory, set.getId(), set.getName());
            }
        }
    }
    
    private static void listContentSets(ContentService service) {
        IOConsole.DEFAULT.println("Content sets:");
        int count = 1;
        for (ContentSet c : service.getContentSets()) {
            IOConsole.DEFAULT.println((count++)+".) "+c.getName());
        }
    }
    
    private static ContentSet getContentSet(String [] args, ContentService service) {
        String name = args[2];
        for (ContentSet c : service.getContentSets()) {
            if (c.getName().equals(name)) {
                return c;
            }
        }
        IOConsole.DEFAULT.println(name+" is not a valid content set");
        return null;
    }
    
    private static void checkArgs(String [] args) throws UsageException {
        if (args.length < 2) {
            throw new UsageException("Invalid arguments");
        }
    }
    
    private static File getSiteDirectory(String [] args) throws UsageException {
        File siteDirectory = new File(args[0]);
        if (!siteDirectory.exists()) {
            throw new UsageException("Site directory does not exist");
        }
        return siteDirectory;
    }
    
    private static int getPort(String [] args) throws UsageException {
        try {
            return Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            throw new UsageException("Port must be a number");
        }
    }
    
    private static boolean listing(String [] args) {
        return args.length == 2 || args.length == 3 && StringUtils.isBlank(args[2]);
    }
    
    private static String getPassword() {
        return String.valueOf(IOConsole.DEFAULT.readPassword("web", false));
    }
    
    private static String getUsername() {
        return IOConsole.DEFAULT.readLine("Enter [web] username");
    }
    
    private static void export(File siteDirectory, Long id, String name) {
        String strId = String.valueOf(id);
        LuceneHelper helper = new LuceneHelper(siteDirectory);
        File file = new File(helper.getEventDirectory(), strId);
        if (!file.exists()) {
            IOConsole.DEFAULT.println("There are no events for content set: "+name);
            return;
        }
        
        try {
            File workDir = new File(System.getProperty("java.io.tmpdir"), strId+"-workdir");
            workDir.mkdirs();
            
            doExport(workDir, siteDirectory, id, name);
            doZip(workDir, name);
            
            FileUtils.deleteDirectory(workDir);
            System.out.println("Done");
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }
    
    private static void doExport(File workDir, File siteDirectory, Long contentSetId, String exportName) {
        ExportService service = new ExportService(workDir);
        LuceneHelper helper = new LuceneHelper(siteDirectory);
        SynchronizationHistory history = new LuceneSynchronizationHistory(helper);
        
        List<Synchronization> synchronizations = history.getSynchronizations(contentSetId, null, null, false, SynchronizationHistory.PAGE_SIZE);
        service.export(new SynchronizationExportData(false, synchronizations), exportName);
        
        for (Synchronization sync : synchronizations) {
            List<ReportDetail> details = history.getSynchronizationDetails(contentSetId, sync.getId(), null, null, SynchronizationHistory.PAGE_SIZE);
            service.export(new SynchronizationDetailsExportData(false, details), exportName);
        }
        
        System.out.println("Exporting...");
        service.shutdown();
        System.out.println("Export complete");
    }
    
    private static void doZip(File workDir, String name) throws IOException {
        System.out.println("Zipping exported files...");
        File output = new File(name+".zip");
        
        // KRJ 2016-08-31: Converted zipOut and input creation to "try with resources"
        // based on an HP Fortify recommendation
        
        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(output))) {
        
            File dir = new File(workDir, name);
        
            for (File f : dir.listFiles()) {
                zipOut.putNextEntry(new ZipEntry(name+File.separator+f.getName()));
                try (InputStream inputStream = new FileInputStream(f)) {
                    IOUtils.copyLarge(inputStream, zipOut);
                }
            }
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
            
        System.out.println("Zipped files to: "+output.getAbsolutePath());
    }
    
    private static void usage(String message) {
        System.err.println(message);
        System.err.println("Usage: java "+LuceneExporter.class.getSimpleName()+" <site directory> <port number> [content set name]");
    }
}
