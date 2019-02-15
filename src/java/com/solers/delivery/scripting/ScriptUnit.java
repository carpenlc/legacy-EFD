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
package com.solers.delivery.scripting;

import java.io.File;

/**
 * Contains information for a script
 */
public class ScriptUnit {
    private final String id;
    private final String groupId;
    private final File file;
    private final String scriptLanguage;
    private final String arguments;

    public ScriptUnit(String id, String groupId, File file, String scriptLanguage, String args) {
        super();
        this.id = id;
        this.groupId = groupId;
        this.file = file;
        this.scriptLanguage = scriptLanguage;
        this.arguments = args;
    }

    public String getId() {
        return id;
    }

    public String getGroupId() {
        return groupId;
    }

    public File getFile() {
        return file;
    }

    public String getScriptLanguage() {
        return scriptLanguage;
    }    

    public String getArguments() {
        return arguments;
    }

    public String toString() {
        String fileName = file != null ? file.getAbsolutePath() : "null";
        return String.format("Id: %s \nFile: %s \nLanguage: %s", id, fileName, scriptLanguage);
    }
}
