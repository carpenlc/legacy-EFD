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
package com.solers.jmx.registry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
@Aspect
public class AutoRegisterAspect {
    
    private static final Logger log = Logger.getLogger(AutoRegisterAspect.class);
    
    private Registrar registrar;
    
    public AutoRegisterAspect() {
        registrar = new MBeanRegistrar();
    }
    
    public void setRegistrar(Registrar registrar) {
        this.registrar = registrar;
    }
    
    @AfterReturning("execution(*.new(..)) && @within(a) && target(o)")
    public void advice(AutoRegister a, Object o) {
        register(o, a);
    }

    public void register(Object object) {
        if (!object.getClass().isAnnotationPresent(AutoRegister.class)) {
            throw new IllegalArgumentException(object+" does not have the "+AutoRegister.class.getSimpleName()+" annotation");
        }
        
        AutoRegister info = object.getClass().getAnnotation(AutoRegister.class);
        register(object, info);
    }
    
    private void register(Object object, AutoRegister info) {
        log.info("Registering "+object+" in JMX");
        registrar.registerManagedBean(object, info.category(), getObjectName(object));
    }
    
    /**
     * Get the object name for {@code obj}.  If the class annotates
     * a method with @{code AutoRegisterObjectName}, it will use the result
     * of that method invocation as the object name.
     * 
     * Otherwise, it will use the name of the class
     * 
     * @param obj
     * @return
     */
    private Object getObjectName(Object obj) {
        for (Method m : obj.getClass().getDeclaredMethods()) {
            if (m.isAnnotationPresent(AutoRegisterObjectName.class)) {
                try {
                    return m.invoke(obj);
                } catch (InvocationTargetException ex) {
                    throw new RuntimeException("Error occurred getting object name", ex);
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException("Could not access object name", ex);
                }
            }
        }
        return obj.getClass().getSimpleName();
    }
    
}
