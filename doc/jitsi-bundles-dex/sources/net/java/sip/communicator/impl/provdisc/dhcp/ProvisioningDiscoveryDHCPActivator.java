package net.java.sip.communicator.impl.provdisc.dhcp;

import net.java.sip.communicator.service.netaddr.NetworkAddressManagerService;
import net.java.sip.communicator.service.provdisc.ProvisioningDiscoveryService;
import net.java.sip.communicator.util.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class ProvisioningDiscoveryDHCPActivator implements BundleActivator {
    private static BundleContext bundleContext = null;
    private static NetworkAddressManagerService networkAddressManagerService = null;
    private static ProvisioningDiscoveryServiceDHCPImpl provisioningService = new ProvisioningDiscoveryServiceDHCPImpl();
    private final Logger logger = Logger.getLogger(ProvisioningDiscoveryDHCPActivator.class);

    public void start(BundleContext bundleContext) throws Exception {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("DHCP provisioning discovery Service [STARTED]");
        }
        bundleContext.registerService(ProvisioningDiscoveryService.class.getName(), provisioningService, null);
        bundleContext = bundleContext;
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("DHCP provisioning discovery Service [REGISTERED]");
        }
    }

    public void stop(BundleContext bundleContext) throws Exception {
        bundleContext = null;
        if (this.logger.isInfoEnabled()) {
            this.logger.info("DHCP provisioning discovery Service ...[STOPPED]");
        }
    }

    public static NetworkAddressManagerService getNetworkAddressManagerService() {
        if (networkAddressManagerService == null) {
            networkAddressManagerService = (NetworkAddressManagerService) bundleContext.getService(bundleContext.getServiceReference(NetworkAddressManagerService.class.getName()));
        }
        return networkAddressManagerService;
    }
}
