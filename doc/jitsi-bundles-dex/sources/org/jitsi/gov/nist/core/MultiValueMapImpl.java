package org.jitsi.gov.nist.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class MultiValueMapImpl<V> implements MultiValueMap<String, V>, Cloneable {
    private static final long serialVersionUID = 4275505380960964605L;
    private HashMap<String, ArrayList<V>> map = null;

    public List<V> put(String key, V value) {
        ArrayList<V> keyList = null;
        if (this.map != null) {
            keyList = (ArrayList) this.map.get(key);
        }
        if (keyList == null) {
            keyList = new ArrayList();
            getMap().put(key, keyList);
        }
        keyList.add(value);
        return keyList;
    }

    public boolean containsValue(Object value) {
        Set pairs = null;
        if (this.map != null) {
            pairs = this.map.entrySet();
        }
        if (pairs == null) {
            return false;
        }
        for (Entry value2 : pairs) {
            if (((ArrayList) value2.getValue()).contains(value)) {
                return true;
            }
        }
        return false;
    }

    public void clear() {
        if (this.map != null) {
            for (Entry value : this.map.entrySet()) {
                ((ArrayList) value.getValue()).clear();
            }
            this.map.clear();
        }
    }

    public Collection values() {
        if (this.map == null) {
            return new ArrayList();
        }
        Collection returnList = new ArrayList(this.map.size());
        for (Entry value : this.map.entrySet()) {
            Object[] values = ((ArrayList) value.getValue()).toArray();
            for (Object add : values) {
                returnList.add(add);
            }
        }
        return returnList;
    }

    public Object clone() {
        MultiValueMapImpl obj = new MultiValueMapImpl();
        if (this.map != null) {
            obj.map = (HashMap) this.map.clone();
        }
        return obj;
    }

    public int size() {
        if (this.map == null) {
            return 0;
        }
        return this.map.size();
    }

    public boolean containsKey(Object key) {
        if (this.map == null) {
            return false;
        }
        return this.map.containsKey(key);
    }

    public Set entrySet() {
        if (this.map == null) {
            return new HashSet();
        }
        return this.map.entrySet();
    }

    public boolean isEmpty() {
        if (this.map == null) {
            return true;
        }
        return this.map.isEmpty();
    }

    public Set<String> keySet() {
        if (this.map == null) {
            return new HashSet();
        }
        return this.map.keySet();
    }

    public Object remove(String key, V item) {
        if (this.map == null) {
            return null;
        }
        ArrayList<V> list = (ArrayList) this.map.get(key);
        if (list != null) {
            return Boolean.valueOf(list.remove(item));
        }
        return null;
    }

    public List<V> get(Object key) {
        if (this.map == null) {
            return null;
        }
        return (List) this.map.get(key);
    }

    public List<V> put(String key, List<V> value) {
        return (List) getMap().put(key, (ArrayList) value);
    }

    public List<V> remove(Object key) {
        if (this.map == null) {
            return null;
        }
        return (List) this.map.remove(key);
    }

    public void putAll(Map<? extends String, ? extends List<V>> mapToPut) {
        for (String k : mapToPut.keySet()) {
            ArrayList<V> al = new ArrayList();
            al.addAll((Collection) mapToPut.get(k));
            getMap().put(k, al);
        }
    }

    public HashMap<String, ArrayList<V>> getMap() {
        if (this.map == null) {
            this.map = new HashMap(0);
        }
        return this.map;
    }
}
