package net.java.sip.communicator.service.provdisc;

import java.util.ArrayList;
import java.util.List;
import net.java.sip.communicator.service.provdisc.event.DiscoveryEvent;
import net.java.sip.communicator.service.provdisc.event.DiscoveryListener;

public abstract class AbstractProvisioningDiscoveryService implements ProvisioningDiscoveryService {
    private List<DiscoveryListener> listeners = new ArrayList();

    public abstract String discoverURL();

    public abstract String getMethodName();

    public abstract void startDiscovery();

    public void addDiscoveryListener(DiscoveryListener listener) {
        if (!this.listeners.contains(listener)) {
            this.listeners.add(listener);
        }
    }

    public void removeDiscoveryListener(DiscoveryListener listener) {
        if (this.listeners.contains(listener)) {
            this.listeners.remove(listener);
        }
    }

    public void fireDiscoveryEvent(DiscoveryEvent event) {
        for (DiscoveryListener listener : this.listeners) {
            listener.notifyProvisioningURL(event);
        }
    }
}
