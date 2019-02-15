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
package com.solers.delivery.domain;

import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public abstract class BaseValidationTestCase extends TestCase {
    
    @SuppressWarnings("unchecked")
    protected <T> void assertInvalidProperty(T bean, String propertyName) {
        ClassValidator<T> validator = new ClassValidator<T>((Class<T>)bean.getClass());
        InvalidValue [] values = validator.getInvalidValues(bean);
        for (InvalidValue value : values) {
            
            if (value.getPropertyName() != null && value.getPropertyName().equals(propertyName)) {
                return;
            }
        }
        fail(propertyName+" was not an invalid value");
    }
    
    @SuppressWarnings("unchecked")
    protected <T> void assertValidProperty(T bean, String propertyName) {
        ClassValidator<T> validator = new ClassValidator<T>((Class<T>)bean.getClass());
        InvalidValue [] values = validator.getInvalidValues(bean);
        for (InvalidValue value : values) {
            if (value.getPropertyName() != null && value.getPropertyName().equals(propertyName)) {
                fail(propertyName+" was an invalid value: "+value.getMessage());
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    protected <T> void assertValid(T bean) {
        ClassValidator<T> validator = new ClassValidator<T>((Class<T>)bean.getClass());
        InvalidValue [] values = validator.getInvalidValues(bean);
        assertEquals(0, values.length);
    }
    
    @SuppressWarnings("unchecked")
    protected <T> void assertInvalid(T bean) {
        ClassValidator<T> validator = new ClassValidator<T>((Class<T>)bean.getClass());
        InvalidValue [] values = validator.getInvalidValues(bean);
        assertTrue(values.length > 0);
    }
    
}
