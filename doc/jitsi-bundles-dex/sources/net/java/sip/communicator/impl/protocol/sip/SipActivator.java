package net.java.sip.communicator.impl.protocol.sip;

import java.util.Dictionary;
import java.util.Hashtable;
import net.java.sip.communicator.service.certificate.CertificateService;
import net.java.sip.communicator.service.gui.UIService;
import net.java.sip.communicator.service.hid.HIDService;
import net.java.sip.communicator.service.netaddr.NetworkAddressManagerService;
import net.java.sip.communicator.service.protocol.ProtocolProviderFactory;
import net.java.sip.communicator.util.Logger;
import net.java.sip.communicator.util.ServiceUtils;
import org.jitsi.service.configuration.ConfigurationService;
import org.jitsi.service.fileaccess.FileAccessService;
import org.jitsi.service.neomedia.MediaService;
import org.jitsi.service.packetlogging.PacketLoggingService;
import org.jitsi.service.resources.ResourceManagementService;
import org.jitsi.service.version.VersionService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

public class SipActivator implements BundleActivator {
    static BundleContext bundleContext = null;
    private static CertificateService certService = null;
    private static ConfigurationService configurationService = null;
    private static FileAccessService fileService = null;
    private static HIDService hidService = null;
    private static MediaService mediaService = null;
    private static NetworkAddressManagerService networkAddressManagerService = null;
    private static PacketLoggingService packetLoggingService = null;
    private static ResourceManagementService resources = null;
    private static ProtocolProviderFactorySipImpl sipProviderFactory = null;
    private static UIService uiService = null;
    private static VersionService versionService = null;
    private Logger logger = Logger.getLogger(SipActivator.class.getName());
    private ServiceRegistration sipPpFactoryServReg = null;
    private UriHandlerSipImpl uriHandlerSipImpl = null;

    public void start(BundleContext context) throws Exception {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Started.");
        }
        bundleContext = context;
        sipProviderFactory = createProtocolProviderFactory();
        this.uriHandlerSipImpl = new UriHandlerSipImpl(sipProviderFactory);
        Dictionary<String, String> properties = new Hashtable();
        properties.put("PROTOCOL_NAME", "SIP");
        this.sipPpFactoryServReg = context.registerService(ProtocolProviderFactory.class.getName(), sipProviderFactory, properties);
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("SIP Protocol Provider Factory ... [REGISTERED]");
        }
    }

    /* access modifiers changed from: protected */
    public ProtocolProviderFactorySipImpl createProtocolProviderFactory() {
        return new ProtocolProviderFactorySipImpl();
    }

    public static CertificateService getCertificateVerificationService() {
        if (certService == null) {
            ServiceReference guiVerifyReference = bundleContext.getServiceReference(CertificateService.class.getName());
            if (guiVerifyReference != null) {
                certService = (CertificateService) bundleContext.getService(guiVerifyReference);
            }
        }
        return certService;
    }

    public static ConfigurationService getConfigurationService() {
        if (configurationService == null) {
            configurationService = (ConfigurationService) bundleContext.getService(bundleContext.getServiceReference(ConfigurationService.class.getName()));
        }
        return configurationService;
    }

    public static NetworkAddressManagerService getNetworkAddressManagerService() {
        if (networkAddressManagerService == null) {
            networkAddressManagerService = (NetworkAddressManagerService) bundleContext.getService(bundleContext.getServiceReference(NetworkAddressManagerService.class.getName()));
        }
        return networkAddressManagerService;
    }

    public static HIDService getHIDService() {
        if (hidService == null) {
            ServiceReference hidReference = bundleContext.getServiceReference(HIDService.class.getName());
            if (hidReference == null) {
                return null;
            }
            hidService = (HIDService) bundleContext.getService(hidReference);
        }
        return hidService;
    }

    public static BundleContext getBundleContext() {
        return bundleContext;
    }

    public static ProtocolProviderFactorySipImpl getProtocolProviderFactory() {
        return sipProviderFactory;
    }

    public static MediaService getMediaService() {
        if (mediaService == null) {
            mediaService = (MediaService) bundleContext.getService(bundleContext.getServiceReference(MediaService.class.getName()));
        }
        return mediaService;
    }

    public static VersionService getVersionService() {
        if (versionService == null) {
            versionService = (VersionService) bundleContext.getService(bundleContext.getServiceReference(VersionService.class.getName()));
        }
        return versionService;
    }

    public static UIService getUIService() {
        if (uiService == null) {
            uiService = (UIService) bundleContext.getService(bundleContext.getServiceReference(UIService.class.getName()));
        }
        return uiService;
    }

    public static ResourceManagementService getResources() {
        if (resources == null) {
            resources = (ResourceManagementService) ServiceUtils.getService(bundleContext, ResourceManagementService.class);
        }
        return resources;
    }

    public static PacketLoggingService getPacketLogging() {
        if (packetLoggingService == null) {
            packetLoggingService = (PacketLoggingService) ServiceUtils.getService(bundleContext, PacketLoggingService.class);
        }
        return packetLoggingService;
    }

    public static FileAccessService getFileAccessService() {
        if (fileService == null) {
            fileService = (FileAccessService) ServiceUtils.getService(bundleContext, FileAccessService.class);
        }
        return fileService;
    }

    public void stop(BundleContext context) throws Exception {
        sipProviderFactory.stop();
        this.sipPpFactoryServReg.unregister();
        if (this.uriHandlerSipImpl != null) {
            this.uriHandlerSipImpl.dispose();
            this.uriHandlerSipImpl = null;
        }
        configurationService = null;
        networkAddressManagerService = null;
        mediaService = null;
        versionService = null;
        uiService = null;
        hidService = null;
        packetLoggingService = null;
        certService = null;
        fileService = null;
    }
}
