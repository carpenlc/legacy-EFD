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
package com.solers.delivery.management.registry;

import java.util.HashMap;
import java.util.Map;

import com.solers.jmx.registry.Registrar;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class MockRegistrar implements Registrar {
    
    Map<Object, Object> data = new HashMap<Object, Object>();
    
    @Override
    public <M> M getManagedBean(Class<? extends M> type, Object key) {
        return type.cast(data.get(key));
    }

    @Override
    public void registerManagedBean(Object object, String typeCategory, Object key) {
        data.put(key, object);
    }
}
