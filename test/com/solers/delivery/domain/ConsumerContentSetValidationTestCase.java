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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;
import java.util.Set;
import java.util.HashSet;

import com.solers.util.unit.TimeIntervalUnit;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class ConsumerContentSetValidationTestCase extends BaseValidationTestCase {
    
    public void setUp() {
        System.setProperty("efd.home", ".");
    }
    
    @SuppressWarnings("unchecked")
    public void testDuplicateValidations() {
        ConsumerContentSet contentSet = new ConsumerContentSet();
        ClassValidator<ConsumerContentSet> validator = new ClassValidator<ConsumerContentSet>((Class<ConsumerContentSet>)contentSet.getClass());
        InvalidValue [] values = validator.getInvalidValues(contentSet, "path");
        
        assertEquals(6, values.length);
    }
    
    public void testValidConsumerContentSet() throws Exception {
        ConsumerContentSet contentSet = getContentSet();           
        
        List<FileFilter> filters = new ArrayList<FileFilter>();
        contentSet.setFileFilters(filters);
        FileFilter filter = new FileFilter("", FileFilter.Pattern.BEGINS);
        filter.setPattern("pattern");
        filters.add(filter);
        
        Set<ScheduleExpression> expressions = new HashSet<ScheduleExpression>();
        expressions.add(new ScheduleExpression("0 0/2 * * * ?"));
        contentSet.setScheduleExpressions(expressions);
        assertValid(contentSet);
    }
    
    public void testValidFileFilters() {
        ConsumerContentSet contentSet = new ConsumerContentSet();
        List<FileFilter> filters = new ArrayList<FileFilter>();
        FileFilter filter = new FileFilter("", FileFilter.Pattern.BEGINS);
        
        assertValidProperty(contentSet, "pattern");
        
        contentSet.setFileFilters(filters);
        
        assertValidProperty(contentSet, "pattern");
        
        filters.add(filter);
        
        assertInvalidProperty(contentSet, "pattern");
    }
    
    public void testValidSupplierName() {
        ConsumerContentSet contentSet = new ConsumerContentSet();
        
        assertInvalidProperty(contentSet, "supplierName");
        contentSet.setSupplierName("foo");
        assertValidProperty(contentSet, "supplierName");
    }
    
    public void testValidSupplierAddress() {
        ConsumerContentSet contentSet = new ConsumerContentSet();
        assertInvalidProperty(contentSet, "supplierAddress");
        contentSet.setSupplierAddress("foo");
        assertValidProperty(contentSet, "supplierAddress");
    }
    
    public void testValidSupplierPort() {
        ConsumerContentSet contentSet = new ConsumerContentSet();
        assertInvalidProperty(contentSet, "supplierPort");
        contentSet.setSupplierPort(-1);
        assertInvalidProperty(contentSet, "supplierPort");
        contentSet.setSupplierPort(0);
        assertInvalidProperty(contentSet, "supplierPort");
        contentSet.setSupplierPort(1);
        assertValidProperty(contentSet, "supplierPort");
    }
    
    public void testValidFileDeleteDelay() {
        ConsumerContentSet contentSet = new ConsumerContentSet();
        contentSet.setFileDeleteDelay(-1);
        assertInvalidProperty(contentSet, "fileDeleteDelay");
        contentSet.setFileDeleteDelay(0);
        assertValidProperty(contentSet, "fileDeleteDelay");
    }
    
    public void testValidMaxFileSize() {
        ConsumerContentSet contentSet = new ConsumerContentSet();
        contentSet.setMaxFileSize(-1);
        assertInvalidProperty(contentSet, "maxFileSize");
        contentSet.setMaxFileSize(0);
        assertValidProperty(contentSet, "maxFileSize");
    }
    
    public void testValidScheduleExpression() throws Exception {
        ConsumerContentSet contentSet = new ConsumerContentSet();
        assertInvalid(contentSet);
              
        ScheduleExpression expression = new ScheduleExpression("0 0/2 * * * *");
        contentSet.addScheduleExpression(expression);
        assertInvalid(contentSet);
        
        contentSet = getContentSet();
        expression.setDuration(-1);
        expression.setDurationUnit(TimeIntervalUnit.HOURS);
        contentSet.addScheduleExpression(expression);
        assertInvalid(contentSet);
        
        expression.setCronExpression("0 0/2 * * * ?");
        expression.setDuration(20);
        expression.setDurationUnit(TimeIntervalUnit.MINUTES);
        assertValid(contentSet);          
    }
    
    private ConsumerContentSet getContentSet() throws Exception {
        ConsumerContentSet contentSet = new ConsumerContentSet();
        File validPath = File.createTempFile("testValidConsumerContentSet", "data");
        validPath.deleteOnExit();
        
        contentSet.setName("valid-name");
        contentSet.setPath(validPath.getAbsolutePath());
        contentSet.setInventoryInterval(1);
        
        contentSet.setSupplierAddress("addr");
        contentSet.setSupplierPort(21);
        contentSet.setSupplierName("Foo");
        return contentSet;
    }
}
