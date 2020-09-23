package org.jitsi.impl.configuration;

import java.util.Hashtable;
import java.util.Set;

public abstract class HashtableConfigurationStore<T extends Hashtable> implements ConfigurationStore {
    protected final T properties;

    protected HashtableConfigurationStore(T properties) {
        this.properties = properties;
    }

    public Object getProperty(String name) {
        Object value = this.properties.get(name);
        return value != null ? value : System.getProperty(name);
    }

    public String[] getPropertyNames() {
        String[] strArr;
        synchronized (this.properties) {
            Set<?> propertyNames = this.properties.keySet();
            strArr = (String[]) propertyNames.toArray(new String[propertyNames.size()]);
        }
        return strArr;
    }

    public boolean isSystemProperty(String name) {
        return System.getProperty(name) != null;
    }

    public void removeProperty(String name) {
        this.properties.remove(name);
    }

    public void setNonSystemProperty(String name, Object value) {
        this.properties.put(name, value);
    }

    public void setSystemProperty(String name) {
        removeProperty(name);
    }
}
