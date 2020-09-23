package net.java.sip.communicator.impl.notification;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import net.java.sip.communicator.service.notification.CommandNotificationAction;
import net.java.sip.communicator.service.notification.CommandNotificationHandler;
import net.java.sip.communicator.util.Logger;
import org.jitsi.util.StringUtils;

public class CommandNotificationHandlerImpl implements CommandNotificationHandler {
    private Logger logger = Logger.getLogger(CommandNotificationHandlerImpl.class);

    public String getActionType() {
        return "CommandAction";
    }

    public void execute(CommandNotificationAction action, Map<String, String> cmdargs) {
        String actionDescriptor = action.getDescriptor();
        if (!StringUtils.isNullOrEmpty(actionDescriptor, true)) {
            if (cmdargs != null) {
                for (Entry<String, String> cmdarg : cmdargs.entrySet()) {
                    actionDescriptor = actionDescriptor.replace("${" + ((String) cmdarg.getKey()) + "}", (CharSequence) cmdarg.getValue());
                }
            }
            try {
                Runtime.getRuntime().exec(actionDescriptor);
            } catch (IOException ioe) {
                this.logger.error("Failed to execute the following command: " + action.getDescriptor(), ioe);
            }
        }
    }
}
