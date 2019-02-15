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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.IOUtils;

import com.solers.delivery.util.FileSystemUtil;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.StreamException;

public class ScriptInput {
    List<DirectoryRecord> directoryList;

    public ScriptInput() {}

    public ScriptInput(String scriptDir) {
        DirectoryRecord record = new DirectoryRecord(scriptDir, null, null);
        this.directoryList = new ArrayList<DirectoryRecord>();
        this.directoryList.add(record);
    }
    
    public List<DirectoryRecord> getDirectoryList() {
        return directoryList;
    }

    public void setDirectoryList(List<DirectoryRecord> directoryList) {
        this.directoryList = directoryList;
    }
    
    public void assignGroupId(int startNumber) {
        if(directoryList != null) {
            for(DirectoryRecord record: directoryList) {
                record.setGroupId(startNumber++);
            }
        }
    }
    
    public List<String> validate() {
        List<String> errors = new ArrayList<String>();
        
        if(directoryList != null) {
            for(DirectoryRecord record: directoryList ) {
                if(record.getLocation() == null) {
                    errors.add("Directory location is required for the 'directory' entry " );
                }else {
                    if(!record.getLocation().exists()) {
                        errors.add(String.format("Directory %s does not exist", record.getLocation()) );
                    }
                }
            }
        }
        
        int dirCount = directoryList != null ? directoryList.size():0;
        
        if(dirCount == 0) {
            errors.add("There is no file or directory entry");
        }
        return errors;
    }

    public static class BaseRecord {        
        private File location;
        private String language;
        private String arguments;
        
        public BaseRecord(String location, String language, String arguments) {
            super();
            this.location = FileSystemUtil.relativeToEFDHome(location);
            this.language = language;
            this.arguments = arguments;
        }
        
        public File getLocation() {
            return location;
        }
        public String getLanguage() {
            return language;
        }
        public String getArguments() {
            return arguments;
        }
        
    }
    
    public static class DirectoryRecord extends BaseRecord{
        private List<String> filenames; // accumulated names of files exists or existed in the directory 
                                        // from a point in time
        private int groupId;


        public DirectoryRecord(String directoryName, String language, String args) {
            super(directoryName, language, args);            
            this.filenames = null;
            this.groupId = 0;
        }      
        
        public void addFilenames(Collection<String> names) {
            if(this.filenames == null) {
                this.filenames = new ArrayList<String>();
            }
            this.filenames.addAll(names);
        }
        
        public List<String> getFilenames() {
            List<String> names = new ArrayList<String>();
            if(this.filenames != null) {
                names.addAll(this.filenames);
            }
            return names; 
        }
        
        public boolean hasFilename(String name) {
            boolean exist = false;
            if(filenames != null) {
                exist = filenames.contains(name);
            }
            return exist;
        }              

        public int getGroupId() {
            return groupId;
        }

        public void setGroupId(int groupId) {
            this.groupId = groupId;
        }
    }

    public String toXml() {
        XStream xstream = createXStream();
        return xstream.toXML(this);
    }

    public static ScriptInput fromXml(String xml) {
        if (xml == null || xml.trim().length() == 0) {
            throw new IllegalArgumentException("xml must not be null or an empty string.");
        }
        
        ScriptInput input = null;

        try {
            XStream xstream = createXStream();
            input = (ScriptInput) xstream.fromXML(xml);
            
            //set location to be relative to efd.home
            List<DirectoryRecord> list = new ArrayList<DirectoryRecord>();
            for(DirectoryRecord record : input.getDirectoryList()) {
                DirectoryRecord newRecord = new DirectoryRecord(record.getLocation().getPath(), record.getLanguage(), record.getArguments());
                newRecord.setGroupId(record.getGroupId());
                list.add(newRecord);
            }
            
            input.getDirectoryList().clear();
            input.getDirectoryList().addAll(list);
        } catch (StreamException ex) {
            //rethrow as IllegalArgumentException to insulate caller
            //from dealing with a third party exception
            throw new IllegalArgumentException(ex);
        }
        
        return input;
    }

    public void writeToFile(String fileName) throws IOException {
        XStream xstream = createXStream();
        Writer writer = null;
        try {
            writer = new FileWriter(fileName);
            xstream.toXML(this, writer);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    public static ScriptInput readFromFile(String fileName) throws FileNotFoundException {
        Reader reader = null;
        try {
            reader = new FileReader(fileName);
            XStream xstream = createXStream();
            return (ScriptInput) xstream.fromXML(reader);
        } catch (NullPointerException npe) {
            throw new FileNotFoundException("file name cannot be null.");
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    public static XStream createXStream() {
        XStream xstream = new XStream();
        xstream.alias("input", ScriptInput.class);
        xstream.aliasAttribute(BaseRecord.class, "location", "location");
        xstream.aliasAttribute(BaseRecord.class, "language", "language");
        xstream.aliasAttribute(BaseRecord.class, "arguments", "arguments");
        xstream.alias("directory", DirectoryRecord.class);       
        xstream.alias("list", Vector.class);
        xstream.omitField(DirectoryRecord.class, "filenames");
        xstream.omitField(DirectoryRecord.class, "groupId");
        return xstream;
    }

    public static void generateSampleXmlFile(String fileName) throws IOException {
        ScriptInput inputSample = createSample();
        inputSample.writeToFile(fileName);
    }
    
    private static ScriptInput createSample() {
        ScriptInput input = new ScriptInput();

        List<DirectoryRecord> dirs = new Vector<DirectoryRecord>();
        dirs.add(new DirectoryRecord("/path/dir1", "JavaScript", "arg4 arg5 arg6"));
        dirs.add(new DirectoryRecord("/path/dir2", null, null));
        input.setDirectoryList(dirs);
        return input;
    }
}
