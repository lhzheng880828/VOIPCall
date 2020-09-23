package org.jitsi.service.neomedia;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public abstract class AbstractMediaStream implements MediaStream {
    private String name;
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /* access modifiers changed from: protected */
    public void firePropertyChange(String property, Object oldValue, Object newValue) {
        this.propertyChangeSupport.firePropertyChange(property, oldValue, newValue);
    }

    public String getName() {
        return this.name;
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public void setName(String name) {
        this.name = name;
    }
}
