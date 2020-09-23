package net.java.sip.communicator.impl.packetlogging;

import net.java.sip.communicator.util.Logger;
import net.java.sip.communicator.util.ServiceUtils;
import org.jitsi.service.configuration.ConfigurationService;
import org.jitsi.service.fileaccess.FileAccessService;
import org.jitsi.service.packetlogging.PacketLoggingService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class PacketLoggingActivator implements BundleActivator {
    static final String LOGGING_DIR_NAME = "log";
    private static BundleContext bundleContext = null;
    private static ConfigurationService configurationService = null;
    private static FileAccessService fileAccessService;
    private static Logger logger = Logger.getLogger(PacketLoggingActivator.class);
    private static PacketLoggingServiceImpl packetLoggingService = null;

    public void start(BundleContext bundleContext) throws Exception {
        fileAccessService = (FileAccessService) ServiceUtils.getService(bundleContext, FileAccessService.class);
        if (fileAccessService != null) {
            bundleContext = bundleContext;
            packetLoggingService = new PacketLoggingServiceImpl();
            packetLoggingService.start();
            bundleContext.registerService(PacketLoggingService.class.getName(), packetLoggingService, null);
            if (logger.isInfoEnabled()) {
                logger.info("Packet Logging Service ...[REGISTERED]");
            }
        }
    }

    public void stop(BundleContext bundleContext) throws Exception {
        if (packetLoggingService != null) {
            packetLoggingService.stop();
        }
        configurationService = null;
        fileAccessService = null;
        packetLoggingService = null;
        if (logger.isInfoEnabled()) {
            logger.info("Packet Logging Service ...[STOPPED]");
        }
    }

    public static ConfigurationService getConfigurationService() {
        if (configurationService == null) {
            configurationService = (ConfigurationService) bundleContext.getService(bundleContext.getServiceReference(ConfigurationService.class.getName()));
        }
        return configurationService;
    }

    public static FileAccessService getFileAccessService() {
        return fileAccessService;
    }
}
