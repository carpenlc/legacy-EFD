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

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.restlet.resource.Representation;

import com.solers.delivery.reports.history.ReportDetail;
import com.solers.delivery.reports.history.Synchronization;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public final class HistoryConverter extends Converter {
    
    @SuppressWarnings("unchecked")
    public <T> List<T> fromList(Representation r) throws IOException {
        return (List<T>) convert(r);
    }
    
    protected XStream initialize(XStream stream) {
        stream.setMode(XStream.NO_REFERENCES);
        
        stream.alias("list", List.class);
        stream.aliasType("list", List.class);
        stream.alias("detail", ReportDetail.class);
        stream.alias("synchronization", Synchronization.class);
        
        stream.omitField(ReportDetail.class, "transferred");
        stream.omitField(Synchronization.class, "endDate");
        
        CollectionConverter converter = new CollectionConverter(stream.getMapper()) {
            @Override
            @SuppressWarnings("unchecked")
            public boolean canConvert(Class type) {
                return Collection.class.isAssignableFrom(type);
            }
            
        };
        
        stream.registerConverter(converter);
        stream.registerConverter(new JavaBeanConverter(stream.getMapper()), XStream.PRIORITY_VERY_LOW);
        return stream;
    }
}
