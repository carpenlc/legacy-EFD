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
package com.solers.delivery.inventory;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

public class MockUtil {
    public static URL createResource(String data) {
        try {
            File f = File.createTempFile("resource", ".tmp");
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(data.getBytes());
            fos.flush();
            fos.close();
            f.deleteOnExit();
            return f.toURI().toURL();
        } catch (Exception e) {
            return null;
        }
    }
}
