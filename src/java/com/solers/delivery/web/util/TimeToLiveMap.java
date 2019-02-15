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
package com.solers.delivery.web.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.solers.util.unit.TimeIntervalUnit;

/**
 * <p>Time to live Map implementation.</p>  
 * 
 * <p>Entries are expired as they are requested or
 * any time the entire map is read: {@code keySet()}, {@code values()}, 
 * {@code entrySet()}, {@code containsKey(Object)}, {@code containsValue(Object)}
 * </p>
 * 
 * <p>This map is not thread safe</p>
 * 
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class TimeToLiveMap<K,V> implements Map<K,V> {
    
    /**
     * Default ttl of 60 minutes
     */
    private static final Long DEFAULT_TTL = 3600000L;
    
    private final Map<K,TimeToLiveEntry<K,V>> map;
    private final long ttl;
    
    public TimeToLiveMap() {
        this(DEFAULT_TTL);
    }
    
    public TimeToLiveMap(TimeIntervalUnit unit, Long value) {
        this(unit.toMillis(value));
    }
    
    /**
     * Constructor
     * @param milliseconds Time to live specified in milliseconds
     */
    public TimeToLiveMap(Long milliseconds) {
        map = new ConcurrentHashMap<K,TimeToLiveEntry<K,V>>();
        ttl = milliseconds.longValue();
    }
    
    public void clear() {
        map.clear();
    }

    public boolean containsKey(Object key) {
        return keySet().contains(key);
    }
    
    public boolean containsValue(Object value) {
        return values().contains(value);
    }
    
    public boolean isEmpty() {
        return entrySet().isEmpty();
    }
    
    public int size() {
        return entrySet().size();
    }

    public Set<Map.Entry<K,V>> entrySet() {
        Set<Map.Entry<K,V>> result = new HashSet<Map.Entry<K,V>>();
        
        for (TimeToLiveEntry<K,V> entry : map.values()) {
            if ( !expireAndRemove(entry) ) {
                result.add( entry );
            }
        }
        
        return result;
    }
    
    public Set<K> keySet() {
        Set<K> result = new HashSet<K>();
        
        for (TimeToLiveEntry<K,V> entry : map.values()) {
            if ( !expireAndRemove(entry) ) {
                result.add( entry.getKey() );
            }
        }
        
        return result;
    }
    
    public Collection<V> values() {
        Collection<V> result = new ArrayList<V>();
        for (TimeToLiveEntry<K,V> entry : map.values()) {
            if ( !expireAndRemove(entry) ) {
                result.add( entry.getValue() );
            }
        }
       
        return result;
    }

    public V get(Object key) {
        V result = null;
        TimeToLiveEntry<K,V> entry = map.get(key);
        
        if ( entry != null ) {
            if ( !expireAndRemove(entry) ) {
                result = entry.getValue();
            }
        }
        
        return result;
    }

    public V put(K key, V value) {
        TimeToLiveEntry<K,V> previous = map.put(key, new TimeToLiveEntry<K,V>(key,value, getTTL()));
        return previous == null ? null : previous.isExpired() ? null : previous.getValue();
    }

    public void putAll(Map<? extends K, ? extends V> t) {
        for (Map.Entry<? extends K, ? extends V> entry : t.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public V remove(Object key) {
        TimeToLiveEntry<K,V> previous = map.remove(key);
        return previous == null ? null : previous.isExpired() ? null : previous.getValue();
    }
    
    /**
     * Set the value marked by key to expire in the given time amount
     * @param key
     * @param unit
     * @param value
     */
    public void expireKey(K key, TimeIntervalUnit unit, long value) {
        long ttl = unit.toMillis(value);
        TimeToLiveEntry<K,V> entry = map.get(key);
        
        if (entry != null) {
            entry = new TimeToLiveEntry<K,V>(entry.key, entry.value, ttl);
            map.put(key, entry);
        }
        
    }

    protected long getTTL() {
        return ttl;
    }
    
    protected boolean expireAndRemove(TimeToLiveEntry<K,V> entry) {
        boolean expired = entry.isExpired();
        if (expired) {
            map.remove(entry.getKey());
        }
        return expired;
    }
    
    private static class TimeToLiveEntry<K,V> implements Map.Entry<K,V> {
        
        protected final K key;
        private final long created;
        
        protected V value;
        private long ttl;
        
        TimeToLiveEntry(K key, V value, long ttl) {
            this.key = key;
            this.created = System.currentTimeMillis();
            this.value = value;
            this.ttl = ttl;
        }
        
        public void setTTL(long ttl) {
            this.ttl = ttl;
        }
        
        public boolean isExpired() {
            long current = System.currentTimeMillis();
            long difference = current - created;
            return difference >= ttl;
        }
        
        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public V setValue(V value) {
            V old = value;
            this.value = value;
            return old;
        }

        public int hashCode() {
            return getKey().hashCode() | getValue().hashCode();
        }
        
        @SuppressWarnings("unchecked")
        public boolean equals(Object o) {
            if (o instanceof Map.Entry) {
                Map.Entry e = (Map.Entry) o;
                return e.getKey().equals(getKey()) && e.getValue().equals(getValue()); 
            }
            return false;
        }
 
    }
}
