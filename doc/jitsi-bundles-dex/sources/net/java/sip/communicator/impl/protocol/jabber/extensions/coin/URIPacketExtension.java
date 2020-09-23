package net.java.sip.communicator.impl.protocol.jabber.extensions.coin;

import java.util.Map.Entry;
import net.java.sip.communicator.impl.protocol.jabber.extensions.AbstractPacketExtension;
import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.packet.PacketExtension;

public class URIPacketExtension extends AbstractPacketExtension {
    public static final String ELEMENT_DISPLAY_TEXT = "display-text";
    public static final String ELEMENT_NAME = "uri";
    public static final String ELEMENT_PURPOSE = "purpose";
    public static final String NAMESPACE = "";
    private String displayText = null;
    private String purpose = null;

    public URIPacketExtension(String elementName) {
        super("", elementName);
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }

    public String getDisplayText() {
        return this.displayText;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getPurpose() {
        return this.purpose;
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
        if (this.displayText != null) {
            bldr.append(Separators.LESS_THAN).append("display-text").append(Separators.GREATER_THAN).append(this.displayText).append("</").append("display-text").append(Separators.GREATER_THAN);
        }
        if (this.purpose != null) {
            bldr.append(Separators.LESS_THAN).append("purpose").append(Separators.GREATER_THAN).append(this.purpose).append("</").append("purpose").append(Separators.GREATER_THAN);
        }
        for (PacketExtension ext : getChildExtensions()) {
            bldr.append(ext.toXML());
        }
        bldr.append("</").append(getElementName()).append(Separators.GREATER_THAN);
        return bldr.toString();
    }
}
