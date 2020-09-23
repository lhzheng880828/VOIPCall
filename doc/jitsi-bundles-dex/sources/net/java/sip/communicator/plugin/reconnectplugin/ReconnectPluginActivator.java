package net.java.sip.communicator.plugin.reconnectplugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import net.java.sip.communicator.service.gui.UIService;
import net.java.sip.communicator.service.netaddr.NetworkAddressManagerService;
import net.java.sip.communicator.service.netaddr.event.ChangeEvent;
import net.java.sip.communicator.service.netaddr.event.NetworkConfigurationChangeListener;
import net.java.sip.communicator.service.notification.NotificationService;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.ProtocolProviderService;
import net.java.sip.communicator.service.protocol.RegistrationState;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeEvent;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeListener;
import net.java.sip.communicator.util.Logger;
import net.java.sip.communicator.util.ServiceUtils;
import org.jitsi.service.configuration.ConfigurationService;
import org.jitsi.service.resources.ResourceManagementService;
import org.jitsi.util.OSUtils;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

public class ReconnectPluginActivator implements BundleActivator, ServiceListener, NetworkConfigurationChangeListener, RegistrationStateChangeListener {
    public static final String ATLEAST_ONE_CONNECTION_PROP = "net.java.sip.communicator.plugin.reconnectplugin.ATLEAST_ONE_SUCCESSFUL_CONNECTION";
    private static final int MAX_RECONNECT_DELAY = 300;
    private static final long NETWORK_DOWN_THRESHOLD = 30000;
    public static final String NETWORK_NOTIFICATIONS = "NetworkNotifications";
    private static final int RECONNECT_DELAY_MAX = 4;
    private static final int RECONNECT_DELAY_MIN = 2;
    private static BundleContext bundleContext = null;
    private static ConfigurationService configurationService = null;
    /* access modifiers changed from: private|static|final */
    public static final Logger logger = Logger.getLogger(ReconnectPluginActivator.class);
    private static NotificationService notificationService;
    private static ResourceManagementService resourcesService;
    private static UIService uiService;
    private final Map<ProtocolProviderService, List<String>> autoReconnEnabledProviders = new HashMap();
    /* access modifiers changed from: private */
    public Set<String> connectedInterfaces = new HashSet();
    /* access modifiers changed from: private|final */
    public final Map<ProtocolProviderService, ReconnectTask> currentlyReconnecting = new HashMap();
    private Timer delayedNetworkDown;
    /* access modifiers changed from: private */
    public Set<ProtocolProviderService> needsReconnection = new HashSet();
    private NetworkAddressManagerService networkAddressManagerService = null;
    /* access modifiers changed from: private */
    public Timer timer = null;
    private Set<ProtocolProviderService> unregisteringProviders = new HashSet();

    private class ReconnectTask extends TimerTask {
        /* access modifiers changed from: private */
        public long delay;
        private ProtocolProviderService provider;
        private Thread thread = null;

        public ReconnectTask(ProtocolProviderService provider) {
            this.provider = provider;
        }

        public void run() {
            if (this.thread == null || !Thread.currentThread().equals(this.thread)) {
                this.thread = new Thread(this);
                this.thread.start();
                return;
            }
            try {
                if (ReconnectPluginActivator.logger.isInfoEnabled()) {
                    ReconnectPluginActivator.logger.info("Start reconnecting " + this.provider.getAccountID().getDisplayName());
                }
                this.provider.register(ReconnectPluginActivator.getUIService().getDefaultSecurityAuthority(this.provider));
            } catch (OperationFailedException ex) {
                ReconnectPluginActivator.logger.error("cannot re-register provider will keep going", ex);
            }
        }
    }

    public void start(BundleContext bundleContext) throws Exception {
        try {
            logger.logEntry();
            bundleContext = bundleContext;
            bundleContext.addServiceListener(this);
            if (this.timer == null) {
                this.timer = new Timer("Reconnect timer", true);
            }
            this.networkAddressManagerService = (NetworkAddressManagerService) ServiceUtils.getService(bundleContext, NetworkAddressManagerService.class);
            this.networkAddressManagerService.addNetworkConfigurationChangeListener(this);
            try {
                ServiceReference[] protocolProviderRefs = bundleContext.getServiceReferences(ProtocolProviderService.class.getName(), null);
                if (protocolProviderRefs != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Found " + protocolProviderRefs.length + " already installed providers.");
                    }
                    for (ServiceReference service : protocolProviderRefs) {
                        handleProviderAdded((ProtocolProviderService) bundleContext.getService(service));
                    }
                }
            } catch (InvalidSyntaxException ex) {
                logger.error("Error while retrieving service refs", ex);
            }
        } finally {
            logger.logExit();
        }
    }

    public void stop(BundleContext bundleContext) throws Exception {
        if (this.timer != null) {
            this.timer.cancel();
            this.timer = null;
        }
    }

    public static UIService getUIService() {
        if (uiService == null) {
            uiService = (UIService) bundleContext.getService(bundleContext.getServiceReference(UIService.class.getName()));
        }
        return uiService;
    }

    public static ResourceManagementService getResources() {
        if (resourcesService == null) {
            ServiceReference serviceReference = bundleContext.getServiceReference(ResourceManagementService.class.getName());
            if (serviceReference == null) {
                return null;
            }
            resourcesService = (ResourceManagementService) bundleContext.getService(serviceReference);
        }
        return resourcesService;
    }

    public static ConfigurationService getConfigurationService() {
        if (configurationService == null) {
            configurationService = (ConfigurationService) bundleContext.getService(bundleContext.getServiceReference(ConfigurationService.class.getName()));
        }
        return configurationService;
    }

    public static NotificationService getNotificationService() {
        if (notificationService == null) {
            notificationService = (NotificationService) bundleContext.getService(bundleContext.getServiceReference(NotificationService.class.getName()));
            notificationService.registerDefaultNotificationForEvent(NETWORK_NOTIFICATIONS, "PopupMessageAction", null, null);
        }
        return notificationService;
    }

    public void serviceChanged(ServiceEvent serviceEvent) {
        ServiceReference serviceRef = serviceEvent.getServiceReference();
        if (serviceRef.getBundle().getState() != 16) {
            Object sService = bundleContext.getService(serviceRef);
            if (sService instanceof NetworkAddressManagerService) {
                switch (serviceEvent.getType()) {
                    case 1:
                        if (this.networkAddressManagerService == null) {
                            this.networkAddressManagerService = (NetworkAddressManagerService) sService;
                            this.networkAddressManagerService.addNetworkConfigurationChangeListener(this);
                            return;
                        }
                        return;
                    case 4:
                        ((NetworkAddressManagerService) sService).removeNetworkConfigurationChangeListener(this);
                        return;
                    default:
                        return;
                }
            } else if (sService instanceof ProtocolProviderService) {
                switch (serviceEvent.getType()) {
                    case 1:
                        handleProviderAdded((ProtocolProviderService) sService);
                        return;
                    case 4:
                        handleProviderRemoved((ProtocolProviderService) sService);
                        return;
                    default:
                        return;
                }
            }
        }
    }

    private void handleProviderAdded(ProtocolProviderService provider) {
        if (logger.isTraceEnabled()) {
            logger.trace("New protocol provider is comming " + provider.getProtocolName());
        }
        provider.addRegistrationStateChangeListener(this);
    }

    private void handleProviderRemoved(ProtocolProviderService provider) {
        if (logger.isTraceEnabled()) {
            logger.trace("Provider modified forget every instance of it");
        }
        if (hasAtLeastOneSuccessfulConnection(provider)) {
            setAtLeastOneSuccessfulConnection(provider, false);
        }
        provider.removeRegistrationStateChangeListener(this);
        this.autoReconnEnabledProviders.remove(provider);
        this.needsReconnection.remove(provider);
        if (this.currentlyReconnecting.containsKey(provider)) {
            ((ReconnectTask) this.currentlyReconnecting.remove(provider)).cancel();
        }
    }

    public synchronized void configurationChanged(ChangeEvent event) {
        ProtocolProviderService pp;
        if (event.getType() == 1) {
            if (this.connectedInterfaces.isEmpty()) {
                onNetworkUp();
                for (ProtocolProviderService pp2 : this.needsReconnection) {
                    if (this.currentlyReconnecting.containsKey(pp2)) {
                        ((ReconnectTask) this.currentlyReconnecting.remove(pp2)).cancel();
                    }
                    reconnect(pp2);
                }
                this.needsReconnection.clear();
            }
            this.connectedInterfaces.add((String) event.getSource());
        } else if (event.getType() == 0) {
            String ifaceName = (String) event.getSource();
            this.connectedInterfaces.remove(ifaceName);
            if (this.connectedInterfaces.size() > 0) {
                for (Entry<ProtocolProviderService, List<String>> entry : this.autoReconnEnabledProviders.entrySet()) {
                    if (((List) entry.getValue()).contains(ifaceName)) {
                        pp2 = (ProtocolProviderService) entry.getKey();
                        if (this.currentlyReconnecting.containsKey(pp2)) {
                            ((ReconnectTask) this.currentlyReconnecting.remove(pp2)).cancel();
                        }
                        reconnect(pp2);
                    }
                }
            } else {
                this.needsReconnection.addAll(this.autoReconnEnabledProviders.keySet());
                this.needsReconnection.addAll(this.currentlyReconnecting.keySet());
                for (ProtocolProviderService pp22 : this.needsReconnection) {
                    if (this.currentlyReconnecting.containsKey(pp22)) {
                        ((ReconnectTask) this.currentlyReconnecting.remove(pp22)).cancel();
                    }
                    unregister(pp22, false, null, null);
                }
                this.connectedInterfaces.clear();
                onNetworkDown();
            }
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Event received " + event + " src=" + event.getSource());
            traceCurrentPPState();
        }
    }

    private void unregister(ProtocolProviderService pp, boolean reconnect, RegistrationStateChangeListener listener, ReconnectTask task) {
        this.unregisteringProviders.add(pp);
        final ProtocolProviderService protocolProviderService = pp;
        final boolean z = reconnect;
        final RegistrationStateChangeListener registrationStateChangeListener = listener;
        final ReconnectTask reconnectTask = task;
        new Thread(new Runnable() {
            public void run() {
                try {
                    if (!protocolProviderService.getRegistrationState().equals(RegistrationState.UNREGISTERING) && !protocolProviderService.getRegistrationState().equals(RegistrationState.UNREGISTERED) && !protocolProviderService.getRegistrationState().equals(RegistrationState.CONNECTION_FAILED)) {
                        protocolProviderService.unregister();
                    } else if (z) {
                        if (registrationStateChangeListener != null) {
                            protocolProviderService.removeRegistrationStateChangeListener(registrationStateChangeListener);
                        }
                        if (ReconnectPluginActivator.this.timer != null && reconnectTask != null) {
                            if (ReconnectPluginActivator.this.currentlyReconnecting.containsKey(protocolProviderService)) {
                                ((ReconnectTask) ReconnectPluginActivator.this.currentlyReconnecting.remove(protocolProviderService)).cancel();
                            }
                            ReconnectPluginActivator.this.currentlyReconnecting.put(protocolProviderService, reconnectTask);
                            if (ReconnectPluginActivator.logger.isInfoEnabled()) {
                                ReconnectPluginActivator.logger.info("Reconnect " + protocolProviderService.getAccountID().getDisplayName() + " after " + reconnectTask.delay + " ms.");
                            }
                            ReconnectPluginActivator.this.timer.schedule(reconnectTask, reconnectTask.delay);
                        }
                    }
                } catch (Throwable t) {
                    ReconnectPluginActivator.logger.error("Error unregistering pp:" + protocolProviderService, t);
                }
            }
        }).start();
    }

    private void traceCurrentPPState() {
        logger.trace("connectedInterfaces: " + this.connectedInterfaces);
        logger.trace("autoReconnEnabledProviders: " + this.autoReconnEnabledProviders.keySet());
        logger.trace("currentlyReconnecting: " + this.currentlyReconnecting.keySet());
        logger.trace("needsReconnection: " + this.needsReconnection);
        logger.trace("unregisteringProviders: " + this.unregisteringProviders);
        logger.trace("----");
    }

    private void notify(String title, String i18nKey, String[] params, Object tag) {
        Map<String, Object> extras = new HashMap();
        extras.put("PopupMessageNotificationHandler.tag", tag);
        getNotificationService().fireNotification(NETWORK_NOTIFICATIONS, title, getResources().getI18NString(i18nKey, params), null, extras);
    }

    public void registrationStateChanged(RegistrationStateChangeEvent evt) {
        if (!(evt.getSource() instanceof ProtocolProviderService)) {
            return;
        }
        if (evt.getNewState().equals(RegistrationState.REGISTERED) || evt.getNewState().equals(RegistrationState.UNREGISTERED) || evt.getNewState().equals(RegistrationState.CONNECTION_FAILED)) {
            synchronized (this) {
                try {
                    ProtocolProviderService pp = (ProtocolProviderService) evt.getSource();
                    if (evt.getNewState().equals(RegistrationState.CONNECTION_FAILED)) {
                        if (!hasAtLeastOneSuccessfulConnection(pp)) {
                            if (evt.getReasonCode() == 3) {
                                notify(getResources().getI18NString("service.gui.ERROR"), "service.gui.NON_EXISTING_USER_ID", new String[]{pp.getAccountID().getService()}, pp.getAccountID());
                            } else {
                                notify(getResources().getI18NString("service.gui.ERROR"), "plugin.reconnectplugin.CONNECTION_FAILED_MSG", new String[]{pp.getAccountID().getUserID(), pp.getAccountID().getService()}, pp.getAccountID());
                            }
                            return;
                        } else if (this.needsReconnection.contains(pp)) {
                            return;
                        } else {
                            if (this.connectedInterfaces.isEmpty()) {
                                this.needsReconnection.add(pp);
                                if (this.currentlyReconnecting.containsKey(pp)) {
                                    ((ReconnectTask) this.currentlyReconnecting.remove(pp)).cancel();
                                }
                            } else {
                                reconnect(pp);
                            }
                            this.unregisteringProviders.remove(pp);
                            if (logger.isTraceEnabled()) {
                                logger.trace("Got Connection Failed for " + pp);
                                traceCurrentPPState();
                            }
                        }
                    } else if (evt.getNewState().equals(RegistrationState.REGISTERED)) {
                        if (!hasAtLeastOneSuccessfulConnection(pp)) {
                            setAtLeastOneSuccessfulConnection(pp, true);
                        }
                        this.autoReconnEnabledProviders.put(pp, new ArrayList(this.connectedInterfaces));
                        if (this.currentlyReconnecting.containsKey(pp)) {
                            ((ReconnectTask) this.currentlyReconnecting.remove(pp)).cancel();
                        }
                        this.unregisteringProviders.remove(pp);
                        if (logger.isTraceEnabled()) {
                            logger.trace("Got Registered for " + pp);
                            traceCurrentPPState();
                        }
                    } else if (evt.getNewState().equals(RegistrationState.UNREGISTERED)) {
                        this.autoReconnEnabledProviders.remove(pp);
                        if (!this.unregisteringProviders.contains(pp) && this.currentlyReconnecting.containsKey(pp)) {
                            ((ReconnectTask) this.currentlyReconnecting.remove(pp)).cancel();
                        }
                        this.unregisteringProviders.remove(pp);
                        if (logger.isTraceEnabled()) {
                            logger.trace("Got Unregistered for " + pp);
                            traceCurrentPPState();
                        }
                    }
                } catch (Throwable ex) {
                    logger.error("Error dispatching protocol registration change", ex);
                }
            }
        } else {
            return;
        }
    }

    private void reconnect(final ProtocolProviderService pp) {
        long delay;
        if (this.currentlyReconnecting.containsKey(pp)) {
            delay = Math.min(2 * ((ReconnectTask) this.currentlyReconnecting.get(pp)).delay, 300000);
        } else {
            delay = ((long) (2.0d + (Math.random() * 4.0d))) * 1000;
        }
        final ReconnectTask task = new ReconnectTask(pp);
        task.delay = delay;
        RegistrationStateChangeListener listener = new RegistrationStateChangeListener() {
            /* JADX WARNING: Missing block: B:33:?, code skipped:
            return;
     */
            public void registrationStateChanged(net.java.sip.communicator.service.protocol.event.RegistrationStateChangeEvent r5) {
                /*
                r4 = this;
                r0 = r5.getSource();
                r0 = r0 instanceof net.java.sip.communicator.service.protocol.ProtocolProviderService;
                if (r0 == 0) goto L_0x002f;
            L_0x0008:
                r0 = r5.getNewState();
                r1 = net.java.sip.communicator.service.protocol.RegistrationState.UNREGISTERED;
                r0 = r0.equals(r1);
                if (r0 != 0) goto L_0x0020;
            L_0x0014:
                r0 = r5.getNewState();
                r1 = net.java.sip.communicator.service.protocol.RegistrationState.CONNECTION_FAILED;
                r0 = r0.equals(r1);
                if (r0 == 0) goto L_0x002f;
            L_0x0020:
                monitor-enter(r4);
                r0 = r11;	 Catch:{ all -> 0x0068 }
                r0.removeRegistrationStateChangeListener(r4);	 Catch:{ all -> 0x0068 }
                r0 = net.java.sip.communicator.plugin.reconnectplugin.ReconnectPluginActivator.this;	 Catch:{ all -> 0x0068 }
                r0 = r0.timer;	 Catch:{ all -> 0x0068 }
                if (r0 != 0) goto L_0x0030;
            L_0x002e:
                monitor-exit(r4);	 Catch:{ all -> 0x0068 }
            L_0x002f:
                return;
            L_0x0030:
                r0 = net.java.sip.communicator.plugin.reconnectplugin.ReconnectPluginActivator.this;	 Catch:{ all -> 0x0068 }
                r0 = r0.connectedInterfaces;	 Catch:{ all -> 0x0068 }
                r0 = r0.size();	 Catch:{ all -> 0x0068 }
                if (r0 != 0) goto L_0x006b;
            L_0x003c:
                r0 = net.java.sip.communicator.plugin.reconnectplugin.ReconnectPluginActivator.this;	 Catch:{ all -> 0x0068 }
                r0 = r0.needsReconnection;	 Catch:{ all -> 0x0068 }
                r1 = r11;	 Catch:{ all -> 0x0068 }
                r0.add(r1);	 Catch:{ all -> 0x0068 }
                r0 = net.java.sip.communicator.plugin.reconnectplugin.ReconnectPluginActivator.this;	 Catch:{ all -> 0x0068 }
                r0 = r0.currentlyReconnecting;	 Catch:{ all -> 0x0068 }
                r1 = r11;	 Catch:{ all -> 0x0068 }
                r0 = r0.containsKey(r1);	 Catch:{ all -> 0x0068 }
                if (r0 == 0) goto L_0x0066;
            L_0x0055:
                r0 = net.java.sip.communicator.plugin.reconnectplugin.ReconnectPluginActivator.this;	 Catch:{ all -> 0x0068 }
                r0 = r0.currentlyReconnecting;	 Catch:{ all -> 0x0068 }
                r1 = r11;	 Catch:{ all -> 0x0068 }
                r0 = r0.remove(r1);	 Catch:{ all -> 0x0068 }
                r0 = (net.java.sip.communicator.plugin.reconnectplugin.ReconnectPluginActivator.ReconnectTask) r0;	 Catch:{ all -> 0x0068 }
                r0.cancel();	 Catch:{ all -> 0x0068 }
            L_0x0066:
                monitor-exit(r4);	 Catch:{ all -> 0x0068 }
                goto L_0x002f;
            L_0x0068:
                r0 = move-exception;
                monitor-exit(r4);	 Catch:{ all -> 0x0068 }
                throw r0;
            L_0x006b:
                r0 = net.java.sip.communicator.plugin.reconnectplugin.ReconnectPluginActivator.this;	 Catch:{ all -> 0x0068 }
                r0 = r0.currentlyReconnecting;	 Catch:{ all -> 0x0068 }
                r1 = r11;	 Catch:{ all -> 0x0068 }
                r0 = r0.containsKey(r1);	 Catch:{ all -> 0x0068 }
                if (r0 == 0) goto L_0x008a;
            L_0x0079:
                r0 = net.java.sip.communicator.plugin.reconnectplugin.ReconnectPluginActivator.this;	 Catch:{ all -> 0x0068 }
                r0 = r0.currentlyReconnecting;	 Catch:{ all -> 0x0068 }
                r1 = r11;	 Catch:{ all -> 0x0068 }
                r0 = r0.remove(r1);	 Catch:{ all -> 0x0068 }
                r0 = (net.java.sip.communicator.plugin.reconnectplugin.ReconnectPluginActivator.ReconnectTask) r0;	 Catch:{ all -> 0x0068 }
                r0.cancel();	 Catch:{ all -> 0x0068 }
            L_0x008a:
                r0 = net.java.sip.communicator.plugin.reconnectplugin.ReconnectPluginActivator.this;	 Catch:{ all -> 0x0068 }
                r0 = r0.currentlyReconnecting;	 Catch:{ all -> 0x0068 }
                r1 = r11;	 Catch:{ all -> 0x0068 }
                r2 = r3;	 Catch:{ all -> 0x0068 }
                r0.put(r1, r2);	 Catch:{ all -> 0x0068 }
                r0 = net.java.sip.communicator.plugin.reconnectplugin.ReconnectPluginActivator.logger;	 Catch:{ all -> 0x0068 }
                r0 = r0.isInfoEnabled();	 Catch:{ all -> 0x0068 }
                if (r0 == 0) goto L_0x00db;
            L_0x00a1:
                r0 = net.java.sip.communicator.plugin.reconnectplugin.ReconnectPluginActivator.logger;	 Catch:{ all -> 0x0068 }
                r1 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0068 }
                r1.<init>();	 Catch:{ all -> 0x0068 }
                r2 = "Reconnect ";
                r1 = r1.append(r2);	 Catch:{ all -> 0x0068 }
                r2 = r11;	 Catch:{ all -> 0x0068 }
                r2 = r2.getAccountID();	 Catch:{ all -> 0x0068 }
                r2 = r2.getDisplayName();	 Catch:{ all -> 0x0068 }
                r1 = r1.append(r2);	 Catch:{ all -> 0x0068 }
                r2 = " after ";
                r1 = r1.append(r2);	 Catch:{ all -> 0x0068 }
                r2 = r3;	 Catch:{ all -> 0x0068 }
                r2 = r2.delay;	 Catch:{ all -> 0x0068 }
                r1 = r1.append(r2);	 Catch:{ all -> 0x0068 }
                r2 = " ms.";
                r1 = r1.append(r2);	 Catch:{ all -> 0x0068 }
                r1 = r1.toString();	 Catch:{ all -> 0x0068 }
                r0.info(r1);	 Catch:{ all -> 0x0068 }
            L_0x00db:
                r0 = net.java.sip.communicator.plugin.reconnectplugin.ReconnectPluginActivator.this;	 Catch:{ all -> 0x0068 }
                r0 = r0.timer;	 Catch:{ all -> 0x0068 }
                r1 = r3;	 Catch:{ all -> 0x0068 }
                r2 = r3;	 Catch:{ all -> 0x0068 }
                r2 = r2.delay;	 Catch:{ all -> 0x0068 }
                r0.schedule(r1, r2);	 Catch:{ all -> 0x0068 }
                monitor-exit(r4);	 Catch:{ all -> 0x0068 }
                goto L_0x002f;
                */
                throw new UnsupportedOperationException("Method not decompiled: net.java.sip.communicator.plugin.reconnectplugin.ReconnectPluginActivator$AnonymousClass2.registrationStateChanged(net.java.sip.communicator.service.protocol.event.RegistrationStateChangeEvent):void");
            }
        };
        pp.addRegistrationStateChangeListener(listener);
        unregister(pp, true, listener, task);
    }

    private boolean hasAtLeastOneSuccessfulConnection(ProtocolProviderService pp) {
        String value = (String) getConfigurationService().getProperty("net.java.sip.communicator.plugin.reconnectplugin.ATLEAST_ONE_SUCCESSFUL_CONNECTION." + pp.getAccountID().getAccountUniqueID());
        if (value == null || !value.equals(Boolean.TRUE.toString())) {
            return false;
        }
        return true;
    }

    private void setAtLeastOneSuccessfulConnection(ProtocolProviderService pp, boolean value) {
        getConfigurationService().setProperty("net.java.sip.communicator.plugin.reconnectplugin.ATLEAST_ONE_SUCCESSFUL_CONNECTION." + pp.getAccountID().getAccountUniqueID(), Boolean.valueOf(value).toString());
    }

    private void onNetworkUp() {
        if (this.delayedNetworkDown != null) {
            this.delayedNetworkDown.cancel();
            this.delayedNetworkDown = null;
        }
    }

    private void onNetworkDown() {
        if (!OSUtils.IS_ANDROID) {
            notifyNetworkDown();
        } else if (this.delayedNetworkDown == null) {
            this.delayedNetworkDown = new Timer();
            this.delayedNetworkDown.schedule(new TimerTask() {
                public void run() {
                    ReconnectPluginActivator.this.notifyNetworkDown();
                }
            }, NETWORK_DOWN_THRESHOLD);
        }
    }

    /* access modifiers changed from: private */
    public void notifyNetworkDown() {
        if (logger.isTraceEnabled()) {
            logger.trace("Network is down!");
        }
        notify("", "plugin.reconnectplugin.NETWORK_DOWN", new String[0], this);
    }
}
