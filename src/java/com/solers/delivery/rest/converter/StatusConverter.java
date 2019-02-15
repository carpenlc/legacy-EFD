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

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.resource.Representation;

import com.solers.delivery.content.status.CurrentSynchronization;
import com.solers.delivery.management.ConsumerStatus;
import com.solers.delivery.management.SupplierStatus;
import com.solers.delivery.transport.http.TransferStatus;
import com.solers.delivery.management.XmlProxy;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.javabean.BeanProvider;
import com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;
import com.thoughtworks.xstream.mapper.DynamicProxyMapper;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class StatusConverter extends Converter {
    
    public <T> T from(Representation entity, Class<T> type) throws IOException {
        try {
            String data = entity.getText();
            if (data.equals(to(MediaType.TEXT_XML, null).getText())) {
                return null;
            } else {
                return XmlProxy.proxy(type, data);
            }
        } finally {
            entity.release();
        }
    }
    
    @SuppressWarnings("unchecked")
    public List<CurrentSynchronization> fromList(Representation r) throws IOException {
        return (List<CurrentSynchronization>) convert(r);
    }
    
    @Override
    protected XStream initialize(XStream stream) {
        stream.setMode(XStream.NO_REFERENCES);
        
        // The default BeanProvider only serializes properties with read and write methods
        // Our interfaces only have read methods so we need to change
        // the default behavior
        BeanProvider provider = new BeanProvider() {
            protected boolean canStreamProperty(PropertyDescriptor descriptor) {
                return descriptor.getReadMethod() != null;
            }
        };
        
        // Ensure that only our two status classes get converted with this converter
        JavaBeanConverter converter = new JavaBeanConverter(stream.getMapper(), provider) {
            @SuppressWarnings("unchecked")
            public boolean canConvert(Class type) {
                return (SupplierStatus.class.isAssignableFrom(type) || 
                        ConsumerStatus.class.isAssignableFrom(type) ||
                        TransferStatus.class.isAssignableFrom(type));
            }
        };
        
        // The dynamic proxy mapper will set the default alias to be <dynamic-proxy>
        // It should be <status>
        DynamicProxyMapper p = (DynamicProxyMapper) stream.getMapper().lookupMapperOfType(DynamicProxyMapper.class);
        p.setAlias("status");
                
        // Other non-proxy implementations of the status classes should be aliased
        // to "status" as well
        stream.aliasType("status", ConsumerStatus.class);
        stream.aliasType("status", SupplierStatus.class);
        stream.aliasType("status", TransferStatus.class);        
        
        stream.alias("list", List.class);
        stream.aliasType("list", List.class);
        stream.alias("currentSynchronization", CurrentSynchronization.class);
        
        stream.registerConverter(converter, XStream.PRIORITY_VERY_HIGH);
        return stream;
    }
}
