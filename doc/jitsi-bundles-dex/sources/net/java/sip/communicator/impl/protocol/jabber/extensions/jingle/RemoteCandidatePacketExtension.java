package net.java.sip.communicator.impl.protocol.jabber.extensions.jingle;

public class RemoteCandidatePacketExtension extends CandidatePacketExtension {
    public static final String ELEMENT_NAME = "remote-candidate";

    public RemoteCandidatePacketExtension() {
        super(ELEMENT_NAME);
    }
}
