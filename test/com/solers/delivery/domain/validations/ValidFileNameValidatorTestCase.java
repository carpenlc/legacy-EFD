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

import com.solers.delivery.domain.validations.validators.ValidFileNameValidator;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class ValidFileNameValidatorTestCase extends TestCase {
    
    /**
     * These tests should only run on windows because unix pathnames can pretty much
     * contain anything
     */
    public void testIsValid() {
        if (System.getProperty("os.name").contains("Windows")) {
            ValidFileNameValidator v = new ValidFileNameValidator();
            
            assertFalse(v.isValid(null));
            assertFalse(v.isValid("c:\\invalid:"));
            assertTrue(v.isValid("c:\\valid"));
        }
    }
}
