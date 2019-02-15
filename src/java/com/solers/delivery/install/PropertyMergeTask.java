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

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class PropertyMergeTask extends Task {
    
    private String old;
    private String current;
    private String result;
    
    public void setOld(String old) {
        this.old = old;
    }
    
    public void setCurrent(String current) {
        this.current = current;
    }
    
    public void setResult(String result) {
        this.result = result;
    }
    
    @Override
    public void execute() throws BuildException {
        Properties old = load(this.old);
        Properties current = load(this.current);
        Properties result = new Properties();
        
        for (String key : current.stringPropertyNames()) {
            if (old.containsKey(key)) {
                result.put(key, old.getProperty(key));
            } else {
                result.put(key, current.getProperty(key));
            }
        }
        
        // KRJ 2016-08-26: Converted writer creation to "try with resources"
        // based on an HP Fortify recommendation
        
        try (FileWriter writer = new FileWriter(this.result)) {
            result.store(writer, null);
        } 
        catch (IOException ex) {
            throw new BuildException(ex.getMessage(), ex);
        }
    }
    
    private Properties load(String fileName) {
        Properties result = new Properties();
        
        // KRJ 2016-08-31: Converted reader creation to "try with resources"
        // based on an HP Fortify recommendation
        
        try (FileReader r = new FileReader(fileName)) {
            result.load(r);
        } catch (IOException ex) {
            throw new BuildException(ex.getMessage(), ex);
        }
        return result;
    }
}
