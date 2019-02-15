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
package com.solers.lucene;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;


/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class ObjectCreator {
    
    public static <T> T create(Class<T> clazz, Document document) {
        T result = newInstance(clazz);
        
        for (PropertyDescriptor p : getProperties(clazz)) {
            Method w = p.getWriteMethod();
            if (shouldSet(w)) {
                LuceneField info = w.getAnnotation(LuceneField.class);
                Field field = getField(document, p, info);
                if (field != null) {
                    Object value = info.converter().convertFrom(field.stringValue());
                    setValue(result, w, value);
                }
            }
        }
        
        return result;
    }
    
    private static <T> void setValue(T result, Method w, Object value) {
        try {
            w.invoke(result, value);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        } catch (InvocationTargetException ex) {
            throw new RuntimeException(ex.getTargetException());
        }
    }
    
    private static Field getField(Document document, PropertyDescriptor p, LuceneField info) {
        String name = info.name().equals("") ? p.getName() : info.name();
        Field field = document.getField(name);
        return field;
    }

    private static boolean shouldSet(Method w) {
        return w != null && w.isAnnotationPresent(LuceneField.class);
    }
    
    private static <T> T newInstance(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private static PropertyDescriptor[] getProperties(Class<?> clazz) {
        try {
            return Introspector.getBeanInfo(clazz).getPropertyDescriptors();
        } catch (IntrospectionException ex) {
            throw new RuntimeException(ex);
        }
    }
    
}
