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

import com.solers.delivery.domain.User;
import com.solers.util.dao.GenericDAO;

/**
 * Created by IntelliJ IDEA.
 * User: mhasan
 * Date: Mar 21, 2007
 * Time: 2:32:06 PM
 * To change this template use File | Settings | File Templates.
 */
public interface UserDAO extends GenericDAO<User, Long> {

    public static final String GET_USER_BY_USERNAME = "getUserByUsername";

    public User getUserByUsername(String username);
}
