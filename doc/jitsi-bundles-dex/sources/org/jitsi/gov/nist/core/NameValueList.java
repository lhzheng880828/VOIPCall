package org.jitsi.gov.nist.core;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class NameValueList implements Serializable, Cloneable, Map<String, NameValue> {
    private static final long serialVersionUID = -6998271876574260243L;
    private Map<String, NameValue> hmap;
    private String separator = Separators.SEMICOLON;
    private boolean sync = false;

    public NameValueList(boolean sync) {
        this.sync = sync;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public String encode() {
        return encode(new StringBuilder()).toString();
    }

    public StringBuilder encode(StringBuilder buffer) {
        if (!isEmpty()) {
            Iterator<NameValue> iterator = iterator();
            if (iterator.hasNext()) {
                while (true) {
                    GenericObject obj = iterator.next();
                    if (obj instanceof GenericObject) {
                        obj.encode(buffer);
                    } else {
                        buffer.append(obj.toString());
                    }
                    if (!iterator.hasNext()) {
                        break;
                    }
                    buffer.append(this.separator);
                }
            }
        }
        return buffer;
    }

    public String toString() {
        return encode();
    }

    public void set(NameValue nv) {
        put(nv.getName().toLowerCase(), nv);
    }

    public void set(String name, Object value) {
        put(name.toLowerCase(), new NameValue(name, value));
    }

    public boolean equals(Object otherObject) {
        if (otherObject == null || !otherObject.getClass().equals(getClass())) {
            return false;
        }
        NameValueList other = (NameValueList) otherObject;
        if (size() != size()) {
            return false;
        }
        Iterator<String> li = getNames();
        while (li.hasNext()) {
            Object key = (String) li.next();
            NameValue nv1 = getNameValue(key);
            NameValue nv2 = other.get(key);
            if (nv2 == null) {
                return false;
            }
            if (!nv2.equals(nv1)) {
                return false;
            }
        }
        return true;
    }

    public Object getValue(String name) {
        return getValue(name, true);
    }

    public Object getValue(String name, boolean stripQuotes) {
        NameValue nv = getNameValue(name.toLowerCase());
        if (nv != null) {
            return nv.getValueAsObject(stripQuotes);
        }
        return null;
    }

    public NameValue getNameValue(String name) {
        if (this.hmap == null) {
            return null;
        }
        return (NameValue) this.hmap.get(name.toLowerCase());
    }

    public boolean hasNameValue(String name) {
        return containsKey(name.toLowerCase());
    }

    public boolean delete(String name) {
        Object lcName = name.toLowerCase();
        if (!containsKey(lcName)) {
            return false;
        }
        remove(lcName);
        return true;
    }

    public Object clone() {
        NameValueList retval = new NameValueList();
        retval.setSeparator(this.separator);
        if (this.hmap != null) {
            Iterator<NameValue> it = iterator();
            while (it.hasNext()) {
                retval.set((NameValue) ((NameValue) it.next()).clone());
            }
        }
        return retval;
    }

    public int size() {
        if (this.hmap == null) {
            return 0;
        }
        return this.hmap.size();
    }

    public boolean isEmpty() {
        if (this.hmap == null) {
            return true;
        }
        return this.hmap.isEmpty();
    }

    public Iterator<NameValue> iterator() {
        return getMap().values().iterator();
    }

    public Iterator<String> getNames() {
        return getMap().keySet().iterator();
    }

    public String getParameter(String name) {
        return getParameter(name, true);
    }

    public String getParameter(String name, boolean stripQuotes) {
        Object val = getValue(name, stripQuotes);
        if (val == null) {
            return null;
        }
        if (val instanceof GenericObject) {
            return ((GenericObject) val).encode();
        }
        return val.toString();
    }

    public void clear() {
        if (this.hmap != null) {
            this.hmap.clear();
        }
    }

    public boolean containsKey(Object key) {
        if (this.hmap == null) {
            return false;
        }
        return this.hmap.containsKey(key.toString().toLowerCase());
    }

    public boolean containsValue(Object value) {
        if (this.hmap == null) {
            return false;
        }
        return this.hmap.containsValue(value);
    }

    public Set<Entry<String, NameValue>> entrySet() {
        if (this.hmap == null) {
            return new HashSet();
        }
        return this.hmap.entrySet();
    }

    public NameValue get(Object key) {
        if (this.hmap == null) {
            return null;
        }
        return (NameValue) this.hmap.get(key.toString().toLowerCase());
    }

    public Set<String> keySet() {
        if (this.hmap == null) {
            return new HashSet();
        }
        return this.hmap.keySet();
    }

    public NameValue put(String name, NameValue nameValue) {
        return (NameValue) getMap().put(name, nameValue);
    }

    public void putAll(Map<? extends String, ? extends NameValue> map) {
        getMap().putAll(map);
    }

    public NameValue remove(Object key) {
        if (this.hmap == null) {
            return null;
        }
        return (NameValue) getMap().remove(key.toString().toLowerCase());
    }

    public Collection<NameValue> values() {
        return getMap().values();
    }

    public int hashCode() {
        return getMap().keySet().hashCode();
    }

    /* access modifiers changed from: protected */
    public Map<String, NameValue> getMap() {
        if (this.hmap == null) {
            if (this.sync) {
                this.hmap = new ConcurrentHashMap(0);
            } else {
                this.hmap = new LinkedHashMap(0);
            }
        }
        return this.hmap;
    }
}
