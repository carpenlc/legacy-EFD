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

import com.solers.delivery.domain.validations.validators.GbsIsEnabledValidator;
import com.solers.delivery.transport.gbs.GBSConfigurator;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class GbsIsEnabledValidatorTestCase extends TestCase {
    
    public void testIsValid() {
        GbsIsEnabledValidator v = new GbsIsEnabledValidator();
        assertTrue(v.isValid(null));
        
        GBSConfigurator.setGBSEnabled(false);
        assertFalse(GBSConfigurator.isGBSEnabled());
        assertTrue(v.isValid(Boolean.FALSE));
        assertFalse(v.isValid(Boolean.TRUE));
        
        GBSConfigurator.setGBSEnabled(true);
        assertTrue(v.isValid(Boolean.TRUE));
    }
    
}
