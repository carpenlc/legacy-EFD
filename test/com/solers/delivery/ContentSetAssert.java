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
package com.solers.delivery;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import com.solers.delivery.domain.AllowedHost;
import com.solers.delivery.domain.ConsumerContentSet;
import com.solers.delivery.domain.ContentSet;
import com.solers.delivery.domain.FileFilter;
import com.solers.delivery.domain.FtpConnection;
import com.solers.delivery.domain.ScheduleExpression;
import com.solers.delivery.domain.FileFilter.Pattern;
import com.solers.delivery.util.FileSystemUtil;
import com.solers.util.unit.FileSizeUnit;
import com.solers.util.unit.TimeIntervalUnit;

public class ContentSetAssert {
    
    public static final String CONTENT_SET_JSON_WITH_ID = "{\"supplierContentSet\":{\"allowedHosts\":{\"allowedHost\":{\"alias\":\"testAlias\",\"commonName\":\"testCommonName\"}},\"creationTime\":\"200\",\"description\":\"description\",\"enabled\":\"true\",\"filters\":\"\",\"ftpConnection\":{\"directory\":\"directory\",\"host\":\"localhost\",\"passive\":\"true\",\"password\":\"password\",\"port\":\"21\",\"username\":\"username\"},\"id\":\"1\",\"inventoryInterval\":\"100\",\"inventoryIntervalUnit\":\"SECONDS\",\"name\":\"name\",\"path\":\"path\",\"supplier\":\"true\",\"supportsGbsTransport\":\"true\",\"scheduleExpressions\":{\"scheduleExpression\":{\"cronExpression\":\"0 0/2 * * * ?\",\"duration\":\"10\",\"durationUnit\":\"MINUTES\"}}}}";
    public static final String CONSUMER_CONTENT_SET_JSON_WITH_ID = "{\"consumerContentSet\":{\"creationTime\":\"200\",\"description\":\"description\",\"enabled\":\"true\",\"fileDeleteDelay\":\"100\",\"fileDeleteDelayUnit\":\"DAYS\",\"filters\":{\"filter\":{\"id\":\"1\",\"pattern\":\"abc\",\"patternType\":\"BEGINS\"}},\"id\":\"1\",\"inventoryInterval\":\"100\",\"inventoryIntervalUnit\":\"SECONDS\",\"maxFileSize\":\"100\",\"maxFileSizeUnit\":\"MEGABYTES\",\"name\":\"name\",\"path\":\"path\",\"supplier\":\"false\",\"supplierAddress\":\"localhost\",\"supplierName\":\"Supplier\",\"supplierPort\":\"443\",\"supportsGbsTransport\":\"true\",\"scheduleExpressions\":{\"scheduleExpression\":{\"cronExpression\":\"0 0/2 * * * ?\",\"duration\":\"10\",\"durationUnit\":\"MINUTES\"}}}}";
    public static final String CONTENT_SET_JSON_WITHOUT_ID = "{\"supplierContentSet\":{\"allowedHosts\":{\"allowedHost\":{\"alias\":\"testAlias\",\"commonName\":\"testCommonName\"}},\"creationTime\":\"200\",\"description\":\"description\",\"enabled\":\"true\",\"filters\":\"\",\"ftpConnection\":{\"directory\":\"directory\",\"host\":\"localhost\",\"passive\":\"true\",\"password\":\"password\",\"port\":\"21\",\"username\":\"username\"},\"inventoryInterval\":\"100\",\"inventoryIntervalUnit\":\"SECONDS\",\"name\":\"name\",\"path\":\"path\",\"supplier\":\"true\",\"supportsGbsTransport\":\"true\",\"scheduleExpressions\":{\"scheduleExpression\":{\"cronExpression\":\"0 0/2 * * * ?\",\"duration\":\"10\",\"durationUnit\":\"MINUTES\"}}}}";
    public static final String CONSUMER_CONTENT_SET_JSON_WITHOUT_ID = "{\"consumerContentSet\":{\"creationTime\":\"200\",\"description\":\"description\",\"enabled\":\"true\",\"fileDeleteDelay\":\"100\",\"fileDeleteDelayUnit\":\"DAYS\",\"filters\":{\"filter\":{\"id\":\"1\",\"pattern\":\"abc\",\"patternType\":\"BEGINS\"}},\"inventoryInterval\":\"100\",\"inventoryIntervalUnit\":\"SECONDS\",\"maxFileSize\":\"100\",\"maxFileSizeUnit\":\"MEGABYTES\",\"name\":\"name\",\"path\":\"path\",\"supplier\":\"false\",\"supplierAddress\":\"localhost\",\"supplierName\":\"Supplier\",\"supplierPort\":\"443\",\"supportsGbsTransport\":\"true\",\"scheduleExpressions\":{\"scheduleExpression\":{\"cronExpression\":\"0 0/2 * * * ?\",\"duration\":\"10\",\"durationUnit\":\"MINUTES\"}}}}";
    
    private static final String SCHEDULE_EXPRESSIONS =
        "<scheduleExpressions>" +
            "<scheduleExpression>" +
                "<id>1</id>" +
                "<cronExpression>0 0/2 * * * ?</cronExpression>" +
                "<duration>10</duration>" +
                "<durationUnit>MINUTES</durationUnit>" +
            "</scheduleExpression>" +
        "</scheduleExpressions>";
    
    public static final String CONTENT_SET_XML_EXISTING = 
        "<supplierContentSet>" +
            "<creationTime>200</creationTime>" +
            "<description>description</description>" +
            "<enabled>true</enabled>" +
            "<filters/>" +
            "<id>1</id>" +
            "<inventoryInterval>100</inventoryInterval>" +
            "<inventoryIntervalUnit>SECONDS</inventoryIntervalUnit>" +
            "<name>name</name>" +
            "<path>path</path>" +
            "<supplier>true</supplier>" +
            "<supportsGbsTransport>true</supportsGbsTransport>" +
            "<ftpConnection>" +
                "<directory>directory</directory>" +
                "<host>localhost</host>" +
                "<passive>true</passive>" +
                "<password>password</password>" +
                "<port>21</port>" +
                "<username>username</username>" +
            "</ftpConnection>" +
            "<allowedHosts>" +
                "<allowedHost>" +
                  "<alias>testAlias</alias>" +
                  "<commonName>testCommonName</commonName>" +
                "</allowedHost>" +
            "</allowedHosts>" +
            SCHEDULE_EXPRESSIONS +
        "</supplierContentSet>";
    
    public static final String CONSUMER_CONTENT_SET_XML_EXISTING = 
        "<consumerContentSet>" +
            "<creationTime>200</creationTime>" +
            "<description>description</description>" +
            "<enabled>true</enabled>" +
            "<fileDeleteDelay>100</fileDeleteDelay>" +
            "<fileDeleteDelayUnit>DAYS</fileDeleteDelayUnit>" +
            "<filters>" +
              "<filter>" +
                "<id>1</id>" +
                "<pattern>abc</pattern>" +
                "<patternType>BEGINS</patternType>" +
              "</filter>" +
            "</filters>" +
            "<id>1</id>" +
            "<inventoryInterval>100</inventoryInterval>" +
            "<inventoryIntervalUnit>SECONDS</inventoryIntervalUnit>" +
            "<maxFileSize>100</maxFileSize>" +
            "<maxFileSizeUnit>MEGABYTES</maxFileSizeUnit>" +
            "<name>name</name>" +
            "<path>path</path>" +
            "<supplier>false</supplier>" +
            "<supplierAddress>localhost</supplierAddress>" +
            "<supplierName>Supplier</supplierName>" +
            "<supplierPort>443</supplierPort>" +
            "<supportsGbsTransport>true</supportsGbsTransport>" +
            SCHEDULE_EXPRESSIONS +
        "</consumerContentSet>";
    
    public static final String CONTENT_SET_XML_NEW = 
        "<supplierContentSet>" +
            //"<creationTime>200</creationTime>" +
            "<description>description</description>" +
            "<enabled>true</enabled>" +
            "<filters/>" +
            "<inventoryInterval>100</inventoryInterval>" +
            "<inventoryIntervalUnit>SECONDS</inventoryIntervalUnit>" +
            "<name>name</name>" +
            "<path>path</path>" +
            "<supplier>true</supplier>" +
            "<supportsGbsTransport>true</supportsGbsTransport>" +
            "<ftpConnection>" +
                "<directory>directory</directory>" +
                "<host>localhost</host>" +
                "<passive>true</passive>" +
                "<password>password</password>" +
                "<port>21</port>" +
                "<username>username</username>" +
            "</ftpConnection>" +
            "<allowedHosts>" +
                "<allowedHost>" +
                  "<alias>testAlias</alias>" +
                  "<commonName>testCommonName</commonName>" +
                "</allowedHost>" +
            "</allowedHosts>" +
            SCHEDULE_EXPRESSIONS +
        "</supplierContentSet>";
    
    public static final String CONSUMER_CONTENT_SET_XML_NEW = 
        "<consumerContentSet>" +
            //"<creationTime>200</creationTime>" +
            "<description>description</description>" +
            "<enabled>true</enabled>" +
            "<fileDeleteDelay>100</fileDeleteDelay>" +
            "<fileDeleteDelayUnit>DAYS</fileDeleteDelayUnit>" +
            "<filters>" +
              "<filter>" +
                "<id>1</id>" +
                "<pattern>abc</pattern>" +
                "<patternType>BEGINS</patternType>" +
              "</filter>" +
            "</filters>" +
            "<inventoryInterval>100</inventoryInterval>" +
            "<inventoryIntervalUnit>SECONDS</inventoryIntervalUnit>" +
            "<maxFileSize>100</maxFileSize>" +
            "<maxFileSizeUnit>MEGABYTES</maxFileSizeUnit>" +
            "<name>name</name>" +
            "<path>path</path>" +
            "<supplier>false</supplier>" +
            "<supplierAddress>localhost</supplierAddress>" +
            "<supplierName>Supplier</supplierName>" +
            "<supplierPort>443</supplierPort>" +
            "<supportsGbsTransport>true</supportsGbsTransport>" +
            SCHEDULE_EXPRESSIONS + 
        "</consumerContentSet>";
    
    private static ScheduleExpression getScheduleExpression() {
        ScheduleExpression expression = new ScheduleExpression("0 0/2 * * * ?");
        expression.setDuration(10);
        expression.setDurationUnit(TimeIntervalUnit.MINUTES);
        return expression;
    }

    private static void assertScheduleExpression(ScheduleExpression expression) {
        Assert.assertEquals("0 0/2 * * * ?", expression.getCronExpression());
        Assert.assertEquals(10, expression.getDuration());
        Assert.assertEquals(TimeIntervalUnit.MINUTES, expression.getDurationUnit());
    }

    public static ContentSet createContentSet() {
        ContentSet c = new ContentSet();
        c.setId(1l);
        c.setCreationTime(200);
        c.setDescription("description");
        c.setEnabled(true);
        c.setInventoryInterval(100);
        c.setInventoryIntervalUnit(TimeIntervalUnit.SECONDS);
        c.setName("name");
        c.setPath("path");
        c.setSupportsGbsTransport(true);
        c.setSupplier(true);
        
        FtpConnection conn = new FtpConnection();
        conn.setDirectory("directory");
        conn.setHost("localhost");
        conn.setPassive(true);
        conn.setPassword("password");
        conn.setPort("21");
        conn.setUsername("username");
        
        c.setFtpConnection(conn);
        
        AllowedHost host = new AllowedHost();
        host.setAlias("testAlias");
        host.setCommonName("testCommonName");
        
        c.addAllowedHost(host);
        
        c.addScheduleExpression(getScheduleExpression());
        
        return c;
    }
    
    public static ConsumerContentSet createConsumerContentSet() {
        ConsumerContentSet c = new ConsumerContentSet();
        c.setId(1l);
        c.setCreationTime(200);
        c.setDescription("description");
        c.setEnabled(true);
        c.setInventoryInterval(100);
        c.setInventoryIntervalUnit(TimeIntervalUnit.SECONDS);
        c.setName("name");
        c.setPath("path");
        c.setSupportsGbsTransport(true);
        c.setSupplier(false);
        
        c.setFileDeleteDelay(100);
        c.setFileDeleteDelayUnit(TimeIntervalUnit.DAYS);
        c.setMaxFileSize(100);
        c.setMaxFileSizeUnit(FileSizeUnit.MEGABYTES);
        c.setSupplierAddress("localhost");
        c.setSupplierName("Supplier");
        c.setSupplierPort(443);
        
        List<FileFilter> filters = new ArrayList<FileFilter>();
        FileFilter f = new FileFilter();
        f.setId(1l);
        f.setPattern("abc");
        f.setPatternType(Pattern.BEGINS);
        filters.add(f);
        c.setFileFilters(filters);
        
        c.addScheduleExpression(getScheduleExpression());
        
        return c;
    }
    
    public static void assertContentSet(ContentSet contentSet) {
        Assert.assertNotNull(contentSet);
        Assert.assertEquals(new Long(1), contentSet.getId());
        Assert.assertEquals("name", contentSet.getName());
        Assert.assertEquals(FileSystemUtil.correctPath("path"), contentSet.getPath());
        Assert.assertTrue(contentSet.isEnabled());
        Assert.assertTrue(contentSet.isSupplier());
        Assert.assertEquals(100, contentSet.getInventoryInterval());
        Assert.assertEquals(TimeIntervalUnit.SECONDS, contentSet.getInventoryIntervalUnit());
        Assert.assertTrue(contentSet.getCreationTime() > 0);
        Assert.assertEquals("description", contentSet.getDescription());
        Assert.assertTrue(contentSet.isSupportsGbsTransport());
        Assert.assertTrue(contentSet.getFileFilters().isEmpty());
        
        FtpConnection conn = contentSet.getFtpConnection();
        
        Assert.assertNotNull(conn);
        Assert.assertEquals("directory", conn.getDirectory());
        Assert.assertEquals("localhost", conn.getHost());
        Assert.assertTrue(conn.isPassive());
        Assert.assertEquals("password", conn.getPassword());
        Assert.assertEquals("21", conn.getPort());
        Assert.assertEquals("username", conn.getUsername());
        
        Assert.assertEquals(1, contentSet.getAliases().size());
        
        AllowedHost host = contentSet.getAliases().iterator().next();
        
        Assert.assertEquals("testAlias", host.getAlias());
        Assert.assertEquals("testCommonName", host.getCommonName());
        
        assertScheduleExpression(contentSet.getScheduleExpressions().iterator().next());
   }
    
    public static void assertConsumerContentSet(ConsumerContentSet contentSet) {
        Assert.assertNotNull(contentSet);
        Assert.assertEquals(new Long(1), contentSet.getId());
        Assert.assertEquals("name", contentSet.getName());
        Assert.assertEquals(FileSystemUtil.correctPath("path"), contentSet.getPath());
        Assert.assertTrue(contentSet.isEnabled());
        Assert.assertFalse(contentSet.isSupplier());
        Assert.assertEquals(100, contentSet.getInventoryInterval());
        Assert.assertEquals(TimeIntervalUnit.SECONDS, contentSet.getInventoryIntervalUnit());
        Assert.assertTrue(contentSet.getCreationTime() > 0);
        Assert.assertEquals("description", contentSet.getDescription());
        Assert.assertTrue(contentSet.isSupportsGbsTransport());
        Assert.assertEquals(100, contentSet.getFileDeleteDelay());
        Assert.assertEquals(TimeIntervalUnit.DAYS, contentSet.getFileDeleteDelayUnit());
        Assert.assertEquals(100, contentSet.getMaxFileSize());
        Assert.assertEquals(FileSizeUnit.MEGABYTES, contentSet.getMaxFileSizeUnit());
        Assert.assertEquals("localhost", contentSet.getSupplierAddress());
        Assert.assertEquals("Supplier", contentSet.getSupplierName());
        Assert.assertEquals(443, contentSet.getSupplierPort());
        
        Assert.assertFalse(contentSet.getFileFilters().isEmpty());
        
        FileFilter filter = contentSet.getFileFilters().get(0);
        
        Assert.assertEquals("abc", filter.getPattern());
        Assert.assertEquals(Pattern.BEGINS, filter.getPatternType());
        
        assertScheduleExpression(contentSet.getScheduleExpressions().iterator().next());
    }
}
