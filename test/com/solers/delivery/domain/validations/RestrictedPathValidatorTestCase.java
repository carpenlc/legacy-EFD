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

import junit.framework.TestCase;

import com.solers.delivery.domain.validations.validators.RestrictedPathValidator;
import com.solers.delivery.util.FileSystemUtil;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class RestrictedPathValidatorTestCase extends TestCase {
    
    public void testIsValid() throws Exception {
        System.setProperty("efd.home", ".");
        
        RestrictedPathValidator v = new RestrictedPathValidator();
        
        assertFalse(v.isValid(null));
        assertFalse(v.isValid(new File(FileSystemUtil.getEFDHome(), "test").getPath()));
        
        assertTrue(v.isValid(new File(FileSystemUtil.getEFDHome().getParentFile().getParentFile().getParentFile(), "test").getPath()));
    }
}
