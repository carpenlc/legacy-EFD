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

import java.util.Collection;
import java.util.List;

import org.hibernate.SQLQuery;

import com.solers.delivery.daos.AlertDAO;
import com.solers.delivery.domain.Alert;
import com.solers.delivery.domain.Alert.AlertType;
import com.solers.util.Page;
import com.solers.util.dao.GenericHibernateDAO;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class AlertDAOHibernate extends GenericHibernateDAO<Alert, Long> implements AlertDAO {
    
    @Override
    @SuppressWarnings("unchecked")
    public Page<Alert> listBy(AlertType type, int startIndex, int max) {
        SQLQuery q = getSession().createSQLQuery("select count(id) from alerts where type = :type or type = :all");
        q.setParameter("type", type.ordinal());
        q.setParameter("all", AlertType.ALL.ordinal());
        
        Integer count = (Integer) q.uniqueResult();
        
        // Derby does not support "order by" in subqueries so we have to 
        // select from the end of the result set.
        int ascendingEnd = startIndex + max;
        int descendingStart = count - ascendingEnd;
        int descendingEnd = count - startIndex;
        
        if (descendingStart < 0) {
            descendingStart = 0;
        }
        
        q = getSession().createSQLQuery("select * from (select row_number() over() as rownum, alerts.* from alerts where type = :type or type = :all) as tmp where rownum > :start and rownum <= :end order by timestamp desc");
        q.setParameter("type", type.ordinal());
        q.setParameter("all", AlertType.ALL.ordinal());
        q.setParameter("start", descendingStart);
        q.setParameter("end", descendingEnd);
        q.addEntity(Alert.class);
        
        List<Alert> list = (List<Alert>) q.list();
        return new Page<Alert>(count, list);
    }

    @Override
    public void makeTransientById(Collection<Long> ids) {
        getSession().createSQLQuery("delete from alerts where id in (:ids)")
            .setParameterList("ids", ids).executeUpdate();        
        
    }

}
