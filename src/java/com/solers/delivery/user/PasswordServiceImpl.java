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

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import com.solers.delivery.daos.DAOFactory;
import com.solers.delivery.daos.PasswordDAO;
import com.solers.delivery.domain.Password;
import com.solers.delivery.domain.User;
import com.solers.delivery.util.password.InvalidPasswordException;
import com.solers.delivery.util.password.PasswordReusedException;
import com.solers.delivery.util.password.PasswordValidator;
import com.solers.delivery.util.password.UserPasswordEncoder;
import com.solers.util.unit.TimeIntervalUnit;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class PasswordServiceImpl implements PasswordService {

    private static String PASSWORD_MESSAGE = "Password must contain at least %d characters, at least 1 upper case letter, 1 lower case letter, 1 non-alphanumeric character, and NOT contain the username, first name, and/or last name.";
    
    private final DAOFactory factory;
    private final UserPasswordEncoder encoder;
    
    private int passwordExpirationDays;
    private int passwordLengthAdmin;
    private int passwordLengthUser;
    private int passwordReuseCount;
    private int maxUserChanges;
    private int userChangesInterval;
    
    public PasswordServiceImpl(DAOFactory factory) {
        this.factory = factory;
        this.encoder = new UserPasswordEncoder();
    }

    public void setPasswordExpirationDays(int passwordExpirationDays) {
        this.passwordExpirationDays = passwordExpirationDays;
    }

    public void setPasswordLengthAdmin(int passwordLengthAdmin) {
        this.passwordLengthAdmin = passwordLengthAdmin;
    }

    public void setPasswordLengthUser(int passwordLengthUser) {
        this.passwordLengthUser = passwordLengthUser;
    }

    public void setPasswordReuseCount(int passwordReuseCount) {
        this.passwordReuseCount = passwordReuseCount;
    }

    public void setMaxUserChanges(int maxUserChanges) {
        this.maxUserChanges = maxUserChanges;
    }

    public void setUserChangesInterval(int userChangesInterval) {
        this.userChangesInterval = userChangesInterval;
    }
    @Override
    @Transactional
    public String getPassword(String username) {
        User user = getUser(username);
        Password result = getCurrentPassword(user.getId());
        if (result == null) {
            return null;
        }
        return result.getPassword();
    }

    @Override
    @Transactional
    public boolean isPasswordExpired(String username) {
        User user = getUser(username);
        Calendar today = Calendar.getInstance();
        
        Calendar expireDate = Calendar.getInstance();
        expireDate.setTime(getCurrentPassword(user.getId()).getCreatedDate());
        expireDate.set(Calendar.DAY_OF_YEAR, expireDate.get(Calendar.DAY_OF_YEAR) + passwordExpirationDays);
        
        return (today.equals(expireDate) || today.after(expireDate)) || user.isInitialPassword();
    }
    
    @Override
    @Transactional
    public void changePassword(String username, String oldpassword, String newPassword) {
        User user = getUser(username);
        
        if (!user.isAdminRole()) {
            if (getRecentPasswordChanges(user) >= maxUserChanges) {
                throw new InvalidPasswordException("Please wait "+TimeIntervalUnit.format(userChangesInterval)+" to change your password again");
            }
        }
        
        String current = getPassword(username);
        
        if (!encoder.isPasswordValid(current, oldpassword)) {
            throw new InvalidPasswordException("Incorrect current password");
        }

        updatePassword(username, newPassword);
        
        if (user.isInitialPassword()) {
            user.setInitialPassword(false);
        }
    }
    
    @Override
    @Transactional
    public void updatePassword(String username, String newPassword) {
        User user = getUser(username);
        User currentUser = getCurrentUser();
        String hashedPassword = encoder.encodePassword(newPassword);
        validate(user, newPassword, hashedPassword);
        Password password =new Password(hashedPassword, user); 
        password.setAdminCreated(currentUser.isAdminRole());
        dao().makePersistent(password);
    }

    private User getCurrentUser() {
    	User retUser = null;
    	
    	if(SecurityContextHolder.getContext()!=null) {
    		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    		if(auth!=null) {
    			retUser = this.getUser(auth.getName());
      		}
    	}
    	return retUser == null ? new User() : retUser;
    	
    }
    
    private void validate(User user, String plainPassword, String hashedPassword) {
        List<Password> passwords = getPasswords(user.getId());
        int passwordLength = user.isAdminRole() ? passwordLengthAdmin : passwordLengthUser;
        
        PasswordValidator.validateSystemPassword(plainPassword, PASSWORD_MESSAGE, passwordLength);
        PasswordValidator.validatePasswordDoesNotContain(plainPassword, user.getUsername(), user.getFirstName(), user.getLastName());

        for (Password pass : passwords) {
            if (pass.getPassword().equals(hashedPassword)) {
                throw new PasswordReusedException(passwordReuseCount);
            }
        }

        if (passwords.size() == passwordReuseCount) {
            dao().deleteOldest(user.getId());
        }
    }

    private Password getCurrentPassword(Long id) {
        Password result = null;
        for (Password p : getPasswords(id)) {
            if (result == null || p.getCreatedDate().after(result.getCreatedDate())) {
                result = p;
            }
        }
        return result;
    }
    
    private PasswordDAO dao() {
        return factory.getPasswordDAO();
    }
    
    private List<Password> getPasswords(Long id) {
        return dao().getPasswords(id);
    }
    
    private User getUser(String username) {
        return factory.getUserDAO().getUserByUsername(username);
    }
    
    private int getRecentPasswordChanges(User user) {
        List<Password> passwords = getPasswords(user.getId());
        // Allow users to change their password if they only have one (the initial password)
        if (passwords.size() == 1) {
            return 0;
        }
        Calendar time = Calendar.getInstance();
        time.set(Calendar.MILLISECOND, time.get(Calendar.MILLISECOND) - userChangesInterval);
        Date d = time.getTime();
        int count = 0;
        for (Password p : passwords) {
            if(p.isAdminCreated()) {
            	continue;
            }
        	
        	Date date = p.getCreatedDate();
            if (date.equals(d) || date.after(d)) {
                count++;
            }
        }
        return count;
    }
}
