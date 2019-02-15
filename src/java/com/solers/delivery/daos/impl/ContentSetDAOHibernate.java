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
package com.solers.delivery.daos.impl;

import java.util.List;

import org.hibernate.Query;

import com.solers.delivery.daos.ContentSetDAO;
import com.solers.delivery.domain.ContentSet;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author DMartin This class extends the generic DAO operations to provide methods for each of the named queries associated with a content set. The queries are
 *         performed via hibernate queries.
 */
@Transactional
public class ContentSetDAOHibernate extends BaseContentSetHibernateDAO<ContentSet, Long> implements ContentSetDAO {

    /**
     * Perform hibernate query on the content_set table.
     * 
     * @param name -
     *            The logical name of the supplier content set.
     * @return - A single content set whose logical name matches the parameter 'name'.
     */
    public ContentSet getSupplierByName(String name) {
        ContentSet retVal = null;
        Query q = getSession().getNamedQuery(ContentSetDAO.GET_SUPPLIER_BY_NAME);
        q.setParameter("name", name);
        retVal = (ContentSet) q.uniqueResult();
        return retVal;
    }

    /**
     * Perform hibernate query on the content_set table.
     * 
     * @return - A complete list of supplier content sets.
     */
    @SuppressWarnings("unchecked")
    public List<ContentSet> getSupplierSets() {
        List<ContentSet> retVal = null;
        Query q = getSession().getNamedQuery(ContentSetDAO.GET_SUPPLIER_SETS);
        retVal = (List<ContentSet>) q.list();
        return retVal;
    }
}
