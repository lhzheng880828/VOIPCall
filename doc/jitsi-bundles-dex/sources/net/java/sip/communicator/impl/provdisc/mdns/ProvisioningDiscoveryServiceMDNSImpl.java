package net.java.sip.communicator.impl.provdisc.mdns;

import net.java.sip.communicator.service.provdisc.AbstractProvisioningDiscoveryService;
import net.java.sip.communicator.service.provdisc.event.DiscoveryEvent;
import net.java.sip.communicator.service.provdisc.event.DiscoveryListener;
import net.java.sip.communicator.util.Logger;

public class ProvisioningDiscoveryServiceMDNSImpl extends AbstractProvisioningDiscoveryService implements DiscoveryListener {
    private static final String METHOD_NAME = "Bonjour";
    private MDNSProvisioningDiscover discover = null;
    private final Logger logger = Logger.getLogger(ProvisioningDiscoveryServiceMDNSImpl.class);

    public ProvisioningDiscoveryServiceMDNSImpl() {
        try {
            this.discover = new MDNSProvisioningDiscover();
            this.discover.addDiscoveryListener(this);
        } catch (Exception e) {
            this.logger.warn("Cannot create JmDNS instance", e);
        }
    }

    public String getMethodName() {
        return METHOD_NAME;
    }

    public String discoverURL() {
        if (this.discover != null) {
            return this.discover.discoverProvisioningURL();
        }
        return null;
    }

    public void startDiscovery() {
        if (this.discover != null) {
            new Thread(this.discover).start();
        }
    }

    public void notifyProvisioningURL(DiscoveryEvent event) {
        fireDiscoveryEvent(event);
    }
}
