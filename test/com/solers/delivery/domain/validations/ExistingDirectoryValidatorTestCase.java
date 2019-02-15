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
package com.solers.delivery.domain.validations;

import java.io.File;

import com.solers.delivery.domain.validations.validators.ExistingDirectoryValidator;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class ExistingDirectoryValidatorTestCase extends TestCase {
    
    public void testIsValid() throws Exception {
        ExistingDirectoryValidator v = new ExistingDirectoryValidator();
        
        assertFalse(v.isValid(null));
        
        File dir = new File("testIsValid");
        assertFalse(dir.exists());
        assertTrue(v.isValid("testIsValid"));
        assertTrue(dir.exists());
        dir.delete();
        
        File f = new File("testIsValid");
        f.createNewFile();
        f.deleteOnExit();
        assertTrue(f.exists());
        assertTrue(v.isValid("testIsValid"));
        f.delete();
    }
    
}
