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
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.solers.delivery.security.PasswordType;
import com.solers.delivery.tools.ClTools;
import com.solers.delivery.util.password.InvalidPasswordException;
import com.solers.delivery.util.password.PasswordValidator;
import com.solers.security.password.DefaultPasswordProvider;
import com.solers.security.password.PasswordProvider;
import com.solers.util.IOConsole;

public class PasswordProviderConsole {

    private static String PASSWORD_MESSAGE = "Password must contain at least %d characters, at least 1 upper case letter, 1 lower case letter, and 1 non-alphanumeric character.";
    private static final int ARGS_CREATE = 2;
    private static final int ARGS_CHECK = 3;
    
    /**
     * Retrieves password and validates it
     * 
     * @param cons
     * @param prompt
     * @return
     */
    public static char[] readPassword(IOConsole cons, String prompt, boolean isSystemPassword) {
        while (true) {
            char[] password = cons.readPassword(prompt, true);

            try {
                PasswordValidator.validateNonSystemPassword(password);
                if (isSystemPassword) {
                    PasswordValidator.validateSystemPassword(String.valueOf(password), PASSWORD_MESSAGE);
                }
                return Arrays.copyOf(password, password.length);
            } catch (InvalidPasswordException ex) {
                System.err.println(ex.getMessage());
                continue;
            } finally {
                if (password != null) {
                    Arrays.fill(password, ' ');
                }
            }
        }
    }
    
    public static void main(String[] args) {
        ClTools.initializeJsafeProvider();
        IOConsole console = IOConsole.DEFAULT;
        PasswordProviderConsole providerConsole = new PasswordProviderConsole();

        if (creating(args)) {
            create(providerConsole, args[0], console);
        } else if (checking(args)) {
            check(providerConsole, args[0], args[2].toCharArray(), console);
        } else {
            usage(console);
        }
    }

    protected static void create(PasswordProviderConsole providerConsole, String filename, IOConsole console) {
        try {
            providerConsole.createPasswordFile(filename, console);
        } catch (IOException ex) {
            console.println("An error occurred creating the password file: " + ex.getMessage());
        }
    }

    protected static boolean creating(String[] args) {
        return args != null && args.length == ARGS_CREATE && args[1].equalsIgnoreCase("create");
    }

    private static void check(PasswordProviderConsole providerConsole, String filename, char[] password, IOConsole console) {
        boolean result = providerConsole.verifyMasterPassword(filename, password, console);
        console.println(result ? "Password Verified" : "Password Not Verified");
    }

    private static void usage(IOConsole console) {
        console.println("Usage: filename [create|check] <password>");
    }

    private static boolean checking(String[] args) {
        return args != null && args.length == ARGS_CHECK && args[1].equalsIgnoreCase("check");
    }
    
    //-------

    public PasswordProvider createPasswordFile(String passwordFile, IOConsole console) throws IOException {
        PasswordProvider provider = initialize(console, passwordFile);
        if (provider == null) {
            return null;
        }

        Map<String, String> passwordMap = getPasswords(console);
        for (Map.Entry<String, String> entry : passwordMap.entrySet()) {
            provider.setPassword(entry.getKey(), entry.getValue());
        }

        return provider;
    }

    protected PasswordProvider createNewProvider(IOConsole console, File file) {
        char[] password = readPassword(console, "Master", true);
        return getInstance(password, file);
    }
    
    protected PasswordProvider getCurrentProvider(IOConsole console, File file) {
        char [] masterPassword = console.readPassword("Current Master", false);
        if (verifyMasterPassword(file.getAbsolutePath(), masterPassword, console)) {
            DefaultPasswordProvider result = (DefaultPasswordProvider) getInstance(masterPassword, file);
            char[] newmaster = readPassword(console, "New Master", true);
            result.initialize(newmaster);
            return result;
        }
        console.println("Incorrect master password");
        return null;
    }

    private Map<String, String> getPasswords(IOConsole console) {
        PasswordType [] types = new PasswordType [] { 
            PasswordType.ROOT_DATABASE, 
            PasswordType.DATABASE,
            PasswordType.PKI_KEY,
            PasswordType.PKI_KEYSTORE,
            PasswordType.PKI_TRUSTSTORE,
            PasswordType.FTP
        };
        Map<String, String> passwords = new HashMap<String, String>(types.length);
        for (PasswordType password : types) {
            passwords.put(password.key(), new String(readPassword(console, password.prompt(), password.isSystemPassword())));
        }
        return Collections.unmodifiableMap(passwords);
    }
    
    private PasswordProvider initialize(IOConsole console, String passwordFile) throws IOException {
        if (passwordFile == null || passwordFile.trim().length() == 0) {
            console.println("Please provide the password filename to create");
            return null;
        }

        File file = new File(passwordFile);
        if (file.exists()) {
            return getCurrentProvider(console, file);
        }

        console.println("Creating password file: " + file.getAbsolutePath());
        if (file.createNewFile()) {
            console.println("Successfully created password file: " + file.getAbsolutePath());
            return createNewProvider(console, file);
        } else {
            console.println("Problem creating password file: " + passwordFile);
            return null;
        }
    }
    
    /**
     * Checks Master Password to make sure it is correct
     * 
     * @param passwordFile -
     *            password file
     * @param masterPassword -
     *            master password
     */
    public boolean verifyMasterPassword(String passwordFile, char[] masterPassword, IOConsole console) {
        File pFile = new File(passwordFile);
        if (checkPassword(masterPassword) && pFile.exists() && pFile.canRead()) {
            PasswordProvider encProps = null;

            try {
                encProps = getInstance(masterPassword, pFile);
            } catch (RuntimeException e) {
                console.println(e.getMessage());
                return false;
            }

            return encProps != null && encProps.getPassword(PasswordType.DATABASE.key()) != null;
        } else {
            console.println("Cannot retreive the Password File: " + pFile.getAbsolutePath());
        }
        return false;
    }
    
    private boolean checkPassword(char[] password) {
        String pw = new String(password);
        boolean valid = (pw != null && pw.trim().length() > 0);
        if (!valid) {
            System.err.println("Password cannot be null or empty!");
        }
        return valid;
    }
    
    public PasswordProvider getInstance(char [] masterPassword, File file) {
        return new DefaultPasswordProvider(masterPassword, file);
    }
}
