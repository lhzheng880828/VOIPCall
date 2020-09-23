package net.java.sip.communicator.plugin.provisioning;

import java.util.Dictionary;
import java.util.Hashtable;
import net.java.sip.communicator.service.credentialsstorage.CredentialsStorageService;
import net.java.sip.communicator.service.gui.ConfigurationForm;
import net.java.sip.communicator.service.gui.LazyConfigurationForm;
import net.java.sip.communicator.service.gui.UIService;
import net.java.sip.communicator.service.netaddr.NetworkAddressManagerService;
import net.java.sip.communicator.service.provdisc.ProvisioningDiscoveryService;
import net.java.sip.communicator.service.provisioning.ProvisioningService;
import net.java.sip.communicator.util.Logger;
import org.jitsi.service.configuration.ConfigurationService;
import org.jitsi.service.resources.ResourceManagementService;
import org.jitsi.util.StringUtils;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class ProvisioningActivator implements BundleActivator {
    private static final String DISABLED_PROP = "net.java.sip.communicator.plugin.provisionconfig.DISABLED";
    static BundleContext bundleContext = null;
    private static ConfigurationService configurationService = null;
    private static CredentialsStorageService credentialsService = null;
    private static final Logger logger = Logger.getLogger(ProvisioningActivator.class);
    private static NetworkAddressManagerService netaddrService = null;
    private static ProvisioningServiceImpl provisioningService = null;
    private static ResourceManagementService resourceService;
    private static UIService uiService;

    public void start(BundleContext bundleContext) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("Provisioning discovery [STARTED]");
        }
        bundleContext = bundleContext;
        String url = null;
        provisioningService = new ProvisioningServiceImpl();
        if (!getConfigurationService().getBoolean(DISABLED_PROP, false)) {
            Dictionary<String, String> properties = new Hashtable();
            properties.put("FORM_TYPE", "ADVANCED_TYPE");
            bundleContext.registerService(ConfigurationForm.class.getName(), new LazyConfigurationForm("net.java.sip.communicator.plugin.provisioning.ProvisioningForm", getClass().getClassLoader(), "plugin.provisioning.PLUGIN_ICON", "plugin.provisioning.PROVISIONING", 2000, true), properties);
        }
        String method = provisioningService.getProvisioningMethod();
        if (!StringUtils.isNullOrEmpty(method, true) && !method.equals("NONE")) {
            ServiceReference[] serviceReferences = bundleContext.getServiceReferences(ProvisioningDiscoveryService.class.getName(), null);
            if (serviceReferences != null) {
                for (ServiceReference ref : serviceReferences) {
                    ProvisioningDiscoveryService provdisc = (ProvisioningDiscoveryService) bundleContext.getService(ref);
                    if (provdisc.getMethodName().equals(method)) {
                        url = provdisc.discoverURL();
                        break;
                    }
                }
            }
            provisioningService.start(url);
            bundleContext.registerService(ProvisioningService.class.getName(), provisioningService, null);
            if (logger.isDebugEnabled()) {
                logger.debug("Provisioning discovery [REGISTERED]");
            }
        }
    }

    public void stop(BundleContext bundleContext) throws Exception {
        bundleContext = null;
        if (logger.isDebugEnabled()) {
            logger.debug("Provisioning discovery [STOPPED]");
        }
    }

    public static UIService getUIService() {
        if (uiService == null) {
            uiService = (UIService) bundleContext.getService(bundleContext.getServiceReference(UIService.class.getName()));
        }
        return uiService;
    }

    public static ResourceManagementService getResourceService() {
        if (resourceService == null) {
            resourceService = (ResourceManagementService) bundleContext.getService(bundleContext.getServiceReference(ResourceManagementService.class.getName()));
        }
        return resourceService;
    }

    public static ConfigurationService getConfigurationService() {
        if (configurationService == null) {
            configurationService = (ConfigurationService) bundleContext.getService(bundleContext.getServiceReference(ConfigurationService.class.getName()));
        }
        return configurationService;
    }

    public static CredentialsStorageService getCredentialsStorageService() {
        if (credentialsService == null) {
            credentialsService = (CredentialsStorageService) bundleContext.getService(bundleContext.getServiceReference(CredentialsStorageService.class.getName()));
        }
        return credentialsService;
    }

    public static NetworkAddressManagerService getNetworkAddressManagerService() {
        if (netaddrService == null) {
            netaddrService = (NetworkAddressManagerService) bundleContext.getService(bundleContext.getServiceReference(NetworkAddressManagerService.class.getName()));
        }
        return netaddrService;
    }

    public static ProvisioningServiceImpl getProvisioningService() {
        return provisioningService;
    }
}
