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
package com.solers.delivery.user;

import org.springframework.dao.DataAccessException;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;

import com.solers.delivery.domain.User;

public class DefaultUserDetailsService implements UserDetailsService {

    private final UserService userService;
    private final PasswordService passwordService;

    public DefaultUserDetailsService(UserService userService, PasswordService passwordService) {
        this.userService = userService;
        this.passwordService = passwordService;
    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
        User user = userService.get(username);

        if (user != null) {
            String password = passwordService.getPassword(username);
            // Ignore whether the users credentials are expired or not.  That check will be made explicitly 
            // when the user logs in. See SystemHelper.login()
            boolean notExpired = true;
            return new org.springframework.security.userdetails.User(user.getUsername(), password, user.isEnabled(), true, notExpired, true, user.getAuthorities());
        }
        throw new UsernameNotFoundException("Username does not exist.");
    }
}
