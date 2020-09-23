package net.java.sip.communicator.impl.protocol.jabber.extensions.coin;

import java.util.Map.Entry;
import net.java.sip.communicator.impl.protocol.jabber.extensions.AbstractPacketExtension;
import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.packet.PacketExtension;

public class ExecutionPacketExtension extends AbstractPacketExtension {
    public static final String ELEMENT_BY = "by";
    public static final String ELEMENT_DISCONNECTION_NAME = "disconnection-info";
    public static final String ELEMENT_JOINING_NAME = "joining-info";
    public static final String ELEMENT_MODIFIED_NAME = "modified";
    public static final String ELEMENT_REASON = "reason";
    public static final String ELEMENT_REFERRED_NAME = "referred";
    public static final String ELEMENT_WHEN = "display-text";
    public static final String NAMESPACE = null;
    private String by = null;
    private String reason = null;
    private String when = null;

    public void setBy(String by) {
        this.by = by;
    }

    public String getBy() {
        return this.by;
    }

    public void setWhen(String when) {
        this.when = when;
    }

    public String getWhen() {
        return this.when;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return this.reason;
    }

    public ExecutionPacketExtension(String elementName) {
        super(NAMESPACE, elementName);
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
        if (this.by != null) {
            bldr.append(Separators.LESS_THAN).append(ELEMENT_BY).append(Separators.GREATER_THAN).append(this.by).append("</").append(ELEMENT_BY).append(Separators.GREATER_THAN);
        }
        if (this.when != null) {
            bldr.append(Separators.LESS_THAN).append("display-text").append(Separators.GREATER_THAN).append(this.when).append("</").append("display-text").append(Separators.GREATER_THAN);
        }
        if (this.reason != null) {
            bldr.append(Separators.LESS_THAN).append("reason").append(Separators.GREATER_THAN).append(this.reason).append("</").append("reason").append(Separators.GREATER_THAN);
        }
        for (PacketExtension ext : getChildExtensions()) {
            bldr.append(ext.toXML());
        }
        bldr.append("</").append(getElementName()).append(Separators.GREATER_THAN);
        return bldr.toString();
    }
}
