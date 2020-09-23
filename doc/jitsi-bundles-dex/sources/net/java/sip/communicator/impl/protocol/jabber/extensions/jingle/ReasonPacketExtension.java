package net.java.sip.communicator.impl.protocol.jabber.extensions.jingle;

import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.packet.PacketExtension;

public class ReasonPacketExtension implements PacketExtension {
    public static final String ELEMENT_NAME = "reason";
    public static final String NAMESPACE = "";
    public static final String TEXT_ELEMENT_NAME = "text";
    private PacketExtension otherExtension;
    private final Reason reason;
    private String text;

    public ReasonPacketExtension(Reason reason, String text, PacketExtension packetExtension) {
        this.reason = reason;
        this.text = text;
        this.otherExtension = packetExtension;
    }

    public Reason getReason() {
        return this.reason;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public PacketExtension getOtherExtension() {
        return this.otherExtension;
    }

    public void setOtherExtension(PacketExtension otherExtension) {
        this.otherExtension = otherExtension;
    }

    public String getElementName() {
        return "reason";
    }

    public String getNamespace() {
        return "";
    }

    public String toXML() {
        StringBuilder bldr = new StringBuilder(Separators.LESS_THAN + getElementName() + Separators.GREATER_THAN);
        bldr.append(Separators.LESS_THAN + getReason().toString() + "/>");
        if (getText() != null) {
            bldr.append("<text>");
            bldr.append(getText());
            bldr.append("</text>");
        }
        if (getOtherExtension() != null) {
            bldr.append(getOtherExtension().toXML());
        }
        bldr.append("</" + getElementName() + Separators.GREATER_THAN);
        return bldr.toString();
    }
}
