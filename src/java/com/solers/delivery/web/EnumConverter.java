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
package com.solers.delivery.web;

import java.lang.reflect.Method;

import org.directwebremoting.ConversionException;
import org.directwebremoting.extend.AbstractConverter;
import org.directwebremoting.extend.Converter;
import org.directwebremoting.extend.InboundVariable;
import org.directwebremoting.extend.NonNestedOutboundVariable;
import org.directwebremoting.extend.OutboundContext;
import org.directwebremoting.extend.OutboundVariable;
import org.directwebremoting.util.LocalUtil;

/**
 * DWR Converter that converts an Enum using the toString() / toValue()
 * methods instead of the standard valueOf() / name().
 * 
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class EnumConverter extends AbstractConverter implements Converter {

    @Override
    public Object convertInbound(Class<?> paramType, InboundVariable data) throws ConversionException {
        if (data.isNull()) {
            return null;
        }
        String value = LocalUtil.urlDecode(data.getValue());
        
        try
        {
            Method getter = paramType.getMethod("toValue", String.class);
            Object reply = getter.invoke(paramType, value);
            if (reply == null)
            {
                throw new ConversionException(paramType);
            }
            return reply;
        }
        catch (NoSuchMethodException ex)
        {
            // We would like to have done: if (!paramType.isEnum())
            // But this catch block has the same effect
            throw new ConversionException(paramType);
        }
        catch (Exception ex)
        {
            throw new ConversionException(paramType, ex);
        }

    }

    @Override
    public OutboundVariable convertOutbound(Object data, OutboundContext ctx) throws ConversionException {
        return new NonNestedOutboundVariable('\'' + ((Enum<?>) data).toString() + '\'');
    }

}
