package net.java.sip.communicator.impl.protocol.jabber;

import java.util.Hashtable;
import net.java.sip.communicator.service.credentialsstorage.CredentialsStorageService;
import net.java.sip.communicator.service.googlecontacts.GoogleContactsService;
import net.java.sip.communicator.service.gui.UIService;
import net.java.sip.communicator.service.hid.HIDService;
import net.java.sip.communicator.service.netaddr.NetworkAddressManagerService;
import net.java.sip.communicator.service.protocol.ProtocolProviderFactory;
import net.java.sip.communicator.service.resources.ResourceManagementServiceUtils;
import net.java.sip.communicator.util.ServiceUtils;
import org.jitsi.service.configuration.ConfigurationService;
import org.jitsi.service.neomedia.MediaService;
import org.jitsi.service.packetlogging.PacketLoggingService;
import org.jitsi.service.resources.ResourceManagementService;
import org.jitsi.service.version.VersionService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

public class JabberActivator implements BundleActivator {
    static BundleContext bundleContext = null;
    private static ConfigurationService configurationService = null;
    private static CredentialsStorageService credentialsService = null;
    private static GoogleContactsService googleService = null;
    private static HIDService hidService = null;
    private static ProtocolProviderFactoryJabberImpl jabberProviderFactory = null;
    private static MediaService mediaService = null;
    private static NetworkAddressManagerService networkAddressManagerService = null;
    private static PacketLoggingService packetLoggingService = null;
    private static ResourceManagementService resourcesService = null;
    private static UIService uiService = null;
    private static VersionService versionService = null;
    private ServiceRegistration jabberPpFactoryServReg = null;
    private UriHandlerJabberImpl uriHandlerImpl = null;

    public void start(BundleContext context) throws Exception {
        bundleContext = context;
        Hashtable<String, String> hashtable = new Hashtable();
        hashtable.put("PROTOCOL_NAME", "Jabber");
        jabberProviderFactory = new ProtocolProviderFactoryJabberImpl();
        this.uriHandlerImpl = new UriHandlerJabberImpl(jabberProviderFactory);
        this.jabberPpFactoryServReg = context.registerService(ProtocolProviderFactory.class.getName(), jabberProviderFactory, hashtable);
    }

    public static ConfigurationService getConfigurationService() {
        if (configurationService == null) {
            configurationService = (ConfigurationService) ServiceUtils.getService(bundleContext, ConfigurationService.class);
        }
        return configurationService;
    }

    public static BundleContext getBundleContext() {
        return bundleContext;
    }

    static ProtocolProviderFactoryJabberImpl getProtocolProviderFactory() {
        return jabberProviderFactory;
    }

    public void stop(BundleContext context) throws Exception {
        jabberProviderFactory.stop();
        this.jabberPpFactoryServReg.unregister();
        if (this.uriHandlerImpl != null) {
            this.uriHandlerImpl.dispose();
            this.uriHandlerImpl = null;
        }
        configurationService = null;
        mediaService = null;
        networkAddressManagerService = null;
        credentialsService = null;
    }

    public static UIService getUIService() {
        if (uiService == null) {
            uiService = (UIService) bundleContext.getService(bundleContext.getServiceReference(UIService.class.getName()));
        }
        return uiService;
    }

    public static ResourceManagementService getResources() {
        if (resourcesService == null) {
            resourcesService = ResourceManagementServiceUtils.getService(bundleContext);
        }
        return resourcesService;
    }

    public static MediaService getMediaService() {
        if (mediaService == null) {
            mediaService = (MediaService) bundleContext.getService(bundleContext.getServiceReference(MediaService.class.getName()));
        }
        return mediaService;
    }

    public static NetworkAddressManagerService getNetworkAddressManagerService() {
        if (networkAddressManagerService == null) {
            networkAddressManagerService = (NetworkAddressManagerService) bundleContext.getService(bundleContext.getServiceReference(NetworkAddressManagerService.class.getName()));
        }
        return networkAddressManagerService;
    }

    public static CredentialsStorageService getCredentialsStorageService() {
        if (credentialsService == null) {
            credentialsService = (CredentialsStorageService) bundleContext.getService(bundleContext.getServiceReference(CredentialsStorageService.class.getName()));
        }
        return credentialsService;
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

    public static PacketLoggingService getPacketLogging() {
        if (packetLoggingService == null) {
            packetLoggingService = (PacketLoggingService) ServiceUtils.getService(bundleContext, PacketLoggingService.class);
        }
        return packetLoggingService;
    }

    public static GoogleContactsService getGoogleService() {
        if (googleService == null) {
            googleService = (GoogleContactsService) ServiceUtils.getService(bundleContext, GoogleContactsService.class);
        }
        return googleService;
    }

    public static VersionService getVersionService() {
        if (versionService == null) {
            versionService = (VersionService) bundleContext.getService(bundleContext.getServiceReference(VersionService.class.getName()));
        }
        return versionService;
    }
}
