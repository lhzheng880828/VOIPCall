package org.jitsi.impl.configuration;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.jitsi.service.configuration.ConfigVetoableChangeListener;

public class ChangeEventDispatcher {
    private Map<String, ChangeEventDispatcher> propertyChangeChildren;
    private List<PropertyChangeListener> propertyChangeListeners;
    private final Object source;
    private Map<String, ChangeEventDispatcher> vetoableChangeChildren;
    private List<ConfigVetoableChangeListener> vetoableChangeListeners;

    public ChangeEventDispatcher(Object sourceObject) {
        if (sourceObject == null) {
            throw new NullPointerException("sourceObject");
        }
        this.source = sourceObject;
    }

    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        if (this.propertyChangeListeners == null) {
            this.propertyChangeListeners = new Vector();
        }
        this.propertyChangeListeners.add(listener);
    }

    public synchronized void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        if (this.propertyChangeChildren == null) {
            this.propertyChangeChildren = new Hashtable();
        }
        ChangeEventDispatcher child = (ChangeEventDispatcher) this.propertyChangeChildren.get(propertyName);
        if (child == null) {
            child = new ChangeEventDispatcher(this.source);
            this.propertyChangeChildren.put(propertyName, child);
        }
        child.addPropertyChangeListener(listener);
    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        if (this.propertyChangeListeners != null) {
            this.propertyChangeListeners.remove(listener);
        }
    }

    public synchronized void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        if (this.propertyChangeChildren != null) {
            ChangeEventDispatcher child = (ChangeEventDispatcher) this.propertyChangeChildren.get(propertyName);
            if (child != null) {
                child.removePropertyChangeListener(listener);
            }
        }
    }

    public synchronized void addVetoableChangeListener(ConfigVetoableChangeListener listener) {
        if (this.vetoableChangeListeners == null) {
            this.vetoableChangeListeners = new Vector();
        }
        this.vetoableChangeListeners.add(listener);
    }

    public synchronized void removeVetoableChangeListener(ConfigVetoableChangeListener listener) {
        if (this.vetoableChangeListeners != null) {
            this.vetoableChangeListeners.remove(listener);
        }
    }

    public synchronized void addVetoableChangeListener(String propertyName, ConfigVetoableChangeListener listener) {
        if (this.vetoableChangeChildren == null) {
            this.vetoableChangeChildren = new Hashtable();
        }
        ChangeEventDispatcher child = (ChangeEventDispatcher) this.vetoableChangeChildren.get(propertyName);
        if (child == null) {
            child = new ChangeEventDispatcher(this.source);
            this.vetoableChangeChildren.put(propertyName, child);
        }
        child.addVetoableChangeListener(listener);
    }

    public synchronized void removeVetoableChangeListener(String propertyName, ConfigVetoableChangeListener listener) {
        if (this.vetoableChangeChildren != null) {
            ChangeEventDispatcher child = (ChangeEventDispatcher) this.vetoableChangeChildren.get(propertyName);
            if (child != null) {
                child.removeVetoableChangeListener(listener);
            }
        }
    }

    public void fireVetoableChange(String propertyName, Object oldValue, Object newValue) {
        if (this.vetoableChangeListeners != null || this.vetoableChangeChildren != null) {
            fireVetoableChange(new PropertyChangeEvent(this.source, propertyName, oldValue, newValue));
        }
    }

    public void fireVetoableChange(PropertyChangeEvent evt) {
        Object oldValue = evt.getOldValue();
        Object newValue = evt.getNewValue();
        String propertyName = evt.getPropertyName();
        if (oldValue == null || newValue == null || !oldValue.equals(newValue)) {
            ConfigVetoableChangeListener[] targets = null;
            ChangeEventDispatcher child = null;
            synchronized (this) {
                if (this.vetoableChangeListeners != null) {
                    targets = (ConfigVetoableChangeListener[]) this.vetoableChangeListeners.toArray(new ConfigVetoableChangeListener[this.vetoableChangeListeners.size()]);
                }
                if (!(this.vetoableChangeChildren == null || propertyName == null)) {
                    child = (ChangeEventDispatcher) this.vetoableChangeChildren.get(propertyName);
                }
            }
            if (!(this.vetoableChangeListeners == null || targets == null)) {
                for (ConfigVetoableChangeListener target : targets) {
                    target.vetoableChange(evt);
                }
            }
            if (child != null) {
                child.fireVetoableChange(evt);
            }
        }
    }

    public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        if (oldValue == null || newValue == null || !oldValue.equals(newValue)) {
            firePropertyChange(new PropertyChangeEvent(this.source, propertyName, oldValue, newValue));
        }
    }

    public void firePropertyChange(PropertyChangeEvent evt) {
        Object oldValue = evt.getOldValue();
        Object newValue = evt.getNewValue();
        String propertyName = evt.getPropertyName();
        if (oldValue == null || newValue == null || !oldValue.equals(newValue)) {
            if (this.propertyChangeListeners != null) {
                for (PropertyChangeListener target : this.propertyChangeListeners) {
                    target.propertyChange(evt);
                }
            }
            if (this.propertyChangeChildren != null && propertyName != null) {
                ChangeEventDispatcher child = (ChangeEventDispatcher) this.propertyChangeChildren.get(propertyName);
                if (child != null) {
                    child.firePropertyChange(evt);
                }
            }
        }
    }

    public synchronized boolean hasPropertyChangeListeners(String propertyName) {
        boolean z = true;
        synchronized (this) {
            if (this.propertyChangeListeners == null || this.propertyChangeListeners.isEmpty()) {
                if (this.propertyChangeChildren != null) {
                    ChangeEventDispatcher child = (ChangeEventDispatcher) this.propertyChangeChildren.get(propertyName);
                    if (!(child == null || child.propertyChangeListeners == null)) {
                        if (child.propertyChangeListeners.isEmpty()) {
                            z = false;
                        }
                    }
                }
                z = false;
            }
        }
        return z;
    }

    public synchronized boolean hasVetoableChangeListeners(String propertyName) {
        boolean z = true;
        synchronized (this) {
            if (this.vetoableChangeListeners == null || this.vetoableChangeListeners.isEmpty()) {
                if (this.vetoableChangeChildren != null) {
                    ChangeEventDispatcher child = (ChangeEventDispatcher) this.vetoableChangeChildren.get(propertyName);
                    if (!(child == null || child.vetoableChangeListeners == null)) {
                        if (child.vetoableChangeListeners.isEmpty()) {
                            z = false;
                        }
                    }
                }
                z = false;
            }
        }
        return z;
    }
}
