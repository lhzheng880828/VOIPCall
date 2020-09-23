package net.java.sip.communicator.impl.protocol.jabber.extensions.coin;

import java.util.Map.Entry;
import net.java.sip.communicator.impl.protocol.jabber.extensions.AbstractPacketExtension;
import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.packet.PacketExtension;

public class DescriptionPacketExtension extends AbstractPacketExtension {
    public static final String ELEMENT_DISPLAY_TEXT = "display-text";
    public static final String ELEMENT_FREE_TEXT = "free-text";
    public static final String ELEMENT_MAX_USER_COUNT = "maximum-user-count";
    public static final String ELEMENT_NAME = "conference-description";
    public static final String ELEMENT_SUBJECT = "subject";
    public static final String NAMESPACE = null;
    private String displayText = null;
    private String freeText = null;
    private int maximumUserCount = 0;
    private String subject = "";

    public DescriptionPacketExtension() {
        super(NAMESPACE, ELEMENT_NAME);
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }

    public void setFreeText(String freeText) {
        this.freeText = freeText;
    }

    public String getSubject() {
        return this.subject;
    }

    public String getDisplayText() {
        return this.displayText;
    }

    public String getFreeText() {
        return this.freeText;
    }

    public String toXML() {
        StringBuilder bldr = new StringBuilder();
        bldr.append(Separators.LESS_THAN).append(getElementName()).append(Separators.SP);
        if (getNamespace() != null) {
            bldr.append("xmlns='").append(getNamespace()).append(Separators.QUOTE);
        }
        for (Entry<String, String> entry : this.attributes.entrySet()) {
            bldr.append(Separators.SP).append((String) entry.getKey()).append("='").append((String) entry.getValue()).append(Separators.QUOTE);
        }
        bldr.append(Separators.GREATER_THAN);
        if (this.subject != null) {
            bldr.append(Separators.LESS_THAN).append("subject").append(Separators.GREATER_THAN).append(this.subject).append("</").append("subject").append(Separators.GREATER_THAN);
        }
        if (this.displayText != null) {
            bldr.append(Separators.LESS_THAN).append("display-text").append(Separators.GREATER_THAN).append(this.displayText).append("</").append("display-text").append(Separators.GREATER_THAN);
        }
        if (this.freeText != null) {
            bldr.append(Separators.LESS_THAN).append(ELEMENT_FREE_TEXT).append(Separators.GREATER_THAN).append(this.freeText).append("</").append(ELEMENT_FREE_TEXT).append(Separators.GREATER_THAN);
        }
        if (this.maximumUserCount != 0) {
            bldr.append(Separators.LESS_THAN).append(ELEMENT_MAX_USER_COUNT).append(Separators.GREATER_THAN).append(this.maximumUserCount).append("</").append(ELEMENT_MAX_USER_COUNT).append(Separators.GREATER_THAN);
        }
        for (PacketExtension ext : getChildExtensions()) {
            bldr.append(ext.toXML());
        }
        bldr.append("</").append(getElementName()).append(Separators.GREATER_THAN);
        return bldr.toString();
    }
}
