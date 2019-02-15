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
package com.solers.delivery.security;

public enum PasswordType {

    // Do not change the "key" values in order
    // to preserve backwards compatibility. They end
    // up being encrypted anyway so it really doesn't matter
    // what they say, as long as they are the same between
    // releases
    ROOT_DATABASE("root.database", "Root Database", true), 
    DATABASE("delivery.database", "Application Database", true), 
    PKI_KEY("ssl", "PKI Private Key", false), 
    PKI_KEYSTORE("ssl.keystore", "PKI Key Store", false),
    PKI_TRUSTSTORE("ssl.truststore", "PKI Trust Store", false), 
    FTP("ftp", "FTP(S) Server", true),
    ENCRYPTION("derbyKey", "n/a", false);

    private final String key;

    private final String prompt;

    // needs to abide by admin password enforcements
    private final boolean isSystemPassword;

    PasswordType(String key, String prompt, boolean isSystemPassword) {
        this.key = key;
        this.prompt = prompt;
        this.isSystemPassword = isSystemPassword;
    }

    public String key() {
        return key;
    }

    public String prompt() {
        return prompt;
    }

    public boolean isSystemPassword() {
        return isSystemPassword;
    }
    
    public static PasswordType keyValueOf(String key) {
        for (PasswordType type : values()) {
            if (key.equals(type.key())) {
                return type;
            }
        }
        throw new IllegalArgumentException(key+" is not a valid PasswordType");
    }

    @Override
    public String toString() {
        return key();
    }
}
