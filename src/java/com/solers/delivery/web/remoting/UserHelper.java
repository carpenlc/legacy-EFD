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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.solers.delivery.domain.User;
import com.solers.delivery.user.PasswordService;
import com.solers.delivery.user.UserService;
import com.solers.delivery.util.password.InvalidPasswordException;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class UserHelper {
    
    private final UserService userService;
    private final PasswordService passwordService;

    public UserHelper(UserService userService, PasswordService passwordService) {
        this.userService = userService;
        this.passwordService = passwordService;
    }
    
    public List<UserNode> getUsers() {
        List<User> users = userService.getUsers();
        List<UserNode> result = new ArrayList<UserNode>(users.size());
        
        for (User u : users) {
            UserNode node = new UserNode(u.getId(), u.getUsername());
            node.setEnabled(u.isEnabled());
            node.setName(u.getName());
            node.setLastLogin(u.getLastLogin());
            node.setFailedLogins(u.getFailedLogins());
            result.add(node);
        }
        return result;
    }
    
    public Long saveUser(User user, String pw, String pwConfirm) {
        boolean newUser = user.getId() == null;
        
        if (newUser) {
            Long newId = userService.save(user);
            user.setId(newId);
        }
        
        try {
            if (newUser || !StringUtils.isBlank(pw) || !StringUtils.isBlank(pwConfirm)) {
                if (!pw.equals(pwConfirm)) {
                    throw new InvalidPasswordException("Passwords do not match");
                }
                passwordService.updatePassword(user.getUsername(), pw);
            }
        } catch (InvalidPasswordException ex) {
            // We need to rollback the user save if the users password was invalid
            if (newUser) {
                userService.remove(user.getId());
            }
            throw ex;
        }
        
        
        if (!newUser) {
            userService.save(user);
        }
        
        return user.getId();
    }
    
    public User getUserById(Long id) {
        return userService.get(id);
    }
    
    public User getUserByName(String username) {
        return userService.get(username);
    }
}
