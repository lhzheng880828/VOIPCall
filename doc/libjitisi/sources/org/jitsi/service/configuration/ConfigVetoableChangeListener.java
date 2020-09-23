package org.jitsi.service.configuration;

import java.beans.PropertyChangeEvent;
import java.util.EventListener;

public interface ConfigVetoableChangeListener extends EventListener {
    void vetoableChange(PropertyChangeEvent propertyChangeEvent) throws ConfigPropertyVetoException;
}
