package net.java.sip.communicator.impl.dns;

import java.net.InetSocketAddress;
import javax.sdp.SdpConstants;
import net.java.sip.communicator.service.dns.CustomResolver;
import net.java.sip.communicator.service.netaddr.NetworkAddressManagerService;
import net.java.sip.communicator.service.netaddr.event.ChangeEvent;
import net.java.sip.communicator.service.netaddr.event.NetworkConfigurationChangeListener;
import net.java.sip.communicator.service.notification.NotificationService;
import net.java.sip.communicator.service.resources.ResourceManagementServiceUtils;
import net.java.sip.communicator.util.Logger;
import net.java.sip.communicator.util.ServiceUtils;
import net.java.sip.communicator.util.UtilActivator;
import org.jitsi.service.configuration.ConfigurationService;
import org.jitsi.service.resources.ResourceManagementService;
import org.jitsi.util.StringUtils;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Options;
import org.xbill.DNS.ResolverConfig;
import org.xbill.DNS.SimpleResolver;

public class DnsUtilActivator implements BundleActivator, ServiceListener {
    public static final String DEFAULT_BACKUP_RESOLVER = "backup-resolver.jitsi.net";
    public static final boolean PDEFAULT_BACKUP_RESOLVER_ENABLED = true;
    public static final String PNAME_BACKUP_RESOLVER = "net.java.sip.communicator.util.dns.BACKUP_RESOLVER";
    public static final String PNAME_BACKUP_RESOLVER_ENABLED = "net.java.sip.communicator.util.dns.BACKUP_RESOLVER_ENABLED";
    public static final String PNAME_BACKUP_RESOLVER_FALLBACK_IP = "net.java.sip.communicator.util.dns.BACKUP_RESOLVER_FALLBACK_IP";
    public static final String PNAME_BACKUP_RESOLVER_PORT = "net.java.sip.communicator.util.dns.BACKUP_RESOLVER_PORT";
    public static final String PNAME_DNSSEC_NAMESERVERS = "net.java.sip.communicator.util.dns.DNSSEC_NAMESERVERS";
    private static BundleContext bundleContext;
    private static ConfigurationService configurationService;
    private static final Logger logger = Logger.getLogger(DnsUtilActivator.class);
    private static NotificationService notificationService;
    private static ResourceManagementService resourceService;

    private static class NetworkListener implements NetworkConfigurationChangeListener {
        private NetworkListener() {
        }

        public void configurationChanged(ChangeEvent event) {
            if ((event.getType() == 1 || event.getType() == 0 || event.getType() == 4) && !event.isInitial()) {
                DnsUtilActivator.reloadDnsResolverConfig();
            }
        }
    }

    public void start(BundleContext context) throws Exception {
        logger.info("DNS service ... [STARTING]");
        bundleContext = context;
        context.addServiceListener(this);
        if (Logger.getLogger("org.xbill").isTraceEnabled()) {
            Options.set("verbose", "1");
        }
        if (!loadDNSProxyForward()) {
            if (UtilActivator.getConfigurationService().getBoolean(PNAME_BACKUP_RESOLVER_ENABLED, true) && !getConfigurationService().getBoolean("net.java.sip.communicator.util.dns.DNSSEC_ENABLED", false)) {
                bundleContext.registerService(CustomResolver.class.getName(), new ParallelResolverImpl(), null);
                logger.info("ParallelResolver ... [REGISTERED]");
            }
            if (getConfigurationService().getBoolean("net.java.sip.communicator.util.dns.DNSSEC_ENABLED", false)) {
                bundleContext.registerService(CustomResolver.class.getName(), new ConfigurableDnssecResolver(), null);
                logger.info("DnssecResolver ... [REGISTERED]");
            }
            logger.info("DNS service ... [STARTED]");
        }
    }

    private static boolean loadDNSProxyForward() {
        if (!getConfigurationService().getBoolean("net.java.sip.communicator.service.connectionProxyForwardDNS", false)) {
            return false;
        }
        try {
            String serverAddress = (String) getConfigurationService().getProperty("net.java.sip.communicator.service.connectionProxyForwardDNSAddress");
            if (StringUtils.isNullOrEmpty(serverAddress, true)) {
                return false;
            }
            int port = 53;
            port = getConfigurationService().getInt("net.java.sip.communicator.service.connectionProxyForwardDNSPort", 53);
            SimpleResolver sResolver = new SimpleResolver(SdpConstants.RESERVED);
            sResolver.setAddress(new InetSocketAddress(serverAddress, port));
            Lookup.setDefaultResolver(sResolver);
            return true;
        } catch (NumberFormatException ne) {
            logger.error("Wrong port value", ne);
        } catch (Throwable t) {
            logger.error("Creating simple forwarding resolver", t);
            return false;
        }
    }

    public static void reloadDnsResolverConfig() {
        ResolverConfig.refresh();
        if (logger.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Reloaded resolver config, default DNS servers are: ");
            for (String s : ResolverConfig.getCurrentConfig().servers()) {
                sb.append(s);
                sb.append(", ");
            }
            logger.info(sb.toString());
        }
        if (Lookup.getDefaultResolver() instanceof CustomResolver) {
            if (logger.isInfoEnabled()) {
                logger.info("Resetting custom resolver " + Lookup.getDefaultResolver().getClass().getSimpleName());
            }
            ((CustomResolver) Lookup.getDefaultResolver()).reset();
        } else if (!loadDNSProxyForward()) {
            Lookup.refreshDefault();
        }
    }

    public void stop(BundleContext context) throws Exception {
    }

    public static ConfigurationService getConfigurationService() {
        if (configurationService == null) {
            configurationService = (ConfigurationService) ServiceUtils.getService(bundleContext, ConfigurationService.class);
        }
        return configurationService;
    }

    public static NotificationService getNotificationService() {
        if (notificationService == null) {
            notificationService = (NotificationService) ServiceUtils.getService(bundleContext, NotificationService.class);
        }
        return notificationService;
    }

    public static ResourceManagementService getResources() {
        if (resourceService == null) {
            resourceService = ResourceManagementServiceUtils.getService(bundleContext);
        }
        return resourceService;
    }

    public void serviceChanged(ServiceEvent event) {
        if (event.getType() == 1) {
            Object service = bundleContext.getService(event.getServiceReference());
            if (service instanceof NetworkAddressManagerService) {
                ((NetworkAddressManagerService) service).addNetworkConfigurationChangeListener(new NetworkListener());
            }
        }
    }
}
