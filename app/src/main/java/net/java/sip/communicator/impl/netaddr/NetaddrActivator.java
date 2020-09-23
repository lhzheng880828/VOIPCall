package net.java.sip.communicator.impl.netaddr;

import net.java.sip.communicator.service.netaddr.NetworkAddressManagerService;
import net.java.sip.communicator.util.Logger;
import net.java.sip.communicator.util.ServiceUtils;
import org.jitsi.service.configuration.ConfigurationService;
import org.jitsi.service.packetlogging.PacketLoggingService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class NetaddrActivator implements BundleActivator {
    private static BundleContext bundleContext = null;
    private static ConfigurationService configurationService = null;
    private static Logger logger = Logger.getLogger(NetworkAddressManagerServiceImpl.class);
    private static PacketLoggingService packetLoggingService = null;
    private NetworkAddressManagerServiceImpl networkAMS = null;

    public void start(BundleContext bundleContext1) throws Exception {
        try {
            logger.logEntry();
            bundleContext = bundleContext1;
            this.networkAMS = new NetworkAddressManagerServiceImpl();
            this.networkAMS.start();
            if (logger.isInfoEnabled()) {
                logger.info("Network Address Manager         ...[  STARTED ]");
            }
            bundleContext.registerService(NetworkAddressManagerService.class.getName(), this.networkAMS, null);
            if (logger.isInfoEnabled()) {
                logger.info("Network Address Manager Service ...[REGISTERED]");
            }
            logger.logExit();
        } catch (Throwable th) {
            th.printStackTrace();
            logger.logExit();
        }
    }

    public static ConfigurationService getConfigurationService() {
        if (configurationService == null) {
            configurationService = (ConfigurationService) ServiceUtils.getService(bundleContext, ConfigurationService.class);
        }
        return configurationService;
    }

    public static PacketLoggingService getPacketLogging() {
        if (packetLoggingService == null) {
            packetLoggingService = (PacketLoggingService) ServiceUtils.getService(bundleContext, PacketLoggingService.class);
        }
        return packetLoggingService;
    }

    public void stop(BundleContext bundleContext) {
        if (this.networkAMS != null) {
            this.networkAMS.stop();
        }
        if (logger.isInfoEnabled()) {
            logger.info("Network Address Manager Service ...[STOPPED]");
        }
        configurationService = null;
        packetLoggingService = null;
    }

    static BundleContext getBundleContext() {
        return bundleContext;
    }
}
