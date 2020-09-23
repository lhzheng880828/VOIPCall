package net.java.sip.communicator.impl.protocol.jabber.extensions.coin;

import java.util.Map.Entry;
import net.java.sip.communicator.impl.protocol.jabber.extensions.AbstractPacketExtension;
import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.packet.PacketExtension;

public class SIPDialogIDPacketExtension extends AbstractPacketExtension {
    public static final String ELEMENT_CALLID = "call-id";
    public static final String ELEMENT_DISPLAY_TEXT = "display-text";
    public static final String ELEMENT_FROMTAG = "from-tag";
    public static final String ELEMENT_NAME = "sip";
    public static final String ELEMENT_TOTAG = "to-tag";
    public static final String NAMESPACE = "";
    private String callID = null;
    private String displayText = null;
    private String fromTag = null;
    private String toTag = null;

    public SIPDialogIDPacketExtension() {
        super("", "sip");
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
        if (this.callID != null) {
            bldr.append(Separators.LESS_THAN).append(ELEMENT_CALLID).append(Separators.GREATER_THAN).append(this.callID).append("</").append(ELEMENT_CALLID).append(Separators.GREATER_THAN);
        }
        if (this.fromTag != null) {
            bldr.append(Separators.LESS_THAN).append("from-tag").append(Separators.GREATER_THAN).append(this.fromTag).append("</").append("from-tag").append(Separators.GREATER_THAN);
        }
        if (this.toTag != null) {
            bldr.append(Separators.LESS_THAN).append("to-tag").append(Separators.GREATER_THAN).append(this.toTag).append("</").append("to-tag").append(Separators.GREATER_THAN);
        }
        for (PacketExtension ext : getChildExtensions()) {
            bldr.append(ext.toXML());
        }
        bldr.append("</").append(getElementName()).append(Separators.GREATER_THAN);
        return bldr.toString();
    }
}
