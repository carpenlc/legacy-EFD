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

import junit.framework.TestCase;

import org.hibernate.validator.Validator;

import com.solers.delivery.domain.FileFilter;
import com.solers.delivery.domain.validations.validators.ValidFilterValidator;

public class ValidFilterValidatorTest extends TestCase {
    public void testValidator() {
        FileFilter invalid = new FileFilter("*", FileFilter.Pattern.REGEX);
        FileFilter valid = new FileFilter(".*", FileFilter.Pattern.REGEX);
        FileFilter validB = new FileFilter("*", FileFilter.Pattern.BEGINS);
        FileFilter validC = new FileFilter("*", FileFilter.Pattern.CONTAINS);
        FileFilter validE = new FileFilter("*", FileFilter.Pattern.ENDS);
        
        Validator<ValidFilter> v = new ValidFilterValidator();
        assertFalse(v.isValid(invalid));
        assertTrue(v.isValid(valid));
        assertTrue(v.isValid(validB));
        assertTrue(v.isValid(validC));
        assertTrue(v.isValid(validE));
    }
    
    public void testNullArgument() {
        Validator<ValidFilter> v = new ValidFilterValidator();
        assertFalse(v.isValid(null));
    }
}
