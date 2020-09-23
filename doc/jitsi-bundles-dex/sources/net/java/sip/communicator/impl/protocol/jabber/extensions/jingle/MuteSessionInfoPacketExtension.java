package net.java.sip.communicator.impl.protocol.jabber.extensions.jingle;

public class MuteSessionInfoPacketExtension extends SessionInfoPacketExtension {
    public static final String NAME_ATTR_VALUE = "name";

    public MuteSessionInfoPacketExtension(boolean mute, String name) {
        super(mute ? SessionInfoType.mute : SessionInfoType.unmute);
        setAttribute("name", name);
    }

    public boolean isMute() {
        return getType() == SessionInfoType.mute;
    }

    public String getName() {
        return getAttributeAsString("name");
    }
}
