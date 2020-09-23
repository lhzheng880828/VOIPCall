package net.java.sip.communicator.impl.notification;

import net.java.sip.communicator.service.notification.PopupMessageNotificationAction;
import net.java.sip.communicator.service.notification.PopupMessageNotificationHandler;
import net.java.sip.communicator.service.systray.PopupMessage;
import net.java.sip.communicator.service.systray.SystrayService;
import net.java.sip.communicator.service.systray.event.SystrayPopupMessageListener;
import net.java.sip.communicator.util.Logger;
import org.jitsi.util.StringUtils;

public class PopupMessageNotificationHandlerImpl implements PopupMessageNotificationHandler {
    private Logger logger = Logger.getLogger(PopupMessageNotificationHandlerImpl.class);

    public String getActionType() {
        return "PopupMessageAction";
    }

    public void popupMessage(PopupMessageNotificationAction action, String title, String message, byte[] icon, Object tag) {
        SystrayService systray = NotificationActivator.getSystray();
        if (systray != null) {
            if (StringUtils.isNullOrEmpty(message)) {
                this.logger.error("Message is null or empty!");
                return;
            }
            PopupMessage popupMsg = new PopupMessage(title, message, icon, tag);
            popupMsg.setTimeout(action.getTimeout());
            popupMsg.setGroup(action.getGroupName());
            systray.showPopupMessage(popupMsg);
        }
    }

    public void addPopupMessageListener(SystrayPopupMessageListener listener) {
        SystrayService systray = NotificationActivator.getSystray();
        if (systray != null) {
            systray.addPopupMessageListener(listener);
        }
    }

    public void removePopupMessageListener(SystrayPopupMessageListener listener) {
        SystrayService systray = NotificationActivator.getSystray();
        if (systray != null) {
            systray.removePopupMessageListener(listener);
        }
    }
}
