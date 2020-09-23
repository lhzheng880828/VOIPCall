package net.java.sip.communicator.impl.provdisc.mdns;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import net.java.sip.communicator.service.provdisc.event.DiscoveryEvent;
import net.java.sip.communicator.service.provdisc.event.DiscoveryListener;
import net.java.sip.communicator.util.Logger;
import org.jitsi.gov.nist.core.Separators;

public class MDNSProvisioningDiscover implements Runnable {
    private static final int MDNS_TIMEOUT = 2000;
    private JmDNS jmdns = null;
    private List<DiscoveryListener> listeners = new ArrayList();
    private final Logger logger = Logger.getLogger(MDNSProvisioningDiscover.class);

    public void run() {
        String url = discoverProvisioningURL();
        if (url != null) {
            DiscoveryEvent evt = new DiscoveryEvent(this, url);
            for (DiscoveryListener listener : this.listeners) {
                listener.notifyProvisioningURL(evt);
            }
        }
    }

    public String discoverProvisioningURL() {
        StringBuffer url = new StringBuffer();
        try {
            this.jmdns = JmDNS.create();
            ServiceInfo info = this.jmdns.getServiceInfo("_https._tcp.local", "Provisioning URL", 2000);
            if (info == null) {
                info = this.jmdns.getServiceInfo("_http._tcp.local", "Provisioning URL", 2000);
            }
            if (info != null && info.getName().equals("Provisioning URL")) {
                url.append(info.getURL(info.getApplication()));
                Enumeration<String> en = info.getPropertyNames();
                if (en.hasMoreElements()) {
                    url.append(Separators.QUESTION);
                }
                while (en.hasMoreElements()) {
                    String tmp = (String) en.nextElement();
                    if (!tmp.equals("path")) {
                        url.append(tmp);
                        url.append(Separators.EQUALS);
                        url.append(info.getPropertyString(tmp));
                        if (en.hasMoreElements()) {
                            url.append(Separators.AND);
                        }
                    }
                }
            }
            try {
                this.jmdns.close();
                this.jmdns = null;
            } catch (Exception e) {
                this.logger.warn("Failed to close JmDNS", e);
            }
            if (url.toString().length() > 0) {
                return url.toString();
            }
            return null;
        } catch (IOException e2) {
            this.logger.info("Failed to create JmDNS", e2);
            return null;
        }
    }

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
}
