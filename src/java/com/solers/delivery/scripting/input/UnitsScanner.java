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
package com.solers.delivery.scripting.input;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.CanReadFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;

import com.solers.delivery.scripting.ScriptConfiguration;
import com.solers.delivery.scripting.ScriptUnit;

public class UnitsScanner {
    private final ScriptConfiguration scriptConfig;    
    private final static AtomicInteger counter = new AtomicInteger(1);
    //file filter for files that are not hidden and can be read
    private final static IOFileFilter FILE_FILTER = FileFilterUtils.andFileFilter(HiddenFileFilter.VISIBLE, CanReadFileFilter.CAN_READ);
    //directory filter for directories that are directories, not hidden, and can be read
    private final static IOFileFilter DIR_FILTER = FileFilterUtils.andFileFilter(
        FileFilterUtils.andFileFilter(HiddenFileFilter.VISIBLE, DirectoryFileFilter.DIRECTORY), 
        CanReadFileFilter.CAN_READ);
    
    public UnitsScanner(ScriptConfiguration scriptConfig) {    
        this.scriptConfig = scriptConfig;
    }
    
    public List<ScriptUnit> scanNext(ScriptInput input) {
        List<ScriptUnit> list = new ArrayList<ScriptUnit>();
        for(ScriptInput.DirectoryRecord record: input.getDirectoryList()) {
            List<String> newFiles = scanFiles(record.getLocation());
            newFiles.removeAll(record.getFilenames());           
            record.addFilenames(newFiles);
            List<ScriptUnit> units = createUnits(newFiles, record.getLanguage(), Integer.toString(record.getGroupId()), record.getArguments());
            list.addAll(units);
        }                     
        return list;
    } 
    
    private List<String> scanFiles(File dir) {
        List<String> list = new ArrayList<String>();
        
        if (dir.exists() && dir.isDirectory()) {
            //only processes dirs/files that are not hidden and can be read
            @SuppressWarnings("unchecked")
            Collection<File> files = FileUtils.listFiles(dir, FILE_FILTER, DIR_FILTER);
            for(File aFile: files) {
                list.add(aFile.getAbsolutePath());
            }            
        } 
        return list;        
    }
    
    private List<ScriptUnit> createUnits(List<String> fileNames, String language, String groupId, String args) {
        List<ScriptUnit> list = new ArrayList<ScriptUnit>();
        if(fileNames != null) {
            for (String fileName: fileNames ) {
                ScriptUnit unit = createUnit(fileName, language, groupId, args);
                list.add(unit);
            }            
        }

        return list;
    }
    
    private ScriptUnit createUnit(String fileName, String language, String groupId, String args) {
        File file = new File(fileName);
        if(language == null) {
            language = getLanguage(file.getName());
        }       
        return new ScriptUnit(createId(fileName), groupId, file, language, args);
    }

    private String getLanguage(String fileName) {
        String extension = (fileName.lastIndexOf(".") == -1) ? "" : fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
        return scriptConfig.getLanguageFromExtension(extension);
    }
    
    private String createId(String fileName) {
        return fileName + "_" + Integer.toString(counter.getAndIncrement());
    }
}
