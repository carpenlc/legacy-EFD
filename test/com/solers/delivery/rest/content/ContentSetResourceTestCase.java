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
package com.solers.delivery.rest.content;

import java.util.Arrays;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.data.Response;
import org.restlet.data.Status;

import com.solers.delivery.ContentSetAssert;
import com.solers.delivery.domain.ConsumerContentSet;
import com.solers.delivery.domain.ContentSet;
import com.solers.delivery.domain.FileFilter;
import com.solers.delivery.domain.FileFilter.Pattern;
import com.solers.delivery.rest.BaseRestTestCase;
import com.solers.delivery.rest.Utils;
import com.solers.delivery.rest.converter.ContentSetConverter;
import com.solers.delivery.rest.converter.ValidationExceptionConverter;
import com.solers.util.dao.ValidationException;


/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class ContentSetResourceTestCase extends BaseRestTestCase {
    
    ContentSetConverter converter = new ContentSetConverter();
    
    public void testGetNotFound() {
        uri = uri + "13";
        get(Status.CLIENT_ERROR_NOT_FOUND);
    }
    
    public void testGetWithoutAuth() throws Exception {
        authenticate = false;
        get(null, Status.CLIENT_ERROR_UNAUTHORIZED);
    }
    
    public void testPutWithoutAuth() throws Exception {
        authenticate = false;
        put(ContentSetAssert.CONTENT_SET_XML_NEW, null, Status.CLIENT_ERROR_UNAUTHORIZED);
    }
    
    public void testDeleteWithoutAuth() throws Exception {
        authenticate = false;
        delete(Status.CLIENT_ERROR_UNAUTHORIZED);
    }
    
    public void testGetBadRequest() {
        uri = uri + "foo";
        get(Status.CLIENT_ERROR_BAD_REQUEST);
    }
    
    public void testGetContentSetXml() throws Exception {
        
        System.err.println("Starting...............");
        System.err.flush();
        
        getDAOFactory().getContentSetDAO().makePersistent(ContentSetAssert.createContentSet());
        uri = uri + "1";
        
        Response r = get(MediaType.TEXT_XML);
        try {
            ContentSetAssert.assertContentSet(converter.from(r.getEntity()));
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }
    
    public void testGetContentSetJSON() throws Exception {
        getDAOFactory().getContentSetDAO().makePersistent(ContentSetAssert.createContentSet());
        uri = uri + "1";
        
        Response r = get(MediaType.APPLICATION_JSON);
        ContentSetAssert.assertContentSet(converter.from(r.getEntity()));
    }
    
    public void atestGetConsumerContentSetXml() throws Exception {
        getDAOFactory().getContentSetDAO().makePersistent(ContentSetAssert.createConsumerContentSet());
        uri = uri + "1";
        
        Response r = get(MediaType.TEXT_XML);
        ConsumerContentSet set = converter.from(r.getEntity());
        ContentSetAssert.assertConsumerContentSet(set);
    }
    
    public void testGetConsumerContentSetJSON() throws Exception {
        getDAOFactory().getContentSetDAO().makePersistent(ContentSetAssert.createConsumerContentSet());
        uri = uri + "1";
        
        Response r = get(MediaType.APPLICATION_JSON);
        ConsumerContentSet set = converter.from(r.getEntity());
        ContentSetAssert.assertConsumerContentSet(set);
    }
    
    public void testDeleteContentSet() {
        getDAOFactory().getContentSetDAO().makePersistent(ContentSetAssert.createContentSet());
        uri = uri + "1";
        
        assertFalse(getDAOFactory().getContentSetDAO().findAll().isEmpty());
        
        delete();
        
        assertTrue(getDAOFactory().getContentSetDAO().findAll().isEmpty());
        assertEquals(new Long(1), getSupplierManager().removed);
    }
    
    public void testDeleteConsumerContentSet() {
        getDAOFactory().getConsumerContentSetDAO().makePersistent(ContentSetAssert.createConsumerContentSet());
        uri = uri + "1";
        
        assertFalse(getDAOFactory().getConsumerContentSetDAO().findAll().isEmpty());
        
        delete();
        
        assertTrue(getDAOFactory().getConsumerContentSetDAO().findAll().isEmpty());
        assertEquals(new Long(1), getConsumerManager().removed);
    }
    
    public void testDeleteBadRequest() {
        uri = uri + "foo";
        delete(Status.CLIENT_ERROR_BAD_REQUEST);
    }
    
    public void testDeleteNotFound() {
        uri = uri + "2";
        delete(Status.CLIENT_ERROR_NOT_FOUND);
    }
    
    public void atestPutContentSetXml() throws Exception {
        getDAOFactory().getContentSetDAO().makePersistent(ContentSetAssert.createContentSet());
        uri = uri + "1";
        
        ContentSet contentSet = ContentSetAssert.createContentSet();
        contentSet.setDescription("new description");
        
        put(converter.to(MediaType.TEXT_XML, contentSet));
        
        contentSet = getDAOFactory().getContentSetDAO().findById(new Long(1), false);
        assertEquals("new description", contentSet.getDescription());
        
        Response r = get(MediaType.TEXT_XML);
        contentSet = converter.from(r.getEntity());
        assertEquals("new description", contentSet.getDescription());
    }
    
    public void testPutContentSetJSON() throws Exception {
        getDAOFactory().getContentSetDAO().makePersistent(ContentSetAssert.createContentSet());
        uri = uri + "1";
        
        ContentSet contentSet = ContentSetAssert.createContentSet();
        contentSet.setDescription("new description");
        
        put(converter.to(MediaType.APPLICATION_JSON, contentSet));
        
        contentSet = getDAOFactory().getContentSetDAO().findById(new Long(1), false);
        assertEquals("new description", contentSet.getDescription());
        
        Response r = get(MediaType.APPLICATION_JSON);
        contentSet = converter.from(r.getEntity());
        assertEquals("new description", contentSet.getDescription());
    }
    
    public void atestPutConsumerContentSetXml() throws Exception {
        getDAOFactory().getConsumerContentSetDAO().makePersistent(ContentSetAssert.createConsumerContentSet());
        uri = uri + "1";
        
        ConsumerContentSet contentSet = ContentSetAssert.createConsumerContentSet();
        FileFilter newFilter = new FileFilter();
        newFilter.setPattern("xyz");
        newFilter.setPatternType(Pattern.CONTAINS);
        contentSet.getFileFilters().add(newFilter);
        contentSet.setDescription("new description");
        
        List<FileFilter> expected = Arrays.asList(new FileFilter("abc", Pattern.BEGINS), new FileFilter("xyz", Pattern.CONTAINS));
        
        put(converter.to(MediaType.TEXT_XML, contentSet));
        
        contentSet = getDAOFactory().getConsumerContentSetDAO().findById(new Long(1), false);
        assertEquals("new description", contentSet.getDescription());
        assertEquals(2, contentSet.getFileFilters().size());
        assertEquals(expected, contentSet.getFileFilters());
        
        expected = Arrays.asList(new FileFilter("abc", Pattern.BEGINS), new FileFilter("xyz", Pattern.CONTAINS));
        
        Response r = get(MediaType.TEXT_XML);
        contentSet = converter.from(r.getEntity());
        assertEquals("new description", contentSet.getDescription());
        assertEquals(2, contentSet.getFileFilters().size());
        assertEquals(expected, contentSet.getFileFilters());
    }
    
    public void testPutConsumerContentSetJSON() throws Exception {
        getDAOFactory().getConsumerContentSetDAO().makePersistent(ContentSetAssert.createConsumerContentSet());
        uri = uri + "1";
        
        ConsumerContentSet contentSet = ContentSetAssert.createConsumerContentSet();
        FileFilter newFilter = new FileFilter();
        newFilter.setPattern("xyz");
        newFilter.setPatternType(Pattern.CONTAINS);
        contentSet.getFileFilters().add(newFilter);
        contentSet.setDescription("new description");
        
        List<FileFilter> expected = Arrays.asList(new FileFilter("abc", Pattern.BEGINS), new FileFilter("xyz", Pattern.CONTAINS));
        
        put(converter.to(MediaType.APPLICATION_JSON, contentSet));
        
        contentSet = getDAOFactory().getConsumerContentSetDAO().findById(new Long(1), false);
        assertEquals("new description", contentSet.getDescription());
        assertEquals(2, contentSet.getFileFilters().size());
        assertEquals(expected, contentSet.getFileFilters());
        
        // Filters coming back don't contain the content set id
        expected = Arrays.asList(new FileFilter("abc", Pattern.BEGINS), new FileFilter("xyz", Pattern.CONTAINS));
        
        Response r = get(MediaType.APPLICATION_JSON);
        contentSet = converter.from(r.getEntity());
        assertEquals("new description", contentSet.getDescription());
        assertEquals(2, contentSet.getFileFilters().size());
        assertEquals(expected, contentSet.getFileFilters());
    }
    
    public void testPutContentSetWithInvalidID() throws Exception {
        getDAOFactory().getContentSetDAO().makePersistent(ContentSetAssert.createContentSet());
        uri = uri + "1";
        
        ContentSet contentSet = ContentSetAssert.createContentSet();
        contentSet.setId(new Long(4));
        
        put(converter.to(MediaType.TEXT_XML, contentSet), Status.CLIENT_ERROR_BAD_REQUEST);
    }
    
    public void testPutContentSetNotFound() throws Exception {
        uri = uri + "1";
        
        ContentSet contentSet = ContentSetAssert.createContentSet();
        
        put(converter.to(MediaType.TEXT_XML, contentSet), Status.CLIENT_ERROR_NOT_FOUND);
    }
    
    public void atestValidationException() throws Exception {
        getDAOFactory().getContentSetDAO().makePersistent(ContentSetAssert.createContentSet());
        uri = uri + "1";
        
        getDAOFactory().getContentSetDAO().throwOnSave = true;
        
        ContentSet contentSet = ContentSetAssert.createContentSet();
        
        Response r = put(converter.to(MediaType.TEXT_XML, contentSet), Utils.CLIENT_ERROR_VALIDATION);
        
        ValidationException ex = (ValidationException) new ValidationExceptionConverter().from(r.getEntity());
        
        assertNotNull(ex);
        assertEquals(3, ex.getMessages().size());
        assertTrue(ex.getMessages().contains("test1"));
        assertTrue(ex.getMessages().contains("test2"));
        assertTrue(ex.getMessages().contains("test3"));
    }
}
