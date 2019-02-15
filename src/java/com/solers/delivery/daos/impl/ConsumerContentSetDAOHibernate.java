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

import com.solers.delivery.daos.ConsumerContentSetDAO;
import com.solers.delivery.domain.ConsumerContentSet;


/**
 * @author DMartin
 * This class extends the generic DAO operations to provide methods for each of the named queries
 * associated with a consumer content set. The queries are performed via hibernate queries.
 */
public class ConsumerContentSetDAOHibernate extends BaseContentSetHibernateDAO<ConsumerContentSet, Long> implements ConsumerContentSetDAO {

    /**
     * Perform hibernate query on the consumer_set_info and content_set tables.
     * @param name - The logical name of the content set.
     * @return - A single consumer content set whose logical name matches the parameter 'name'.
     */
    public ConsumerContentSet getConsumerByName(String name) {
        ConsumerContentSet retVal = null;
        Query q = getSession().getNamedQuery(ConsumerContentSetDAO.GET_CONSUMER_BY_NAME);
        q.setParameter("name", name);
        retVal = (ConsumerContentSet) q.uniqueResult();
        return retVal;
    }

    /**
     * Perform hibernate query on the consumer_set_info and content_set tables.
     * @return - A complete list of consumer content sets.
     */
    @SuppressWarnings("unchecked")
    public List<ConsumerContentSet> getConsumerSets() {
        Query q = getSession().getNamedQuery(ConsumerContentSetDAO.GET_CONSUMER_SETS);
        return (List<ConsumerContentSet>) q.list();
    }
}
