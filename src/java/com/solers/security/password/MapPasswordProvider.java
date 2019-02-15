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
package com.solers.security.password;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class MapPasswordProvider implements PasswordProvider {

    private final Map<String, String> passwords;
    
    private String defaultPassword;
    
    public MapPasswordProvider() {
        this.passwords = new HashMap<String, String>();
    }
    
    public MapPasswordProvider(Map<String, String> passwords) {
        this();
        this.passwords.putAll(passwords);
    }
    
    public void setDefaultPassword(String defaultPassword) {
        this.defaultPassword = defaultPassword;
    }
    
    @Override
    public String getPassword(String key) {
        String result = passwords.get(key);
        if (result == null) {
            result = defaultPassword;
        }
        return result;
    }

    @Override
    public void setPassword(String key, String password) {
        passwords.put(key, password);
    }

}
