package net.java.sip.communicator.service.provdisc;

import net.java.sip.communicator.service.provdisc.event.DiscoveryListener;

public interface ProvisioningDiscoveryService {
    void addDiscoveryListener(DiscoveryListener discoveryListener);

    String discoverURL();

    String getMethodName();

    void removeDiscoveryListener(DiscoveryListener discoveryListener);

    void startDiscovery();
}
