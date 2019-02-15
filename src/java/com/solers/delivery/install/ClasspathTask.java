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
package com.solers.delivery.install;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class ClasspathTask extends Task {
    
    private final List<Entry> entries;
    
    private String format;
    private String property;
    
    public ClasspathTask() {
        entries = new ArrayList<Entry>();
    }
    
    public void addEntry(Entry entry) {
        entries.add(entry);
    }
    
    public Entry createEntry() {
        Entry e = new Entry();
        entries.add(e);
        return e;
    }
    
    public void setFormat(String arg) {
        format = arg;
    }
    
    public void setProperty(String arg) {
        property = arg;
    }
    
    @Override
    public void execute() throws BuildException {
        String result = null;
        
        if (isUnix()) {
            result = unix();
        } else if (isWrapper()) {
            result = wrapper();
        } else {
            result = windows();
        }
        
        getProject().setProperty(property, result);
    }
    
    private boolean isUnix() {
        return format.equals("unix");
    }
    
    private boolean isWrapper() {
        return format.equals("wrapper");
    }
    
    private String wrapper() {
        StringBuilder result = new StringBuilder();
        int count = 1;
        for (Entry e : entries) {
            for (String s : e) {
                result.append(String.format("wrapper.java.classpath.%d=", count++));
                result.append(s);
                result.append("\r\n");
            }
        }
        return result.toString();
    }
    
    private String unix() {
        return buildPath(':');
    }
    
    private String windows() {
        return buildPath(';');
    }
    
    private String buildPath(char separator) {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Entry e : entries) {
            for (String s : e) {
                if (first) {
                    first = false;
                } else {
                    result.append(separator);
                }
                result.append(s);
            }
        }
        return result.toString();
    }
    
    public static class Entry implements Iterable<String> {
        
        private final List<Path> paths;
        
        private String prefix;
        private String value;
        
        public Entry() {
            paths = new ArrayList<Path>();
        }
        
        public void addPath(Path path) {
            paths.add(path);
        }
        
        public void setPrefix(String arg) {
            prefix = arg;
        }
        
        public void setValue(String arg) {
            value = arg;
        }
        
        public Iterator<String> iterator() {
            List<String> result = new ArrayList<String>();
            if (value == null) {
                for (Path path : paths) {
                    for (String s : path.list()) {
                        File f = new File(s);
                        if (prefix == null) {
                            result.add(f.getName());
                        } else {
                            result.add(prefix+f.getName());
                        }
                    }
                }
            } else {
                result.add(value);
            }
            return result.iterator();
        }
    }
}
