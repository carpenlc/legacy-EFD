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
package com.solers.util.dao;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Example;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;


/**
 * GenericHibernateDAO implements the basic CRUD operations for Hibernate. This class should be used as a base class for all hibernate implementations of
 * persistent entity DAOs. The Session member variable is set using setter injection and the DAO itself has no (and should have no) impact on the scope of the
 * Session or on Transaction control.
 * 
 * The SuppressWarnings annotations are needed because Hibernate interfaces are JDK 1.4 and will therefore display warning messages about unchecked types. Once
 * Hibernate interfaces are upgraded, these can be removed.
 * 
 * @author JGimourginas
 */
public class GenericHibernateDAO<T, ID extends Serializable> implements GenericDAO<T, ID> {

    private static final Logger log = Logger.getLogger(GenericHibernateDAO.class);

    private final ClassValidator<T> validator;
    
    private Class<T> persistentClass;
    private Session session;

    @SuppressWarnings("unchecked")
    public GenericHibernateDAO() {
        this.persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        this.validator =  new ClassValidator<T>(this.persistentClass);
    }
    
    public GenericHibernateDAO(Class<T> persistentClass) {
        this.persistentClass = persistentClass;
        this.validator = new ClassValidator<T>(this.persistentClass);
    }

    public void setSession(Session s) {
        this.session = s;
    }

    public Session getSession() {
        if (session == null)
            throw new IllegalStateException("Session has not been set on DAO before usage");
        return session;
    }

    public Class<T> getPersistentClass() {
        return persistentClass;
    }

    @SuppressWarnings("unchecked")
    public T findById(ID id, boolean lock) {
        T entity;
        if (lock) {
            entity = (T) getSession().load(getPersistentClass(), id, LockMode.UPGRADE);
        } else {
            entity = (T) getSession().load(getPersistentClass(), id);
        }

        return entity;
    }
    
    /**
     * Get an entity.
     * 
     * The semantics of getById are different than findById in that find expects
     * an entity to be there and will throw an exception if the entity referred
     * to by {@code id} does not exit.  get returns null
     * @see com.solers.util.dao.GenericDAO#getById(java.io.Serializable)
     */
    @SuppressWarnings("unchecked")
    public T getById(ID id) {
        return (T) getSession().get(getPersistentClass(), id);
    }

    public List<T> findAll() {
        return findByCriteria();
    }

    @SuppressWarnings("unchecked")
    public List<T> findByExample(T exampleInstance, String[] excludeProperty) {
        Criteria crit = getSession().createCriteria(getPersistentClass());
        Example example = Example.create(exampleInstance);
        for (String exclude : excludeProperty) {
            example.excludeProperty(exclude);
        }
        crit.add(example);
        return crit.list();
    }

    public T makePersistent(T entity) {
        try {
            validate(entity);
            getSession().saveOrUpdate(entity);
        } catch (HibernateException e) {
            log.error("Error making persistent: " + entity, e);
        }
        return entity;
    }

    public void makeTransient(T entity) {
        try {
            getSession().delete(entity);
        } catch (HibernateException e) {
            log.error("Error making transient: " + entity, e);
        }
    }
    
    public void makeTransientById(Collection<ID> ids) {
        for (ID id : ids) {
            T entity = getById(id);
            if (entity != null) {
                makeTransient(entity);
            }
        }
    }

    public void flush() {
        getSession().flush();
    }

    public void clear() {
        getSession().clear();
    }

    /**
     * Use this inside subclasses as a convenience method.
     */
    @SuppressWarnings("unchecked")
    protected List<T> findByCriteria(Criterion... criterion) {
        Criteria crit = getSession().createCriteria(getPersistentClass());
        for (Criterion c : criterion) {
            crit.add(c);
        }
        
        return crit.list();
    }
    
    protected void validate(T entity) {
        InvalidValue [] errors = validator.getInvalidValues(entity);
        if (errors.length > 0) {
            ValidationException ex = new ValidationException();
            for (InvalidValue v : errors) {
                ex.addMessage(v.getMessage());
            }
            throw ex;
        }
    }

}
