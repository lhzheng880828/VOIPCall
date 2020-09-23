package net.java.sip.communicator.impl.protocol.jabber.extensions.coin;

import java.util.Map.Entry;
import net.java.sip.communicator.impl.protocol.jabber.extensions.AbstractPacketExtension;
import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.packet.PacketExtension;

public class StatePacketExtension extends AbstractPacketExtension {
    public static final String ELEMENT_ACTIVE = "active";
    public static final String ELEMENT_LOCKED = "locked";
    public static final String ELEMENT_NAME = "conference-state";
    public static final String ELEMENT_USER_COUNT = "user-count";
    public static final String NAMESPACE = null;
    private int active = -1;
    private int locked = -1;
    private int userCount = 0;

    public StatePacketExtension() {
        super(NAMESPACE, ELEMENT_NAME);
    }

    public void setUserCount(int userCount) {
        this.userCount = userCount;
    }

    public void setActive(int active) {
        this.active = active;
    }

    public void setLocked(int locked) {
        this.locked = locked;
    }

    public int getUserCount() {
        return this.userCount;
    }

    public int getActive() {
        return this.active;
    }

    public int getLocked() {
        return this.locked;
    }

    public String toXML() {
        boolean z = true;
        StringBuilder bldr = new StringBuilder();
        bldr.append(Separators.LESS_THAN).append(getElementName()).append(Separators.SP);
        if (getNamespace() != null) {
            bldr.append("xmlns='").append(getNamespace()).append(Separators.QUOTE);
        }
        for (Entry<String, String> entry : this.attributes.entrySet()) {
            bldr.append(Separators.SP).append((String) entry.getKey()).append("='").append((String) entry.getValue()).append(Separators.QUOTE);
        }
        bldr.append(Separators.GREATER_THAN);
        if (this.userCount != 0) {
            bldr.append(Separators.LESS_THAN).append(ELEMENT_USER_COUNT).append(Separators.GREATER_THAN).append(this.userCount).append("</").append(ELEMENT_USER_COUNT).append(Separators.GREATER_THAN);
        }
        if (this.active != -1) {
            boolean z2;
            StringBuilder append = bldr.append(Separators.LESS_THAN).append("active").append(Separators.GREATER_THAN);
            if (this.active > 0) {
                z2 = true;
            } else {
                z2 = false;
            }
            append.append(z2).append("</").append("active").append(Separators.GREATER_THAN);
        }
        if (this.locked != -1) {
            StringBuilder append2 = bldr.append(Separators.LESS_THAN).append(ELEMENT_LOCKED).append(Separators.GREATER_THAN);
            if (this.active <= 0) {
                z = false;
            }
            append2.append(z).append("</").append(ELEMENT_LOCKED).append(Separators.GREATER_THAN);
        }
        for (PacketExtension ext : getChildExtensions()) {
            bldr.append(ext.toXML());
        }
        bldr.append("</").append(getElementName()).append(Separators.GREATER_THAN);
        return bldr.toString();
    }
}
