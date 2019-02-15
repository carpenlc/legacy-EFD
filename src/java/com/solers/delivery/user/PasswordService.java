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


/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public interface PasswordService {
    
    /**
     * @param username
     * @return The current password for the user with id {@code id}
     */
    String getPassword(String username);
    
    /**
     * @param username
     * @return True if the password for {@code user} is expired or needs to be reset
     */
    boolean isPasswordExpired(String username);
    
    /**
     * Change the password for {@code username} to {@code newPassword}. 
     * 
     * The users current password must match {@code oldPassword}
     * @param username
     * @param oldpassword
     * @param newPassword
     */
    void changePassword(String username, String oldpassword, String newPassword);
    
    /**
     * Update the password for {@code user} to {@code newPassword}
     * @param username
     * @param newPassword
     */
    void updatePassword(String username, String newPassword);
}
