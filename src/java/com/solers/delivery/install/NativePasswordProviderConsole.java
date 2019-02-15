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

import com.solers.delivery.tools.ClTools;
import com.solers.security.password.DPAPIPasswordProvider;
import com.solers.security.password.PasswordProvider;
import com.solers.util.IOConsole;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class NativePasswordProviderConsole extends PasswordProviderConsole {
    
    @Override
    public PasswordProvider getInstance(char[] masterPassword, File file) {
        return new DPAPIPasswordProvider(masterPassword, file);
    }

    public static void main(String[] args) {
        ClTools.initializeJsafeProvider();
        IOConsole console = IOConsole.DEFAULT;
        
        if (creating(args)) {
            create(new NativePasswordProviderConsole(), args[0], console);
        } else {
            usage(console);
        }
    }
    
    private static void usage(IOConsole console) {
        console.println("Usage: filename create");
    }
}
