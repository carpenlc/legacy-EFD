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
package com.solers.delivery.tools.inventory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.solers.delivery.inventory.Inventory;
import com.solers.delivery.inventory.index.ReaderFactory;
import com.solers.delivery.inventory.plugin.provider.InventoryProvider;
import com.solers.delivery.inventory.plugin.provider.Parameter;
import com.solers.delivery.inventory.plugin.provider.ProviderInfo;
import com.solers.delivery.inventory.plugin.provider.StandardProviderInfo;

public class IndexInventoryProvider implements InventoryProvider {
    private static final ProviderInfo info = 
        new StandardProviderInfo("NGA", "EFD Binary Manifest", "2.2");
    
    @Override
    public boolean accept(URI resource) {
        try {
            Inventory i = ReaderFactory.newInstance(new File(resource));
            if (i != null) {
                i.close();
                return true;
            }
        } catch (IOException ioe) { }
        return false;
    }

    @Override
    public Collection<Parameter> getParameterInfo() {
        return Collections.emptySet();
    }

    @Override
    public ProviderInfo getProviderInfo() {
        return info;
    }

    @Override
    public Inventory newInstance(URI resource, Map<String, Object> parameters) {
        if (accept(resource)) {
            try {
                return ReaderFactory.newInstance(new File(resource));
            } catch (IOException ioe) {}
        }
        return null;
    }
}
