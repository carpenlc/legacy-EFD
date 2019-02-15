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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * This class contains the mapping of extension to script languages
 */
public class ScriptConfiguration {
    private final Map<String, ScriptLanguage> map;
    private final String defaultLanguage;

    public ScriptConfiguration(List<ScriptLanguage> languageList, String defaultLanguage) {
        Hashtable<String, ScriptLanguage> table = new Hashtable<String, ScriptLanguage>();
        if (languageList != null) {
            for (ScriptLanguage scriptLanguage : languageList) {
                table.put(scriptLanguage.getLanguage(), scriptLanguage);
            }
        }
        map = Collections.unmodifiableMap(table);
        this.defaultLanguage = defaultLanguage;
    }

    /**
     * This method check if a language is supported by the scripting engine
     * 
     * @param language
     */
    public boolean isSupported(String language) {
        boolean supported = false;
        if (language != null) {
            supported = map.get(language) != null;
        }
        return supported;
    }

    /**
     * This method allows to get the language from the file extension. It will first try to get from the map. If noting found, will return the defaultLanguage
     * 
     * @return
     */
    public String getLanguageFromExtension(String extension) {
        String language = null;
        Collection<ScriptLanguage> scriptLanguages = map.values();
        for (ScriptLanguage scriptLanguage : scriptLanguages) {
            if (scriptLanguage.hasExtension(extension)) {
                language = scriptLanguage.getLanguage();
                break;
            }
        }
        if (language == null) {
            language = defaultLanguage;
        }
        return language;
    }

    public Map<String, ScriptLanguage> getMap() {
        return map;
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public static class ScriptLanguage {
        private String language;
        private List<String> extensionsList;
        private String description;

        public ScriptLanguage() {
            this("", "", "");
        }

        public ScriptLanguage(String language, String extensions, String description) {
            super();
            this.language = language;
            this.extensionsList = parseExtensions(extensions);
            this.description = description;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public void setExtensions(String extensions) {
            this.extensionsList = parseExtensions(extensions);
        }

        public boolean hasExtension(String extension) {
            return extensionsList.contains(extension);
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        private List<String> parseExtensions(String extensions) {
            List<String> list = new ArrayList<String>();
            if (extensions != null) {
                String[] extArray = extensions.split(",");
                list.addAll(Arrays.asList(extArray));
            }
            return list;
        }
    }
}
