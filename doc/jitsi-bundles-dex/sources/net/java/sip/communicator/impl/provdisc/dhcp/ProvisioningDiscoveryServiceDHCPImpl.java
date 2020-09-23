package net.java.sip.communicator.impl.provdisc.dhcp;

import net.java.sip.communicator.service.provdisc.AbstractProvisioningDiscoveryService;
import net.java.sip.communicator.service.provdisc.event.DiscoveryEvent;
import net.java.sip.communicator.service.provdisc.event.DiscoveryListener;
import net.java.sip.communicator.util.Logger;

public class ProvisioningDiscoveryServiceDHCPImpl extends AbstractProvisioningDiscoveryService implements DiscoveryListener {
    private static final String METHOD_NAME = "DHCP";
    private DHCPProvisioningDiscover discover = null;
    private final Logger logger = Logger.getLogger(ProvisioningDiscoveryServiceDHCPImpl.class);

    public String getMethodName() {
        return METHOD_NAME;
    }

    public ProvisioningDiscoveryServiceDHCPImpl() {
        try {
            this.discover = new DHCPProvisioningDiscover(6768, (byte) -32);
            this.discover.addDiscoveryListener(this);
        } catch (Exception e) {
            this.logger.warn("Cannot create DHCP client socket", e);
        }
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
