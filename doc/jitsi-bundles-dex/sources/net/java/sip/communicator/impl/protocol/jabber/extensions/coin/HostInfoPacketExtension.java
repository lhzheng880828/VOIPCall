package net.java.sip.communicator.impl.protocol.jabber.extensions.coin;

import java.util.Map.Entry;
import net.java.sip.communicator.impl.protocol.jabber.extensions.AbstractPacketExtension;
import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.packet.PacketExtension;

public class HostInfoPacketExtension extends AbstractPacketExtension {
    public static final String ELEMENT_DISPLAY_TEXT = "display-text";
    public static final String ELEMENT_NAME = "host-info";
    public static final String ELEMENT_WEB_PAGE = "web-page";
    public static final String NAMESPACE = null;
    private String displayText = null;
    private String webPage = null;

    public HostInfoPacketExtension() {
        super(NAMESPACE, ELEMENT_NAME);
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }

    public String getDisplayText() {
        return this.displayText;
    }

    public void setWebPage(String webPage) {
        this.webPage = webPage;
    }

    public String getWebPage() {
        return this.webPage;
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
        if (this.webPage != null) {
            bldr.append(Separators.LESS_THAN).append(ELEMENT_WEB_PAGE).append(Separators.GREATER_THAN).append(this.webPage).append("</").append(ELEMENT_WEB_PAGE).append(Separators.GREATER_THAN);
        }
        for (PacketExtension ext : getChildExtensions()) {
            bldr.append(ext.toXML());
        }
        bldr.append("</").append(ELEMENT_NAME).append(Separators.GREATER_THAN);
        return bldr.toString();
    }
}
