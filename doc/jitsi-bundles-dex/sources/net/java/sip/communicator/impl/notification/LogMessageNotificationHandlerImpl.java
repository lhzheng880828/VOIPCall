package net.java.sip.communicator.impl.notification;

import net.java.sip.communicator.service.notification.LogMessageNotificationAction;
import net.java.sip.communicator.service.notification.LogMessageNotificationHandler;
import net.java.sip.communicator.util.Logger;

public class LogMessageNotificationHandlerImpl implements LogMessageNotificationHandler {
    private Logger logger = Logger.getLogger(LogMessageNotificationHandlerImpl.class);

    public String getActionType() {
        return "LogMessageAction";
    }

    public void logMessage(LogMessageNotificationAction action, String message) {
        if (action.getLogType().equals("ErrorLog")) {
            this.logger.error(message);
        } else if (action.getLogType().equals("InfoLog")) {
            this.logger.info(message);
        } else if (action.getLogType().equals("TraceLog")) {
            this.logger.trace(message);
        }
    }
}
