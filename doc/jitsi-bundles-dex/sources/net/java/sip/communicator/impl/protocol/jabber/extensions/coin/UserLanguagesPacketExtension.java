package net.java.sip.communicator.impl.protocol.jabber.extensions.coin;

import java.util.Map.Entry;
import net.java.sip.communicator.impl.protocol.jabber.extensions.AbstractPacketExtension;
import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.packet.PacketExtension;

public class UserLanguagesPacketExtension extends AbstractPacketExtension {
    public static final String ELEMENT_LANGUAGES = "stringvalues";
    public static final String ELEMENT_NAME = "languages";
    public static final String NAMESPACE = "";
    private String languages = null;

    public UserLanguagesPacketExtension() {
        super("", ELEMENT_NAME);
    }

    public void setLanguages(String languages) {
        this.languages = languages;
    }

    public String getLanguages() {
        return this.languages;
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
        if (this.languages != null) {
            bldr.append(Separators.LESS_THAN).append(ELEMENT_LANGUAGES).append(Separators.GREATER_THAN).append(this.languages).append("</").append(ELEMENT_LANGUAGES).append(Separators.GREATER_THAN);
        }
        for (PacketExtension ext : getChildExtensions()) {
            bldr.append(ext.toXML());
        }
        bldr.append("</").append(getElementName()).append(Separators.GREATER_THAN);
        return bldr.toString();
    }
}
