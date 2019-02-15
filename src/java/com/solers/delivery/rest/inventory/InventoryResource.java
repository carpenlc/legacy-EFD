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
package com.solers.delivery.rest.inventory;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.Variant;

import com.solers.delivery.inventory.plugin.InventoryPlugin;
import com.solers.delivery.inventory.plugin.PluginException;
import com.solers.delivery.inventory.plugin.provider.Parameter;
import com.solers.delivery.rest.Utils;
import com.solers.delivery.rest.converter.Converter;
import com.solers.delivery.rest.converter.ParameterConverter;
import com.solers.delivery.rest.converter.ProviderInfoConverter;

public class InventoryResource extends Resource {
    private Converter providerConverter = new ProviderInfoConverter();
    private Converter parameterConverter = new ParameterConverter();
    
    @Override
    public boolean allowGet() {
        return true;
    }
    
    @Override
    public boolean allowPost() {
        return false;
    }
    
    @Override
    public boolean allowPut() {
        return true;
    }
    
    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        getVariants().clear();
        getVariants().add(new Variant(MediaType.TEXT_XML));
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
    }
    
    @Override
    public void handlePut() {
        Representation entity = getRequest().getEntity();
        String URItext = null;
        try {
            URItext = entity.getText().trim();
        } catch (IOException ioe) {}
        
        if (URItext == null) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Resource URI required.");
            return;
        }
        
        URI uri = null;
        try {
            uri = new URI(URItext);
        } catch (Exception e) {
            getResponse().setStatus(Utils.CLIENT_ERROR_VALIDATION, e.getMessage().replace('\n', ' '));
            return;
        }
        
        try {
            MediaType type = Utils.findType(getRequest(), MediaType.TEXT_XML);
            Collection<Parameter> parameters = InventoryPlugin.getParameterInfo(uri);
            Set<Parameter> paramSet = new HashSet<Parameter>(parameters);
            getResponse().setEntity(parameterConverter.to(type, paramSet));
        } catch (PluginException pe) {
            getResponse().setStatus(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE, pe.getMessage());
        }
    }
    
    @Override
    public Representation represent(Variant variant) {
        return providerConverter.to(
            Utils.findType(getRequest(), MediaType.TEXT_XML),
            InventoryPlugin.getProviderInfo());
    }
}
