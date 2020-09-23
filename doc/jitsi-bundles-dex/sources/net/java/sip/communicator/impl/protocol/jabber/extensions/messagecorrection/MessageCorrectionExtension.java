package net.java.sip.communicator.impl.protocol.jabber.extensions.messagecorrection;

import org.jivesoftware.smack.packet.PacketExtension;

public class MessageCorrectionExtension implements PacketExtension {
    public static final String ELEMENT_NAME = "replace";
    public static final String ID_ATTRIBUTE_NAME = "id";
    public static final String NAMESPACE = "urn:xmpp:message-correct:0";
    public static final String SWIFT_NAMESPACE = "http://swift.im/protocol/replace";
    private String correctedMessageUID;

    public MessageCorrectionExtension(String correctedMessageUID) {
        this.correctedMessageUID = correctedMessageUID;
    }

    public String getElementName() {
        return ELEMENT_NAME;
    }

    public String getNamespace() {
        return NAMESPACE;
    }

    public String toXML() {
        return "<replace id='" + this.correctedMessageUID + "' xmlns='" + NAMESPACE + "' />";
    }

    public String getCorrectedMessageUID() {
        return this.correctedMessageUID;
    }

    public void setCorrectedMessageUID(String correctedMessageUID) {
        this.correctedMessageUID = correctedMessageUID;
    }
}
