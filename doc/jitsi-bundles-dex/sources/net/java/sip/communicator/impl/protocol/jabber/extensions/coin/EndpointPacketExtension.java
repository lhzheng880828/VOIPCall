package net.java.sip.communicator.impl.protocol.jabber.extensions.coin;

import java.util.Map.Entry;
import net.java.sip.communicator.impl.protocol.jabber.extensions.AbstractPacketExtension;
import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.packet.PacketExtension;

public class EndpointPacketExtension extends AbstractPacketExtension {
    public static final String ELEMENT_DISCONNECTION = "disconnection-method";
    public static final String ELEMENT_DISPLAY_TEXT = "display-text";
    public static final String ELEMENT_JOINING = "joining-method";
    public static final String ELEMENT_NAME = "endpoint";
    public static final String ELEMENT_STATUS = "status";
    public static final String ENTITY_ATTR_NAME = "entity";
    public static final String NAMESPACE = null;
    public static final String STATE_ATTR_NAME = "state";
    private DisconnectionType disconnectionType = null;
    private String displayText = null;
    private JoiningType joiningType = null;
    private EndpointStatusType status = null;

    public EndpointPacketExtension(String entity) {
        super(NAMESPACE, "endpoint");
        setAttribute("entity", entity);
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }

    public void setStatus(EndpointStatusType status) {
        this.status = status;
    }

    public void setDisconnectionType(DisconnectionType disconnectionType) {
        this.disconnectionType = disconnectionType;
    }

    public void setJoiningType(JoiningType joiningType) {
        this.joiningType = joiningType;
    }

    public String getDisplayText() {
        return this.displayText;
    }

    public EndpointStatusType getStatus() {
        return this.status;
    }

    public DisconnectionType getDisconnectionType() {
        return this.disconnectionType;
    }

    public JoiningType getJoiningType() {
        return this.joiningType;
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
        if (this.status != null) {
            bldr.append(Separators.LESS_THAN).append("status").append(Separators.GREATER_THAN).append(this.status).append("</").append("status").append(Separators.GREATER_THAN);
        }
        if (this.disconnectionType != null) {
            bldr.append(Separators.LESS_THAN).append(ELEMENT_DISCONNECTION).append(Separators.GREATER_THAN).append(this.disconnectionType).append("</").append(ELEMENT_DISCONNECTION).append(Separators.GREATER_THAN);
        }
        if (this.joiningType != null) {
            bldr.append(Separators.LESS_THAN).append(ELEMENT_JOINING).append(Separators.GREATER_THAN).append(this.joiningType).append("</").append(ELEMENT_JOINING).append(Separators.GREATER_THAN);
        }
        for (PacketExtension ext : getChildExtensions()) {
            bldr.append(ext.toXML());
        }
        bldr.append("</").append("endpoint").append(Separators.GREATER_THAN);
        return bldr.toString();
    }
}
