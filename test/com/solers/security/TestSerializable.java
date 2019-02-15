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
/**
 * 
 */
package com.solers.security;

import java.io.Serializable;

/**
 * @author mchen
 *
 */
public class TestSerializable implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -2812442728236468969L;
    private int foo;
    private String bar;
    
    public TestSerializable(int foo, String bar) {
        this.foo = foo;
        this.bar = bar;
    }
    
    public int getFoo() {
        return this.foo;
    }
    
    public String getBar() {
        return this.bar;
    }
    
    public boolean equals(Object obj) {
        if (!(obj instanceof TestSerializable)) {
            return false;
        }
        
        TestSerializable test = (TestSerializable)obj;
        return this.getBar().equals(test.getBar()) && this.getFoo() == test.getFoo();
    }
    
    public int hashcode() {
        int result = 17;
        result = 31 * result + this.getFoo();
        result = 31 * result + this.getBar().hashCode();
        return result;
    }
}
