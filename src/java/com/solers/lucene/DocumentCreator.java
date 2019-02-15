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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;


/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class DocumentCreator {
    
    public static DocumentResult create(Object obj) {
        Document document = new Document();
        PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper(new StandardAnalyzer());
        for (Method method : obj.getClass().getMethods()) {
            if (method.isAnnotationPresent(LuceneField.class)) {
                Field field = getField(method, obj);
                if (field != null) {
                    document.add(field);
                    analyzer.addAnalyzer(field.name(), getAnalyzer(method));
                }
            }
        }
        return new DocumentResult(document, analyzer);
    }
    
    private static Field getField(Method method, Object obj) {
        LuceneField info = method.getAnnotation(LuceneField.class);
        Field.Store store = info.store() ? Field.Store.YES : Field.Store.NO;
        Field.Index index = info.index().type();
        String value = getValue(method, obj, info.converter());
        String name = info.name().equals("") ? getName(method, obj) : info.name();
        
        if (value == null) {
            return null;
        }
        Field f = new Field(name, value, store, index);
        return f;
    }
    
    private static String getValue(Method method, Object obj, Converter converter) {
        Object result = null;
        try {
            result = method.invoke(obj);
        } catch (InvocationTargetException ex) {
            throw new RuntimeException(ex.getTargetException());
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
        
        return converter.convertTo(result);
    }
    
    private static String getName(Method method, Object obj) {
        try {
            BeanInfo info = Introspector.getBeanInfo(obj.getClass());
            for (PropertyDescriptor p : info.getPropertyDescriptors()) {
                if (p.getReadMethod() != null && p.getReadMethod().equals(method)) {
                    return p.getName();
                }
            }
        } catch (IntrospectionException ex) {
            throw new RuntimeException(ex);
        }
        throw new IllegalArgumentException("Could not find property name for method: "+method);
    }
    
    private static Analyzer getAnalyzer(Method method) {
        LuceneField info = method.getAnnotation(LuceneField.class);
        Class<? extends Analyzer> clazz = info.analyzer();
        try {
            return clazz.newInstance();
        } catch (InstantiationException ex) {
            throw new RuntimeException("Could not instantiate analyzer", ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException("Could not access analyzer", ex);
        }
    }
}
