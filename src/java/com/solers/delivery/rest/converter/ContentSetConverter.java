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
package com.solers.delivery.rest.converter;

import java.util.List;

import org.restlet.resource.Representation;

import com.solers.delivery.domain.AllowedHost;
import com.solers.delivery.domain.ConsumerContentSet;
import com.solers.delivery.domain.ContentSet;
import com.solers.delivery.domain.FileFilter;
import com.solers.delivery.domain.FtpConnection;
import com.solers.delivery.domain.ResourceParameter;
import com.solers.delivery.domain.ScheduleExpression;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;

public class ContentSetConverter extends Converter {
   
    @SuppressWarnings("unchecked")
    public <T extends ContentSet> T from(Representation r) {        
        return (T) convert(r);
    }
    
    @SuppressWarnings("unchecked")
    public <T extends ContentSet> List<T> fromList(Representation r) {
        return (List<T>) convert(r);
    }
    
    @Override
    protected XStream initialize(XStream stream) {
        stream.setMode(XStream.NO_REFERENCES);
        stream.alias("sets", List.class);
        stream.alias("supplierContentSet", ContentSet.class);
        stream.alias("consumerContentSet", ConsumerContentSet.class);
        stream.alias("filter", FileFilter.class);
        stream.alias("ftpConnection", FtpConnection.class);
        stream.alias("allowedHost", AllowedHost.class);
        stream.alias("parameter", ResourceParameter.class);
        stream.alias("scheduleExpression", ScheduleExpression.class);
        
        stream.omitField(ContentSet.class, "fileFilters");
        stream.omitField(ContentSet.class, "resourceParameters");
        stream.omitField(ContentSet.class, "resourceParametersAsMap");
        stream.omitField(ContentSet.class, "aliases");
        stream.omitField(ContentSet.class, "expressions");
        
        stream.omitField(ConsumerContentSet.class, "fileFilters");
        stream.omitField(ConsumerContentSet.class, "aliases");
        stream.omitField(ConsumerContentSet.class, "allowedHosts");
        stream.omitField(ConsumerContentSet.class, "resourceParameters");
        stream.omitField(ConsumerContentSet.class, "resourceParametersAsMap");
        stream.omitField(ConsumerContentSet.class, "expressions");
        
        //stream.omitField(FileFilter.class, "id");        
        //stream.omitField(FtpConnection.class, "id");
        stream.omitField(FtpConnection.class, "plainPassword");
        stream.omitField(ResourceParameter.class, "id");
        stream.omitField(ResourceParameter.class, "persistedValue");
       
        stream.registerConverter(new JavaBeanConverter(stream.getMapper()), XStream.PRIORITY_VERY_LOW);
        
        return stream;
    }
}
