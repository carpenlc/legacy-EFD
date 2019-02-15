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

import java.util.HashMap;
import java.util.Map;

import org.apache.tools.ant.Task;

public class ConfigClassificationTask extends Task {
    private String securityLevel;
    private String securityLabel = null;
    private String bgColor = null;
    private Map<String, String> allLabels;
    private Map<String, String> allColors;
    
    public void setSecurityLevel(String level) {
        this.securityLevel = level;
    }
    
    public void setSecurityLabel(String label) {
        this.securityLabel = label;
    }
    
    public void setBgColor(String color) {
        this.bgColor = color;
    }
    
    public void setLabels(String labels) {
        allLabels = new HashMap<String, String>();
        convertToList(labels, allLabels);
    }
    
    public void setColors(String colors) {
        allColors = new HashMap<String, String>();
        convertToList(colors, allColors);
    }

    public void execute() {
        getProject().setNewProperty(securityLabel, allLabels.get(securityLevel));
        getProject().setNewProperty(bgColor, allColors.get(securityLevel));
    }
    
    private void convertToList(String value, Map<String, String> newList) {
        if (value != null) {
            String[] strArray  = value.split(",");
            int index = 1; 
            for (String label : strArray) {
                newList.put(String.valueOf(index), label.toUpperCase().trim());
                index++;
            }
        }
    }
}

