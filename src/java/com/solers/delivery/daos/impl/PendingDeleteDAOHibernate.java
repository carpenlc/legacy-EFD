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

import com.solers.delivery.daos.PendingDeleteDAO;
import com.solers.delivery.domain.PendingDelete;
import com.solers.util.dao.GenericHibernateDAO;

import org.apache.log4j.Logger;
import org.hibernate.Query;

import java.util.List;
import java.util.Calendar;

/**
 * @author JGimourginas
 */
public class PendingDeleteDAOHibernate extends GenericHibernateDAO<PendingDelete, Long> implements PendingDeleteDAO {

    private static Logger log = Logger.getLogger(PendingDeleteDAOHibernate.class);
    private static final boolean IS_DEBUG_ENABLED = log.isDebugEnabled();

    public PendingDelete getByPath(Long consumerContentSetId, String path) {
        PendingDelete retVal = null;
        Query q = getSession().getNamedQuery(PendingDeleteDAO.GET_BY_PATH);
        q.setParameter("consumerId", consumerContentSetId);
        q.setParameter("path", path);
        List<?> results = q.list();
        if (results != null && results.size() > 0) {
            retVal = (PendingDelete) results.get(0);
            if (IS_DEBUG_ENABLED)
                log.debug("Getting pending delete by path got " + retVal.getPath());
        }
        return retVal;
    }

    public PendingDelete getNextToDelete(Long consumerContentSetId, Calendar date) {
        PendingDelete retVal = null;
        Query q = getSession().getNamedQuery(PendingDeleteDAO.GET_NEXT_TO_DELETE);
        q.setParameter("consumerId", consumerContentSetId);
        q.setParameter("adjustedTime", date);
        List<?> results = q.list();
        if (results != null && results.size() > 0) {
            retVal = (PendingDelete) results.get(0);
            if (IS_DEBUG_ENABLED)
                log.debug("Getting next to delete got " + retVal.getPath());
        }
        return retVal;
    }

}
