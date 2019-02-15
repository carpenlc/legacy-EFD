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
package com.solers.delivery.util.password;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class PasswordValidatorTestCase {
    
    @Test
    public void testValidateNonSystemPasswordNonPrintableChar() {  
        try {
            PasswordValidator.validateNonSystemPassword(new char [] {0x00, 'a', 'b'});
            Assert.fail();
        } catch (InvalidPasswordException expected) {
            
        }
    }
    
    @Test
    public void testValidateNonSystemPassword() {  
        try {
            PasswordValidator.validateNonSystemPassword("first".toCharArray());
        } catch (InvalidPasswordException ex) {
            Assert.fail(ex.getMessage());
        }
        
    }

    @Test
    public void testValidateSystemPasswordLength() {
        try {
            PasswordValidator.validateSystemPassword("xxx", "message");
            Assert.fail();
        } catch (InvalidPasswordException expected) {
            
        }
        
        try {
            PasswordValidator.validateSystemPassword("aaaBBB123#$^", "message");
            Assert.fail();
        } catch (InvalidPasswordException expected) {
            
        }
    }

    @Test
    public void testValidateSystemPasswordLowerCase() {
        try {
            PasswordValidator.validateSystemPassword("AAAAAAAAAAAAAAAA", "message");
            Assert.fail();
        } catch (InvalidPasswordException expected) {
            
        }
    }

    @Test
    public void testValidateSystemPasswordUpperCase() {
        try {
            PasswordValidator.validateSystemPassword("aaaaaaaaaaaaaa", "message");
            Assert.fail();
        } catch (InvalidPasswordException expected) {
            
        }
    }
    
    @Test
    public void testValidateSystemPasswordNumerics() {
        try {
            PasswordValidator.validateSystemPassword("AbAbAbCdCdCdCdCdCDEf", "message");
            Assert.fail();
        } catch (InvalidPasswordException expected) {
            
        }
        
        try {
            PasswordValidator.validateSystemPassword("AbAbAbCdCdCdCdCdCDEf1", "message");
            Assert.fail();
        } catch (InvalidPasswordException expected) {
            
        }
    }

    @Test
    public void testValidateSystemPasswordSpecialChars() {
        try {
            PasswordValidator.validateSystemPassword("AbAbAbC124CdCdCdCD153", "message");
            Assert.fail();
        } catch (InvalidPasswordException expected) {
            
        }
    }

    @Test
    public void testValidateSystemPassword() {
        try {
            PasswordValidator.validateSystemPassword("#%bAbAbC124CdCdCdCD153", "message");
        } catch (InvalidPasswordException ex) {
            Assert.fail(ex.getMessage());
        }
        
        try {
            PasswordValidator.validateSystemPassword("#%bAbAbC124CdCdCdCD15", "message", 14);
        } catch (InvalidPasswordException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testValidatePasswordDoesNotContainNull() {
        try {
            PasswordValidator.validatePasswordDoesNotContain("password");
        } catch (InvalidPasswordException ex) {
            Assert.fail(ex.getMessage());
        }
        
        try {
            PasswordValidator.validatePasswordDoesNotContain("password", (String [])null);
        } catch (InvalidPasswordException ex) {
            Assert.fail(ex.getMessage());
        }
        
        try {
            PasswordValidator.validatePasswordDoesNotContain("password", new String[]{});
        } catch (InvalidPasswordException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testValidatePasswordDoesNotContain() {
        try {
            PasswordValidator.validatePasswordDoesNotContain("1234fghtest343535", "test");
            Assert.fail();
        } catch (InvalidPasswordException expected) {
            
        }
        
        try {
            PasswordValidator.validatePasswordDoesNotContain("1234fghtest343535", "abc", "def", "test");
            Assert.fail();
        } catch (InvalidPasswordException expected) {
            
        }
        
        try {
            PasswordValidator.validatePasswordDoesNotContain("1234fghtest343535", "abc", "def", "ghi");
        } catch (InvalidPasswordException ex) {
            Assert.fail(ex.getMessage());
        }
        
        try {
            PasswordValidator.validatePasswordDoesNotContain("1234fghtest343535", "");
        } catch (InvalidPasswordException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testLessThanMinNumberOfAlphaNumerics() {
        String password = "aaa";
        try {
            PasswordValidator.validateSystemPassword(password, "message");
            Assert.fail();
        } catch (InvalidPasswordException expected) {
            
        }
    }

    @Test
    public void testMinNumberOfAlphaNumerics() {
        String password = "aaBB12##aaBB12##";
        try {
            PasswordValidator.validateSystemPassword(password, "message");
            
        } catch (InvalidPasswordException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testContainsUserName() {
        String password = "aaBB12##UseRaaBB12##";
        
        try {
            PasswordValidator.validatePasswordDoesNotContain(password, "first","last","user");
            Assert.fail();
        } catch (InvalidPasswordException expected) {
            
        }
    }

    @Test
    public void testContainsFirstName() {
        String password = "aaBB12##FirsTaaBB12##";
        try {
            PasswordValidator.validatePasswordDoesNotContain(password, "first","last","user");
            Assert.fail();
        } catch (InvalidPasswordException expected) {
            
        }
    }

    @Test
    public void testContainsLastName() {
        String password = "aaBB12##LastaaBB12##";
        try {
            PasswordValidator.validatePasswordDoesNotContain(password, "first","last","user");
            Assert.fail();
        } catch (InvalidPasswordException expected) {
            
        }
    }
}
