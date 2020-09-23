package net.java.sip.communicator.impl.protocol.jabber.extensions.coin;

import java.util.Map.Entry;
import net.java.sip.communicator.impl.protocol.jabber.extensions.AbstractPacketExtension;
import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.packet.PacketExtension;

public class MediaPacketExtension extends AbstractPacketExtension {
    public static final String ELEMENT_DISPLAY_TEXT = "display-text";
    public static final String ELEMENT_LABEL = "label";
    public static final String ELEMENT_NAME = "media";
    public static final String ELEMENT_SRC_ID = "src-id";
    public static final String ELEMENT_STATUS = "status";
    public static final String ELEMENT_TYPE = "type";
    public static final String ID_ATTR_NAME = "id";
    public static final String NAMESPACE = null;
    private String displayText = null;
    private String label = null;
    private String srcId = null;
    private String status = null;
    private String type = null;

    public MediaPacketExtension(String id) {
        super(NAMESPACE, "media");
        setAttribute("id", id);
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setSrcID(String srcId) {
        this.srcId = srcId;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }

    public String getDisplayText() {
        return this.displayText;
    }

    public String getType() {
        return this.type;
    }

    public String getLabel() {
        return this.label;
    }

    public String getStatus() {
        return this.status;
    }

    public String getSrcID() {
        return this.srcId;
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
        if (this.type != null) {
            bldr.append(Separators.LESS_THAN).append("type").append(Separators.GREATER_THAN).append(this.type).append("</").append("type").append(Separators.GREATER_THAN);
        }
        if (this.srcId != null) {
            bldr.append(Separators.LESS_THAN).append(ELEMENT_SRC_ID).append(Separators.GREATER_THAN).append(this.srcId).append("</").append(ELEMENT_SRC_ID).append(Separators.GREATER_THAN);
        }
        if (this.status != null) {
            bldr.append(Separators.LESS_THAN).append("status").append(Separators.GREATER_THAN).append(this.status).append("</").append("status").append(Separators.GREATER_THAN);
        }
        if (this.label != null) {
            bldr.append(Separators.LESS_THAN).append("label").append(Separators.GREATER_THAN).append(this.label).append("</").append("label").append(Separators.GREATER_THAN);
        }
        for (PacketExtension ext : getChildExtensions()) {
            bldr.append(ext.toXML());
        }
        bldr.append("</").append(getElementName()).append(Separators.GREATER_THAN);
        return bldr.toString();
    }
}
