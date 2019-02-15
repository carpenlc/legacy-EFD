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
package com.solers.delivery.rest.admin;

import com.solers.delivery.domain.User;
import com.solers.delivery.rest.BaseEnableResource;
import com.solers.delivery.user.UserService;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class EnableUserResource extends BaseEnableResource<User> {
    
    private final UserService service;
    
    public EnableUserResource(UserService service) {
        this.service = service;
    }
    
    @Override
    protected void disable(User item) {
        service.disable(item.getId());
    }

    @Override
    protected void enable(User item) {
        service.enable(item.getId());
    }

    @Override
    protected User lookup(long id) {
        return service.get(id);
    }
}
