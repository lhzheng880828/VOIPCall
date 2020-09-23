package org.jitsi.util.event;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import org.jitsi.util.Logger;

public class PropertyChangeNotifier {
    private static final Logger logger = Logger.getLogger(PropertyChangeNotifier.class);
    private final List<PropertyChangeListener> listeners = new ArrayList();

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (listener != null) {
            synchronized (this.listeners) {
                if (!this.listeners.contains(listener)) {
                    this.listeners.add(listener);
                }
            }
        } else if (logger.isDebugEnabled()) {
            logger.debug("The specified argument listener is null and that does not make sense.");
        }
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        if (listener != null) {
            synchronized (this.listeners) {
                this.listeners.remove(listener);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void firePropertyChange(String property, Object oldValue, Object newValue) {
        PropertyChangeListener[] ls;
        synchronized (this.listeners) {
            ls = (PropertyChangeListener[]) this.listeners.toArray(new PropertyChangeListener[this.listeners.size()]);
        }
        if (ls.length != 0) {
            PropertyChangeEvent ev = new PropertyChangeEvent(getPropertyChangeSource(property, oldValue, newValue), property, oldValue, newValue);
            for (PropertyChangeListener l : ls) {
                l.propertyChange(ev);
            }
        }
    }

    /* access modifiers changed from: protected */
    public Object getPropertyChangeSource(String property, Object oldValue, Object newValue) {
        return this;
    }
}
