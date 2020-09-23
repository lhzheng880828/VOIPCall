package net.java.sip.communicator.impl.protocol.jabber.extensions.jingleinfo;

import net.java.sip.communicator.impl.protocol.jabber.extensions.AbstractPacketExtension;
import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.packet.PacketExtension;

public class RelayPacketExtension extends AbstractPacketExtension {
    public static final String ELEMENT_NAME = "relay";
    public static final String NAMESPACE = null;
    private String token = null;

    public RelayPacketExtension() {
        super(NAMESPACE, ELEMENT_NAME);
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return this.token;
    }

    public String toXML() {
        StringBuilder bld = new StringBuilder();
        bld.append(Separators.LESS_THAN).append(ELEMENT_NAME).append(Separators.GREATER_THAN);
        if (this.token != null) {
            bld.append(Separators.LESS_THAN).append("token").append(Separators.GREATER_THAN);
            bld.append(this.token);
            bld.append("</").append("token").append(Separators.GREATER_THAN);
        }
        for (PacketExtension pe : getChildExtensions()) {
            bld.append(pe.toXML());
        }
        bld.append("</").append(ELEMENT_NAME).append(Separators.GREATER_THAN);
        return bld.toString();
    }
}
