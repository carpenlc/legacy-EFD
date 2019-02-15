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
package com.solers.delivery.content;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.solers.delivery.daos.AllowedHostDAO;
import com.solers.delivery.daos.DAOFactory;
import com.solers.delivery.domain.AllowedHost;
import com.solers.delivery.domain.ContentSet;
import com.solers.security.audit.Auditable;
import com.solers.security.audit.Auditable.Action;
import com.solers.security.audit.Auditable.Severity;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class AllowedHostServiceImpl implements AllowedHostService {

    private final DAOFactory factory;
    private final ContentService service;
    
    public AllowedHostServiceImpl(DAOFactory factory, ContentService service) {
        this.factory = factory;
        this.service = service;
    }

    @Override
    @Transactional
    @Auditable(action = Action.MODIFY, severity = Severity.MEDIUM)
    public Long save(AllowedHost host) {
        dao().makePersistent(host);
        return host.getId();
    }

    @Override
    @Transactional
    public List<AllowedHost> list() {
        return dao().findAll();
    }

    @Override
    @Transactional
    @Auditable(action = Action.DELETE, severity = Severity.MEDIUM)
    public void remove(AllowedHost host) {
        dao().makeTransient(host);
        
        for (ContentSet c : service.getSuppliers()) {
            if (c.removeAllowedHost(host)) {
                service.save(c);
            }
        }
    }
    
    @Override
    @Transactional
    public AllowedHost get(String alias) {
        return dao().getByAlias(alias);
    }
    
    private AllowedHostDAO dao() {
        return factory.getAllowedHostDAO();
    }
}
