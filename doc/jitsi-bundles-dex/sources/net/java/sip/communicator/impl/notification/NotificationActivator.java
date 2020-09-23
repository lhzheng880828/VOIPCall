package net.java.sip.communicator.impl.notification;

import net.java.sip.communicator.service.gui.UIService;
import net.java.sip.communicator.service.notification.CommandNotificationHandler;
import net.java.sip.communicator.service.notification.LogMessageNotificationHandler;
import net.java.sip.communicator.service.notification.NotificationService;
import net.java.sip.communicator.service.notification.PopupMessageNotificationHandler;
import net.java.sip.communicator.service.notification.SoundNotificationHandler;
import net.java.sip.communicator.service.systray.SystrayService;
import net.java.sip.communicator.util.Logger;
import net.java.sip.communicator.util.ServiceUtils;
import org.jitsi.service.audionotifier.AudioNotifierService;
import org.jitsi.service.configuration.ConfigurationService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class NotificationActivator implements BundleActivator {
    private static AudioNotifierService audioNotifierService;
    protected static BundleContext bundleContext;
    private static ConfigurationService configurationService;
    private static NotificationService notificationService;
    private static SystrayService systrayService;
    private static UIService uiService = null;
    private CommandNotificationHandler commandHandler;
    private LogMessageNotificationHandler logMessageHandler;
    private final Logger logger = Logger.getLogger(NotificationActivator.class);
    private PopupMessageNotificationHandler popupMessageHandler;
    private SoundNotificationHandler soundHandler;

    public void start(BundleContext bc) throws Exception {
        bundleContext = bc;
        try {
            this.logger.logEntry();
            this.logger.info("Notification handler Service...[  STARTED ]");
            notificationService = (NotificationService) bundleContext.getService(bundleContext.getServiceReference(NotificationService.class.getName()));
            this.commandHandler = new CommandNotificationHandlerImpl();
            this.logMessageHandler = new LogMessageNotificationHandlerImpl();
            this.popupMessageHandler = new PopupMessageNotificationHandlerImpl();
            this.soundHandler = new SoundNotificationHandlerImpl();
            notificationService.addActionHandler(this.commandHandler);
            notificationService.addActionHandler(this.logMessageHandler);
            notificationService.addActionHandler(this.popupMessageHandler);
            notificationService.addActionHandler(this.soundHandler);
            this.logger.info("Notification handler Service ...[REGISTERED]");
        } finally {
            this.logger.logExit();
        }
    }

    public void stop(BundleContext bc) throws Exception {
        notificationService.removeActionHandler(this.commandHandler.getActionType());
        notificationService.removeActionHandler(this.logMessageHandler.getActionType());
        notificationService.removeActionHandler(this.popupMessageHandler.getActionType());
        notificationService.removeActionHandler(this.soundHandler.getActionType());
        this.logger.info("Notification handler Service ...[STOPPED]");
    }

    public static AudioNotifierService getAudioNotifier() {
        if (audioNotifierService == null) {
            ServiceReference serviceReference = bundleContext.getServiceReference(AudioNotifierService.class.getName());
            if (serviceReference != null) {
                audioNotifierService = (AudioNotifierService) bundleContext.getService(serviceReference);
            }
        }
        return audioNotifierService;
    }

    public static SystrayService getSystray() {
        if (systrayService == null) {
            systrayService = (SystrayService) ServiceUtils.getService(bundleContext, SystrayService.class);
        }
        return systrayService;
    }

    public static UIService getUIService() {
        if (uiService == null) {
            uiService = (UIService) ServiceUtils.getService(bundleContext, UIService.class);
        }
        return uiService;
    }

    public static ConfigurationService getConfigurationService() {
        if (configurationService == null) {
            configurationService = (ConfigurationService) ServiceUtils.getService(bundleContext, ConfigurationService.class);
        }
        return configurationService;
    }
}
