package org.jitsi.service.configuration;

import java.beans.PropertyChangeEvent;

public class ConfigPropertyVetoException extends RuntimeException {
    private static final long serialVersionUID = 0;
    private final PropertyChangeEvent evt;

    public ConfigPropertyVetoException(String message, PropertyChangeEvent evt) {
        super(message);
        this.evt = evt;
    }

    public PropertyChangeEvent getPropertyChangeEvent() {
        return this.evt;
    }
}
