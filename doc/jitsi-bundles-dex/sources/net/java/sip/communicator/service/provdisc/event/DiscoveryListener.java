package net.java.sip.communicator.service.provdisc.event;

import java.util.EventListener;

public interface DiscoveryListener extends EventListener {
    void notifyProvisioningURL(DiscoveryEvent discoveryEvent);
}
