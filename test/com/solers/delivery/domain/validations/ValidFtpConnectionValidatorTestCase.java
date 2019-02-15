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

import com.solers.delivery.domain.ConsumerContentSet;
import com.solers.delivery.domain.ContentSet;
import com.solers.delivery.domain.FtpConnection;
import com.solers.delivery.domain.validations.validators.ValidFtpConnectionValidator;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class ValidFtpConnectionValidatorTestCase extends TestCase {
    
    public void testIsValid() {
        ValidFtpConnectionValidator v = new ValidFtpConnectionValidator();
        
        assertFalse(v.isValid(null));
        assertFalse(v.isValid("not a contentset"));
        
        ContentSet c = new ContentSet();
        c.setSupplier(true);
        c.setSupportsGbsTransport(false);
        c.setFtpConnection(null);
  
        assertTrue(v.isValid(c));
        
        c.setSupportsGbsTransport(true);
        assertFalse(v.isValid(c));
        
        c.setFtpConnection(new FtpConnection());
        assertTrue(v.isValid(c));
        
        c.setSupportsGbsTransport(false);
        assertFalse(v.isValid(c));
        
        ConsumerContentSet consumer = new ConsumerContentSet();
        consumer.setSupplier(false);
        consumer.setSupportsGbsTransport(false);
        assertTrue(v.isValid(consumer));
        
        consumer.setSupportsGbsTransport(true);
        assertTrue(v.isValid(consumer));
    }
    
}
