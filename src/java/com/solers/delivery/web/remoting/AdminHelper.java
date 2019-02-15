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
package com.solers.delivery.web.remoting;

import com.solers.delivery.content.AllowedHostService;
import com.solers.delivery.domain.AllowedHost;
import com.solers.delivery.domain.User;
import com.solers.delivery.user.UserService;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class AdminHelper {
    
    private final AllowedHostService allowedHosts;
    private final UserService userService;
    
    public AdminHelper(AllowedHostService allowedHosts, UserService userService) {
        this.allowedHosts = allowedHosts;
        this.userService = userService;
    }

    public void deleteAll(AllowedHost [] hosts, User [] users) {
        for (AllowedHost host : hosts) {
            allowedHosts.remove(host);
        }
        
        for (User user : users) {
            userService.remove(user.getId());
        }
    }
    
    /**
     * Enable the selected users
     * @param ids
     */
    public void enable(Long [] ids) {
        for (Long id : ids) {
            userService.enable(id);
        }
    }
    
    /**
     * Disable the selected users
     * @param ids
     */
    public void disable(Long [] ids) {
        for (Long id : ids) {
            userService.disable(id);
        }
    }
}
