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

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Set;

import javax.management.JMX;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.OperationsException;

import org.apache.log4j.Logger;

/**
 * The MBeanRegistrar is a singleton, configured through Spring, that handles JMX concerns such as
 * registrations and retrievals.  Some fields, such as the MBeanExporter, are only needed for
 * registration and as such may be safely excluded when configured for a client application.
 */
public class MBeanRegistrar implements Registrar {
    
    private static final Logger log = Logger.getLogger(MBeanRegistrar.class);
    
    private final MBeanServerConnection server;
    
    public MBeanRegistrar() {
        this(ManagementFactory.getPlatformMBeanServer());
    }
    
    /**
     * Configures the MBeanRegistrar with the default MXBean declaration policy
     * @param server the server connection to use for retrievals
     */
    public MBeanRegistrar(MBeanServerConnection server) {
        this.server = server;
    }

    /**
     * @throws IllegalArgumentException when the specified interface does not meet the naming declaration policy
     * @throws IllegalArgumentException if the naming object is null.
     * @throws UnsupportedOperationException if no JMX server connection has been configured for this registrar
     */
    public <M> M getManagedBean(Class<? extends M> mxbean, Object namingObject) {
        if (namingObject == null) {
            throw new IllegalArgumentException("Naming object cannot be null.");
        }
        if (server == null) {
            throw new UnsupportedOperationException("No JMX server connection has been configured for this VM.");
        }
        
        try {
            ObjectName jmxName = generateName(namingObject);
            
            Set<ObjectInstance> results = server.queryMBeans(jmxName, null);
            if (results.isEmpty()) {
                return null;
            }
            if (results.size() == 1) {
                return mxbean.cast(JMX.newMXBeanProxy(server, results.iterator().next().getObjectName(), mxbean));
            }
            throw new IllegalArgumentException("Found more than one match for the given object name");
        } catch (MalformedObjectNameException mone) {
            log.warn(mone);
        } catch (IOException ex) {
            log.error("Error querying bean", ex);
        }
        
        return null;
    }
    
    /**
     * @throws IllegalArgumentException when the specified interface does not obey MXBean declaration policy.
     * @throws IllegalArgumentException when the naming object is null.
     * @throws UnsupportOperationException if no exporter has been configured for this registrar
     */
    public void registerManagedBean(Object object, String typeCategory, Object key) {
        if (!(server instanceof MBeanServer)) {
            throw new IllegalStateException("Cannot register beans on the given server");
        }
        if (key == null) {
            throw new IllegalArgumentException("Naming object cannot be null.");
        }
        
        try {
            ObjectName jmxName = generateName(key, typeCategory);
            
            if (server.isRegistered(jmxName)) {
                server.unregisterMBean(jmxName);
            }
            ((MBeanServer)server).registerMBean(object, jmxName);
            
        } catch (OperationsException ex) {
            log.warn("Exception occurred registering mbean", ex);
        } catch (MBeanException ex) {
            log.warn("Exception occurred registering mbean", ex);
        } catch (IOException ex) {
            log.error("Error registering bean", ex);
        }
    }
    
    private ObjectName generateName(Object namingObject) throws MalformedObjectNameException {
        String name = String.format("com.solers.EFD:type=*,name=%s", namingObject.toString());
        return ObjectName.getInstance(name);       
    }
    
    private ObjectName generateName(Object namingObject, String typeCategory) throws MalformedObjectNameException {
        String name = String.format("com.solers.EFD:type=%s,name=%s", typeCategory, namingObject.toString());
        return ObjectName.getInstance(name);
    }
}
