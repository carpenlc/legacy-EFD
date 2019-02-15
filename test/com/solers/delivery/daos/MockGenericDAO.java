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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.solers.util.dao.GenericDAO;
import com.solers.util.dao.ValidationException;

public abstract class MockGenericDAO<T, ID extends Serializable> implements GenericDAO<T, ID> {
    
    Map<ID,T> data = new HashMap<ID,T>();
    
    public boolean throwOnSave = false;
    
    @Override
    public List<T> findAll() {
        return new ArrayList<T>(data.values());
    }

    @Override
    public T findById(ID id, boolean lock) {
        return data.get(id);
    }

    @Override
    public T getById(ID id) {
        return findById(id, true);
    }
    
    @Override
    public List<T> findByExample(T exampleInstance, String[] excludeProperty) {
        return null;
    }
    
    @Override
    public void flush() {
        
    }

    protected void throwValidationException() {
       throw new ValidationException("test1", "test2", "test3");
    }
}
