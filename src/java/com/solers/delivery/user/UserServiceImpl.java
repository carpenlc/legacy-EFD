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

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.solers.delivery.daos.DAOFactory;
import com.solers.delivery.daos.UserDAO;
import com.solers.delivery.domain.User;
import com.solers.security.audit.Auditable;
import com.solers.security.audit.Auditable.Action;
import com.solers.security.audit.Auditable.Severity;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class UserServiceImpl implements UserService {

    private final DAOFactory factory;
    
    public UserServiceImpl(DAOFactory factory) {
        this.factory = factory;
    }

    @Override
    @Transactional
    public User get(Long id) {
        return dao().getById(id);
    }

    @Override
    @Transactional
    public User get(String username) {
        return dao().getUserByUsername(username);
    }

    @Override
    @Transactional
    public List<User> getUsers() {
        return dao().findAll();
    }

    @Override
    @Transactional
    @Auditable(action = Action.MODIFY, severity = Severity.MEDIUM)
    public Long save(User user) {
        dao().makePersistent(user);
        return user.getId();
    }
    
    @Override
    @Transactional
    @Auditable(action = Action.DISABLE, severity = Severity.MEDIUM)
    public void disable(Long id) {
        User u = get(id);
        if (u.isEnabled()) {
            u.setEnabled(false);
        }
    }

    @Override
    @Transactional
    @Auditable(action = Action.ENABLE, severity = Severity.MEDIUM)
    public void enable(Long id) {
        User u = get(id);
        if (!u.isEnabled()) {
            u.setEnabled(true);
            u.setFailedLogins(0);
        }
    }

    @Override
    @Transactional
    @Auditable(action = Action.DELETE, severity = Severity.MEDIUM)
    public void remove(Long id) {
        factory.getPasswordDAO().deleteAll(id);
        dao().makeTransient(get(id));
    }

    private UserDAO dao() {
        return factory.getUserDAO();
    }
}
