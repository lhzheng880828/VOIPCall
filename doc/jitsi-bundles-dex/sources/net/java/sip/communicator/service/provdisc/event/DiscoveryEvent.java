package net.java.sip.communicator.service.provdisc.event;

import java.util.EventObject;

public class DiscoveryEvent extends EventObject {
    private static final long serialVersionUID = 0;
    private String url = null;

    public DiscoveryEvent(Object source, String url) {
        super(source);
        this.url = url;
    }

    public String getProvisioningURL() {
        return this.url;
    }
}
