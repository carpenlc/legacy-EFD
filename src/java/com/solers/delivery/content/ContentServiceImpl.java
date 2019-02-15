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

import java.util.ArrayList;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.solers.delivery.content.consumer.ConsumerContentSetManager;
import com.solers.delivery.content.supplier.SupplierContentSetManager;
import com.solers.delivery.daos.AllowedHostDAO;
import com.solers.delivery.daos.DAOFactory;
import com.solers.delivery.domain.AllowedHost;
import com.solers.delivery.domain.ConsumerContentSet;
import com.solers.delivery.domain.ContentSet;
import com.solers.delivery.inventory.InventoryFactory;
import com.solers.security.audit.Auditable;
import com.solers.security.audit.Auditable.Action;
import com.solers.util.dao.ValidationException;

/**
 * Backend implementation of ContentSetService
 * 
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class ContentServiceImpl implements ContentService {
    
    private final DAOFactory factory;
    private final SupplierContentSetManager supplierManager;
    private final ConsumerContentSetManager consumerManager;
    
    public ContentServiceImpl(DAOFactory factory, SupplierContentSetManager supplierManager, ConsumerContentSetManager consumerManager) {
        this.factory = factory;
        this.supplierManager = supplierManager;
        this.consumerManager = consumerManager;
    }
    
    /**
     * Initialize the content set managers
     */
    @Transactional
    public void initialize() {
        initialize(factory.getContentSetDAO().getSupplierSets(), supplierManager);
        initialize(factory.getConsumerContentSetDAO().getConsumerSets(), consumerManager);
    }
    
    /**
     * For each content set, add it to the given manager
     * @param contentSets
     * @param manager
     */
    protected void initialize(List<? extends ContentSet> contentSets, ContentSetManager manager) {
        for (ContentSet c : contentSets) {
            manager.registerContentSet(c);
        }
    }
    
    /**
     * @see com.solers.delivery.content.ContentService#get(java.lang.Long)
     */
    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public <T extends ContentSet> T get(Long id) {
        return (T) factory.getContentSetDAO().getById(id);
    }

    @Override
    @Transactional
    public List<ContentSet> getContentSets() {
        List<ContentSet> result = new ArrayList<ContentSet>();
        
        result.addAll(getSuppliers());
        result.addAll(getConsumers());
        
        return result;
    }
    
    @Override
    @Transactional
    public List<ContentSet> getSuppliers() {
        List<ContentSet> result = new ArrayList<ContentSet>();
        
        result.addAll(factory.getContentSetDAO().getSupplierSets());
        
        return result;
    }
    
    @Override
    @Transactional
    public List<ConsumerContentSet> getConsumers() {
        List<ConsumerContentSet> result = new ArrayList<ConsumerContentSet>();
        
        result.addAll(factory.getConsumerContentSetDAO().getConsumerSets());
        
        return result;
    }

    /**
     * Removes the given content set and notifies the appropriate content
     * set manager of the change
     * 
     */
    @Override
    @Transactional
    @Auditable(action=Action.DELETE)
    public void remove(Long id) {
        ContentSet contentSet = get(id);
        InventoryFactory.cleanup(contentSet);
        if (contentSet.isSupplier()) {
            supplierManager.unregisterContentSet(contentSet.getId());
            factory.getContentSetDAO().makeTransient(contentSet);
        } else {
            consumerManager.unregisterContentSet(contentSet.getId());
            factory.getConsumerContentSetDAO().makeTransient((ConsumerContentSet) contentSet);
        }
    }

    /**
     * Persists the given content set and notifies the appropriate content
     * set manager of the change
     * 
     * @see com.solers.delivery.content.ContentService#save(com.solers.delivery.domain.ContentSet)
     */
    @Override
    @Transactional
    @Auditable(action=Action.MODIFY)
    public Long save(ContentSet contentSet) throws ValidationException {
        contentSet.setUpdateTime(System.currentTimeMillis());
        if (contentSet.isSupplier()) {
            checkAllowedHosts(contentSet);
            factory.getContentSetDAO().makePersistent(contentSet);
        } else {
            factory.getConsumerContentSetDAO().makePersistent(((ConsumerContentSet) contentSet));
        }
        notifyManager(contentSet);
        return contentSet.getId();
    }

    @Override
    @Transactional
    @Auditable(action=Action.DISABLE)
    public void disable(Long id) {
        ContentSet contentSet = get(id);
        if (contentSet.isEnabled()) {
            contentSet.setEnabled(false);
            notifyManager(contentSet);
        }
    }

    @Override
    @Transactional
    @Auditable(action=Action.ENABLE)
    public void enable(Long id) {
        ContentSet contentSet = get(id);
        if (!contentSet.isEnabled()) {
            contentSet.setEnabled(true);
            notifyManager(contentSet);
        }
    }
    
    private void notifyManager(ContentSet contentSet) {
        if (contentSet.isSupplier()) {
            supplierManager.registerContentSet(contentSet);
        } else {
            consumerManager.registerContentSet(contentSet);
        }
    }
    
    private void checkAllowedHosts(ContentSet contentSet) {
        AllowedHostDAO dao = factory.getAllowedHostDAO();
        for (AllowedHost a : contentSet.getAllowedHosts()) {
            AllowedHost existing = dao.getByAlias(a.getAlias());
            if (existing == null) {
                dao.makePersistent(a);
            } else {
                // Ensure that the actual hibernate instance is saved
                // to avoid "transient instance" errors
                contentSet.removeAllowedHost(a);
                contentSet.addAllowedHost(existing);
            }
        }
    }
}
