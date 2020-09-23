package net.java.sip.communicator.impl.provdisc.mdns;

import net.java.sip.communicator.service.provdisc.ProvisioningDiscoveryService;
import net.java.sip.communicator.util.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class ProvisioningDiscoveryMDNSActivator implements BundleActivator {
    private static ProvisioningDiscoveryServiceMDNSImpl provisioningService = new ProvisioningDiscoveryServiceMDNSImpl();
    private final Logger logger = Logger.getLogger(ProvisioningDiscoveryMDNSActivator.class);

    public void start(BundleContext bundleContext) throws Exception {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("mDNS provisioning discovery Service [STARTED]");
        }
        bundleContext.registerService(ProvisioningDiscoveryService.class.getName(), provisioningService, null);
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("mDNS provisioning discovery Service [REGISTERED]");
        }
    }

    public void stop(BundleContext bundleContext) throws Exception {
        if (this.logger.isInfoEnabled()) {
            this.logger.info("mDNS provisioning discovery Service ...[STOPPED]");
        }
    }
}
