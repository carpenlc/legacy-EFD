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
package com.solers.delivery.management;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Invocation handler that intercepts calls to property get methods (getFoo(), getInt() etc) and uses the DOM to retrieve the value from a backing
 * XML document
 * 
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class XmlProxy implements InvocationHandler {
    
    private static final Logger logger = Logger.getLogger(XmlProxy.class);

    public static <T> T proxy(Class<T> clazz, String xml) {
        return clazz.cast(Proxy.newProxyInstance(XmlProxy.class.getClassLoader(), new Class<?>[] { clazz }, new XmlProxy(xml)));
    }
    
    public static <T> T proxy(Class<T> clazz, Node node) {
        return clazz.cast(Proxy.newProxyInstance(XmlProxy.class.getClassLoader(), new Class<?>[] { clazz }, new XmlProxy(node)));
    }
    
    /*private static Node toDocument(String xml) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            String FEATURE = null;
            try {
                // This is the PRIMARY defense.  If DTDs (doctypes) are disallowed, almost all XML entity attacks are prevented
                // Xerces 2 only - http://xerces.apache.org/xerces2-j/features.html#disallow-doctyp-decl
                
                FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";
                dbf.setFeature(FEATURE, true);
            
                // If you can't completely disable DTDs, then at least do the following:
                // Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-general-entities
                // Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-general-entities
                // JDK7+ - http://xml.org.sax.features/external-general-entities
            
                FEATURE = "http://xml.org.sax.features/external-general-entities";
                dbf.setFeature(FEATURE, false);
            
                // Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-parameter-entities
                // Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-parameter-entities
                // JDK7+ - http://xml.org.sax.features/external-parameter-entities

                FEATURE = "http://xml.org.sax.features/external-parameter-entities";
                dbf.setFeature(FEATURE, false);

                // Disable external DTDs as well
                FEATURE = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
                dbf.setFeature(FEATURE, false);

                // and these as well, per Timothy Morgan's 2014 paper: "XML Schema, DTD and Entity Attacks" (see reference below)

                dbf.setXIncludeAware(false);
                dbf.setExpandEntityReferences(false);

                // And, per Timothy Morgan: "If for some reaseon support for inline DOCTYPEs are a requirement, then
                // ensure the entity settings are disabled (as shown above) and beware that SSRF attacks
                // (http://cwe.mitre.org/data/definitions/918.html) and denial
                // of service attacks (such as billion laughs or decompression bombs via "jar:") are a risk."
            
            }
            catch (ParserConfigurationException e) {
                
                // This should catch a failed setFeature feature
                
                logger.info("ParserConfigurationException was thrown. The feature '" +
                        FEATURE +
                        "' is probably not supported by the EFD XML processor.");
            }
 
            Document root = dbf.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes()));
            NodeList children = root.getChildNodes();
            for (int i=0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    return child;
                }
            }
            return root;
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }*/
    
    private static Node toDocument(String xml) {
        try {
            Document root = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes()));
            NodeList children = root.getChildNodes();
            for (int i=0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    return child;
                }
            }
            return root;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private final Node root;

    public XmlProxy(String xml) {
        this(toDocument(xml));
    }
    
    public XmlProxy(Node root) {
        this.root = root;
    }
    
    private Object to(Node node, String name, Class<?> clazz, Type genericType, int index) throws Exception {
        if (returning(clazz, String.class)) {
            return getString(node, name);
        }

        if (returning(clazz, Double.class)) {
            return Double.valueOf(getString(node, name));
        }

        if (returning(clazz, double.class)) {
            return Double.parseDouble(getString(node, name));
        }

        if (returning(clazz, Long.class)) {
            return Long.valueOf(getString(node, name));
        }

        if (returning(clazz, long.class)) {
            return Long.parseLong(getString(node, name));
        }

        if (returning(clazz, Integer.class)) {
            return Integer.valueOf(getString(node, name));
        }

        if (returning(clazz, int.class)) {
            return Integer.parseInt(getString(node, name));
        }

        if (returning(clazz, Boolean.class)) {
            return Boolean.valueOf(getString(node, name));
        }

        if (returning(clazz, boolean.class)) {
            return Boolean.parseBoolean(getString(node, name));
        }
        if (returning(clazz, List.class)) {
            List<Object> result = new ArrayList<Object>();
            Class<?> type = getType(genericType);
            Node fieldNode = getChildNode(node, name, index);
            NodeList children = fieldNode.getChildNodes();
            
            int idx = 0;
            for (int i=0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    idx++;
                    result.add(to(fieldNode, child.getNodeName(), type, type, idx));
                    
                }
            }

            return result;
        }
        
        if (returningInterface(clazz)) {
            return proxy(clazz, getChildNode(node, name, index));
        }
        
        if (returningEnum(clazz)) {
            String enumName = getString(node, name);
            for (Object o : clazz.getEnumConstants()) {
                Enum<?> e = (Enum<?>) o;
                if (e.name().equals(enumName)) {
                    return e;
                }
            }
            throw new IllegalArgumentException(enumName+" is not a valid enum for "+clazz);
        }
        
        // Try and use java bean properties to populate the result
        Node fieldNode = getChildNode(node, name, index);
        Object o = proxyBean(fieldNode, clazz);
        return clazz.cast(o);
    }
    
    public Object proxyBean(Class<?> clazz) throws Exception {
        return proxyBean(root, clazz);
    }
    
    public Object proxyBean(Node node, Class<?> clazz) throws Exception {
        Object o = clazz.newInstance();
        NodeList children = node.getChildNodes();
        BeanInfo info = Introspector.getBeanInfo(clazz);
        for (int i=0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                for (PropertyDescriptor p : info.getPropertyDescriptors()) {
                    if (child.getNodeName().equals(p.getName())) {
                        Method readMethod = p.getReadMethod();
                        Method writeMethod = p.getWriteMethod();
                        writeMethod.invoke(o, to(node, p.getName(), readMethod.getReturnType(), readMethod.getGenericReturnType(), 1));
                    }
                }
                
            }
        }
        return o;
    }

    @Override
    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
        if (!isReadable(m.getName())) {
            return null;
        }
        return to(root, getName(m), m.getReturnType(), m.getGenericReturnType(), 1);
    }
    
    protected Class<?> getType(Type gType) {
        if (gType instanceof ParameterizedType) {
            Type[] genericArguments = ((ParameterizedType) gType).getActualTypeArguments();
            if (genericArguments != null && genericArguments.length == 1) {
                return (Class<?>) genericArguments[0];
            }
        }
        throw new IllegalArgumentException("List does not have type");
    }

    protected boolean isReadable(Method method) {
        return isReadable(method.getName());
    }

    protected boolean isReadable(String methodName) {
        return methodName.startsWith("get") || methodName.startsWith("is");
    }

    protected boolean returning(Class<?> returnType, Class<?> type) {
        return returnType.equals(type);
    }
    
    protected boolean returningInterface(Class<?> returnType) {
        return returnType.isInterface();
    }
    
    protected boolean returningEnum(Class<?> returnType) {
        return returnType.isEnum();
    }

    protected String getName(Method method) {
        return getName(method.getName());
    }

    protected String getName(String methodName) {
        String result = null;
        if (methodName.startsWith("get")) {
            result = methodName.substring("get".length());
        } else {
            result = methodName.substring("is".length());
        }

        result = Character.toLowerCase(result.charAt(0)) + result.substring(1);
        return result;
    }

    protected String getString(Node node, String propertyName) {
        return getChildNode(node, propertyName, 1).getTextContent();
    }
    
    protected Node getChildNode(Node node, String propertyName, int index) {
        NodeList children = node.getChildNodes();
        int seen = 0;
        for (int i=0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeName().equals(propertyName)) {
                seen++;
                if (seen == index) {
                    return child;
                }
                
            }
        }
        throw new IllegalArgumentException("Document does not have property: "+propertyName+" at index: "+index);
    }
}
