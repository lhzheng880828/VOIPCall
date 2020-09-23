package org.jitsi.impl.configuration;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;

public class SortedProperties extends Properties {
    private static final long serialVersionUID = 0;

    public synchronized Enumeration<Object> keys() {
        final Object[] keys;
        keys = keySet().toArray();
        Arrays.sort(keys);
        return new Enumeration<Object>() {
            private int i = 0;

            public boolean hasMoreElements() {
                return this.i < keys.length;
            }

            public Object nextElement() {
                Object[] objArr = keys;
                int i = this.i;
                this.i = i + 1;
                return objArr[i];
            }
        };
    }

    public synchronized Object put(Object key, Object value) {
        Object obj;
        if (key.toString().trim().length() == 0) {
            obj = null;
        } else {
            obj = super.put(key, value);
        }
        return obj;
    }
}
