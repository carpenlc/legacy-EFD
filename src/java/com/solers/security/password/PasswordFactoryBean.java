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
package com.solers.security.password;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.FieldRetrievingFactoryBean;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class PasswordFactoryBean implements FactoryBean {

    private PasswordProvider provider;
    private String key;
    
    public void setKey(String key) {
        this.key = key;
    }
    
    @SuppressWarnings("unchecked")
    public void setEnumKey(String arg) {
        String className = arg.substring(0, arg.lastIndexOf('.'));
        String enumName = arg.substring(arg.lastIndexOf('.')+1, arg.length());
        try {
            Class clazz = Class.forName(className);
            Enum enumValue = Enum.valueOf(clazz, enumName);
            setKey(enumValue.toString());
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public void setFieldKey(String arg) {
        FieldRetrievingFactoryBean bean = new FieldRetrievingFactoryBean();
        bean.setStaticField(arg);
        try {
            bean.afterPropertiesSet();
            setKey((String) bean.getObject());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public void setProvider(PasswordProvider provider) {
        this.provider = provider;
    }
    
    @Override
    public Object getObject() throws Exception {
        return provider.getPassword(key);
    }

    @Override
    public Class<?> getObjectType() {
        return String.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

}
