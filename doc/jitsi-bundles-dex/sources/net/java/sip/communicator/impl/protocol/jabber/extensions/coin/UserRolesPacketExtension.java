package net.java.sip.communicator.impl.protocol.jabber.extensions.coin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import net.java.sip.communicator.impl.protocol.jabber.extensions.AbstractPacketExtension;
import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.packet.PacketExtension;

public class UserRolesPacketExtension extends AbstractPacketExtension {
    public static final String ELEMENT_NAME = "roles";
    public static final String ELEMENT_ROLE = "entry";
    public static final String NAMESPACE = "";
    private List<String> roles = new ArrayList();

    public UserRolesPacketExtension() {
        super("", ELEMENT_NAME);
    }

    public void addRoles(String role) {
        this.roles.add(role);
    }

    public List<String> getRoles() {
        return this.roles;
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
        for (String role : this.roles) {
            bldr.append(Separators.LESS_THAN).append(ELEMENT_ROLE).append(Separators.GREATER_THAN).append(role).append("</").append(ELEMENT_ROLE).append(Separators.GREATER_THAN);
        }
        for (PacketExtension ext : getChildExtensions()) {
            bldr.append(ext.toXML());
        }
        bldr.append("</").append(getElementName()).append(Separators.GREATER_THAN);
        return bldr.toString();
    }
}
