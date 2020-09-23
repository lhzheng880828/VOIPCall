package net.java.sip.communicator.impl.protocol.jabber.extensions;

public class CallIdPacketExtension extends AbstractPacketExtension {
    public CallIdPacketExtension(String callid) {
        this();
        setText(callid);
    }

    public CallIdPacketExtension() {
        super(ConferenceDescriptionPacketExtension.NAMESPACE, "callid");
    }
}
