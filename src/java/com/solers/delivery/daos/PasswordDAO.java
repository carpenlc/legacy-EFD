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
package com.solers.delivery.daos;

import java.util.List;

import com.solers.delivery.domain.Password;
import com.solers.util.dao.GenericDAO;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public interface PasswordDAO extends GenericDAO<Password, Long> {
    
    String GET_PASSWORDS = "getPasswords";
    String DELETE_OLDEST = "deleteOldest";
    String DELETE_ALL = "deleteAll";
    
    List<Password> getPasswords(Long id);
    
    /**
     * Drop the oldest password for the user with id {@code id}
     * @param id
     */
    void deleteOldest(Long id);
    
    /**
     * Delete all passwords for the user with id {@code id}
     * @param id
     */
    void deleteAll(Long id);
    
}
