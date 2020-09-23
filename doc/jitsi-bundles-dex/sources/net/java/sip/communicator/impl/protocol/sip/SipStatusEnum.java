package net.java.sip.communicator.impl.protocol.sip;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import net.java.sip.communicator.service.protocol.PresenceStatus;

public class SipStatusEnum {
    public static final String AWAY = "Away";
    public static final String BUSY = "Busy (DND)";
    public static final String OFFLINE = "Offline";
    public static final String ONLINE = "Online";
    public static final String ON_THE_PHONE = "On the phone";
    public static final String UNKNOWN = "Unknown";
    private final SipPresenceStatus awayStatus;
    private final SipPresenceStatus busyStatus;
    private final SipPresenceStatus offlineStatus;
    private final SipPresenceStatus onThePhoneStatus;
    private final SipPresenceStatus onlineStatus;
    public final List<PresenceStatus> supportedStatusSet = new LinkedList();
    private final SipPresenceStatus unknownStatus;

    private static class SipPresenceStatus extends PresenceStatus {
        private SipPresenceStatus(int status, String statusName, byte[] statusIcon) {
            super(status, statusName, statusIcon);
        }
    }

    public SipStatusEnum(String iconPath) {
        this.offlineStatus = new SipPresenceStatus(0, OFFLINE, loadIcon(iconPath + "/sip16x16-offline.png"));
        this.busyStatus = new SipPresenceStatus(30, BUSY, loadIcon(iconPath + "/sip16x16-busy.png"));
        this.onThePhoneStatus = new SipPresenceStatus(31, ON_THE_PHONE, loadIcon(iconPath + "/sip16x16-phone.png"));
        this.awayStatus = new SipPresenceStatus(40, AWAY, loadIcon(iconPath + "/sip16x16-away.png"));
        this.onlineStatus = new SipPresenceStatus(65, ONLINE, loadIcon(iconPath + "/sip16x16-online.png"));
        this.unknownStatus = new SipPresenceStatus(1, UNKNOWN, loadIcon(iconPath + "/sip16x16-offline.png"));
        this.supportedStatusSet.add(this.onlineStatus);
        this.supportedStatusSet.add(this.awayStatus);
        this.supportedStatusSet.add(this.onThePhoneStatus);
        this.supportedStatusSet.add(this.busyStatus);
        this.supportedStatusSet.add(this.offlineStatus);
    }

    public SipPresenceStatus getStatus(String statusName) {
        if (statusName.equals(ONLINE)) {
            return this.onlineStatus;
        }
        if (statusName.equals(OFFLINE)) {
            return this.offlineStatus;
        }
        if (statusName.equals(BUSY)) {
            return this.busyStatus;
        }
        if (statusName.equals(ON_THE_PHONE)) {
            return this.onThePhoneStatus;
        }
        if (statusName.equals(AWAY)) {
            return this.awayStatus;
        }
        return this.unknownStatus;
    }

    public Iterator<PresenceStatus> getSupportedStatusSet() {
        return this.supportedStatusSet.iterator();
    }

    public static byte[] loadIcon(String imagePath) {
        return ProtocolIconSipImpl.loadIcon(imagePath);
    }
}
